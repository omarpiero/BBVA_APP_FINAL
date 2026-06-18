-- ============================================================================
-- bd_core_mobile - 05) Flujo demo de creditos E2E
-- Estado: BASE DOCUMENTAL; remoto actualizado el 2026-06-17 con la migracion
--         enable_demo_rls_credit_flow adaptada al esquema real.
-- Fecha: 2026-06-16
-- ----------------------------------------------------------------------------
-- Objetivo:
--   1. Vincular clientes core con Supabase Auth mediante clientes.auth_user_id.
--   2. Crear usuarios demo para cliente, asesor y administrador.
--   3. Agregar defaults de solicitud, calculo de cuota y transicion de estados.
--   4. Habilitar RLS de prueba con reglas por auth.uid() y perfil de asesor.
--
-- Nota operativa:
--   Este archivo requiere aprobacion explicita antes de ejecutarse completo
--   contra el proyecto remoto. El remoto ya recibio el parche minimo de RLS/RPC
--   para el flujo demo usando los usuarios existentes.
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE public.clientes
  ADD COLUMN IF NOT EXISTS auth_user_id UUID;

DO $$
BEGIN
  IF to_regclass('auth.users') IS NOT NULL
     AND NOT EXISTS (
       SELECT 1
       FROM pg_constraint
       WHERE conname = 'clientes_auth_user_id_fkey'
         AND conrelid = 'public.clientes'::regclass
     ) THEN
    ALTER TABLE public.clientes
      ADD CONSTRAINT clientes_auth_user_id_fkey
      FOREIGN KEY (auth_user_id) REFERENCES auth.users(id) ON DELETE SET NULL;
  END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS idx_clientes_auth_user_id
  ON public.clientes(auth_user_id)
  WHERE auth_user_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_clientes_email
  ON public.clientes(email);

CREATE INDEX IF NOT EXISTS idx_solicitudes_cliente_created
  ON public.solicitudes_credito(cliente_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_solicitudes_asesor_created
  ON public.solicitudes_credito(asesor_id, created_at DESC);

CREATE SEQUENCE IF NOT EXISTS public.solicitudes_credito_expediente_seq
  START WITH 1001;

CREATE OR REPLACE FUNCTION public.bbva_calcular_cuota(
  p_monto NUMERIC,
  p_plazo_meses INTEGER,
  p_tea NUMERIC DEFAULT 43.92
) RETURNS NUMERIC
LANGUAGE plpgsql
IMMUTABLE
SET search_path = public
AS $$
DECLARE
  v_tem NUMERIC;
  v_factor NUMERIC;
BEGIN
  IF p_monto IS NULL OR p_plazo_meses IS NULL OR p_monto <= 0 OR p_plazo_meses <= 0 THEN
    RETURN 0;
  END IF;

  v_tem := power((1 + (COALESCE(p_tea, 43.92) / 100.0))::double precision, 1.0 / 12.0)::numeric - 1;

  IF v_tem = 0 THEN
    RETURN round(p_monto / p_plazo_meses, 2);
  END IF;

  v_factor := power((1 + v_tem)::double precision, p_plazo_meses::double precision)::numeric;
  RETURN round(p_monto * ((v_tem * v_factor) / (v_factor - 1)), 2);
END;
$$;

CREATE OR REPLACE FUNCTION public.bbva_solicitud_defaults()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY INVOKER
SET search_path = public
AS $$
BEGIN
  IF NEW.numero_expediente IS NULL OR NEW.numero_expediente = '' THEN
    NEW.numero_expediente :=
      'EXP-' || to_char(now(), 'YYYYMMDD') || '-' ||
      lpad(nextval('public.solicitudes_credito_expediente_seq')::text, 4, '0');
  END IF;

  NEW.tea_referencial := COALESCE(NEW.tea_referencial, 43.92);
  NEW.estado := COALESCE(NEW.estado, 'enviado');

  IF NEW.cuota_estimada IS NULL THEN
    NEW.cuota_estimada := public.bbva_calcular_cuota(
      NEW.monto_solicitado,
      COALESCE(NEW.plazo_meses, 12),
      NEW.tea_referencial
    );
  END IF;

  IF TG_OP = 'UPDATE' THEN
    NEW.updated_at := now();
  END IF;

  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_bbva_solicitud_defaults ON public.solicitudes_credito;
CREATE TRIGGER trg_bbva_solicitud_defaults
BEFORE INSERT OR UPDATE ON public.solicitudes_credito
FOR EACH ROW EXECUTE FUNCTION public.bbva_solicitud_defaults();

CREATE OR REPLACE FUNCTION public.bbva_actualizar_solicitud(
  p_solicitud_id UUID,
  p_estado TEXT,
  p_monto_aprobado NUMERIC DEFAULT NULL,
  p_condicion_adicional TEXT DEFAULT NULL,
  p_motivo_rechazo TEXT DEFAULT NULL
) RETURNS TABLE (
  id UUID,
  numero_expediente VARCHAR,
  estado VARCHAR,
  monto_solicitado NUMERIC,
  monto_aprobado NUMERIC,
  cliente_id UUID,
  asesor_id UUID
)
LANGUAGE plpgsql
SECURITY INVOKER
SET search_path = public
AS $$
DECLARE
  v_sol public.solicitudes_credito%ROWTYPE;
  v_monto NUMERIC;
  v_tea NUMERIC;
  v_plazo INTEGER;
  v_cod_credito VARCHAR(30);
  v_cuota NUMERIC;
  v_saldo NUMERIC;
  v_tem NUMERIC;
  v_interes NUMERIC;
  v_capital NUMERIC;
  i INTEGER;
BEGIN
  IF p_estado NOT IN ('enviado','recibido_comite','en_evaluacion','aprobado','condicionado','rechazado','desembolsado') THEN
    RAISE EXCEPTION 'Estado no permitido: %', p_estado;
  END IF;

  SELECT * INTO v_sol
  FROM public.solicitudes_credito sc
  WHERE sc.id = p_solicitud_id
  FOR UPDATE;

  IF NOT FOUND THEN
    RAISE EXCEPTION 'Solicitud no encontrada: %', p_solicitud_id;
  END IF;

  v_monto := COALESCE(p_monto_aprobado, v_sol.monto_aprobado, v_sol.monto_solicitado);
  v_tea := COALESCE(v_sol.tea_referencial, 43.92);
  v_plazo := COALESCE(v_sol.plazo_meses, 12);

  UPDATE public.solicitudes_credito sc
  SET estado = p_estado,
      monto_aprobado = CASE
        WHEN p_estado IN ('aprobado','desembolsado','condicionado') THEN v_monto
        ELSE sc.monto_aprobado
      END,
      condicion_adicional = CASE
        WHEN p_estado = 'condicionado' THEN COALESCE(p_condicion_adicional, 'Adjuntar sustento adicional de ingresos.')
        ELSE NULL
      END,
      motivo_rechazo = CASE
        WHEN p_estado = 'rechazado' THEN COALESCE(p_motivo_rechazo, 'No cumple politica crediticia de la demo.')
        ELSE NULL
      END,
      updated_at = now()
  WHERE sc.id = p_solicitud_id
  RETURNING * INTO v_sol;

  IF p_estado IN ('aprobado','desembolsado') THEN
    v_cod_credito := left('CC-' || replace(COALESCE(v_sol.numero_expediente, v_sol.id::text), '-', ''), 30);
    v_cuota := public.bbva_calcular_cuota(v_monto, v_plazo, v_tea);

    INSERT INTO public.cr_creditos(
      cod_cuenta_credito,
      cliente_id,
      producto,
      monto_desembolsado,
      saldo_capital,
      saldo_total,
      estado,
      fecha_desembolso,
      tea,
      cuotas_total,
      cuotas_pagadas
    ) VALUES (
      v_cod_credito,
      v_sol.cliente_id,
      'Credito empresarial BBVA',
      v_monto,
      v_monto,
      v_monto,
      CASE WHEN p_estado = 'desembolsado' THEN 'vigente' ELSE 'aprobado' END,
      CASE WHEN p_estado = 'desembolsado' THEN current_date ELSE NULL END,
      v_tea,
      v_plazo,
      0
    )
    ON CONFLICT (cod_cuenta_credito) DO UPDATE
    SET monto_desembolsado = EXCLUDED.monto_desembolsado,
        saldo_capital = EXCLUDED.saldo_capital,
        saldo_total = EXCLUDED.saldo_total,
        estado = EXCLUDED.estado,
        fecha_desembolso = EXCLUDED.fecha_desembolso,
        tea = EXCLUDED.tea,
        cuotas_total = EXCLUDED.cuotas_total,
        sync_at = now();

    v_saldo := v_monto;
    v_tem := power((1 + (v_tea / 100.0))::double precision, 1.0 / 12.0)::numeric - 1;

    FOR i IN 1..v_plazo LOOP
      v_interes := round(v_saldo * v_tem, 2);
      v_capital := round(v_cuota - v_interes, 2);
      IF i = v_plazo THEN
        v_capital := v_saldo;
      END IF;

      INSERT INTO public.cr_cronograma_pagos(
        cod_cuenta_credito,
        nro_cuota,
        fecha_vencimiento,
        monto_cuota,
        monto_capital,
        monto_interes,
        saldo,
        estado_cuota
      ) VALUES (
        v_cod_credito,
        i,
        (current_date + (i || ' month')::interval)::date,
        v_cuota,
        v_capital,
        v_interes,
        greatest(v_saldo - v_capital, 0),
        'pendiente'
      )
      ON CONFLICT (cod_cuenta_credito, nro_cuota) DO UPDATE
      SET fecha_vencimiento = EXCLUDED.fecha_vencimiento,
          monto_cuota = EXCLUDED.monto_cuota,
          monto_capital = EXCLUDED.monto_capital,
          monto_interes = EXCLUDED.monto_interes,
          saldo = EXCLUDED.saldo,
          estado_cuota = EXCLUDED.estado_cuota,
          sync_at = now();

      v_saldo := greatest(v_saldo - v_capital, 0);
    END LOOP;
  END IF;

  INSERT INTO public.sync_outbox(entidad, entidad_id, operacion, payload)
  VALUES ('solicitudes_credito', v_sol.id, 'update', to_jsonb(v_sol));

  RETURN QUERY
  SELECT v_sol.id, v_sol.numero_expediente, v_sol.estado, v_sol.monto_solicitado,
         v_sol.monto_aprobado, v_sol.cliente_id, v_sol.asesor_id;
END;
$$;

GRANT EXECUTE ON FUNCTION public.bbva_actualizar_solicitud(UUID, TEXT, NUMERIC, TEXT, TEXT) TO authenticated;
GRANT EXECUTE ON FUNCTION public.bbva_calcular_cuota(NUMERIC, INTEGER, NUMERIC) TO authenticated;

-- ============================================================================
-- RLS demo por identidad. Ajustar antes de produccion.
-- ============================================================================

ALTER TABLE public.clientes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.asesores ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.solicitudes_credito ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.cr_creditos ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.cr_cronograma_pagos ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sync_outbox ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS clientes_select_own_or_staff ON public.clientes;
CREATE POLICY clientes_select_own_or_staff
ON public.clientes
FOR SELECT
TO authenticated
USING (
  auth.uid() = auth_user_id
  OR EXISTS (
    SELECT 1 FROM public.asesores a
    WHERE a.id = auth.uid()
      AND a.activo = true
  )
);

DROP POLICY IF EXISTS asesores_select_staff ON public.asesores;
CREATE POLICY asesores_select_staff
ON public.asesores
FOR SELECT
TO authenticated
USING (
  id = auth.uid()
  OR EXISTS (
    SELECT 1 FROM public.clientes c
    WHERE c.auth_user_id = auth.uid()
  )
  OR EXISTS (
    SELECT 1 FROM public.asesores a
    WHERE a.id = auth.uid()
      AND a.activo = true
      AND a.perfil IN ('supervisor','administrador','super_operador')
  )
);

DROP POLICY IF EXISTS solicitudes_select_actor ON public.solicitudes_credito;
CREATE POLICY solicitudes_select_actor
ON public.solicitudes_credito
FOR SELECT
TO authenticated
USING (
  asesor_id = auth.uid()
  OR EXISTS (
    SELECT 1 FROM public.clientes c
    WHERE c.id = solicitudes_credito.cliente_id
      AND c.auth_user_id = auth.uid()
  )
  OR EXISTS (
    SELECT 1 FROM public.asesores a
    WHERE a.id = auth.uid()
      AND a.activo = true
      AND a.perfil IN ('supervisor','administrador','super_operador')
  )
);

DROP POLICY IF EXISTS solicitudes_insert_actor ON public.solicitudes_credito;
CREATE POLICY solicitudes_insert_actor
ON public.solicitudes_credito
FOR INSERT
TO authenticated
WITH CHECK (
  asesor_id = auth.uid()
  OR EXISTS (
    SELECT 1 FROM public.clientes c
    WHERE c.id = solicitudes_credito.cliente_id
      AND c.auth_user_id = auth.uid()
      AND solicitudes_credito.canal = 'cliente'
  )
);

DROP POLICY IF EXISTS solicitudes_update_staff ON public.solicitudes_credito;
CREATE POLICY solicitudes_update_staff
ON public.solicitudes_credito
FOR UPDATE
TO authenticated
USING (
  asesor_id = auth.uid()
  OR EXISTS (
    SELECT 1 FROM public.asesores a
    WHERE a.id = auth.uid()
      AND a.activo = true
      AND a.perfil IN ('supervisor','administrador','super_operador')
  )
)
WITH CHECK (
  asesor_id = auth.uid()
  OR EXISTS (
    SELECT 1 FROM public.asesores a
    WHERE a.id = auth.uid()
      AND a.activo = true
      AND a.perfil IN ('supervisor','administrador','super_operador')
  )
);

DROP POLICY IF EXISTS cr_creditos_select_actor ON public.cr_creditos;
CREATE POLICY cr_creditos_select_actor
ON public.cr_creditos
FOR SELECT
TO authenticated
USING (
  EXISTS (
    SELECT 1 FROM public.clientes c
    WHERE c.id = cr_creditos.cliente_id
      AND c.auth_user_id = auth.uid()
  )
  OR EXISTS (
    SELECT 1 FROM public.asesores a
    WHERE a.id = auth.uid()
      AND a.activo = true
  )
);

DROP POLICY IF EXISTS cr_creditos_insert_staff ON public.cr_creditos;
CREATE POLICY cr_creditos_insert_staff
ON public.cr_creditos
FOR INSERT
TO authenticated
WITH CHECK (
  EXISTS (
    SELECT 1 FROM public.asesores a
    WHERE a.id = auth.uid()
      AND a.activo = true
  )
);

DROP POLICY IF EXISTS cr_cronograma_select_actor ON public.cr_cronograma_pagos;
CREATE POLICY cr_cronograma_select_actor
ON public.cr_cronograma_pagos
FOR SELECT
TO authenticated
USING (
  EXISTS (
    SELECT 1
    FROM public.cr_creditos cr
    JOIN public.clientes c ON c.id = cr.cliente_id
    WHERE cr.cod_cuenta_credito = cr_cronograma_pagos.cod_cuenta_credito
      AND (
        c.auth_user_id = auth.uid()
        OR EXISTS (
          SELECT 1 FROM public.asesores a
          WHERE a.id = auth.uid()
            AND a.activo = true
        )
      )
  )
);

DROP POLICY IF EXISTS cr_cronograma_insert_staff ON public.cr_cronograma_pagos;
CREATE POLICY cr_cronograma_insert_staff
ON public.cr_cronograma_pagos
FOR INSERT
TO authenticated
WITH CHECK (
  EXISTS (
    SELECT 1 FROM public.asesores a
    WHERE a.id = auth.uid()
      AND a.activo = true
  )
);

DROP POLICY IF EXISTS sync_outbox_insert_authenticated ON public.sync_outbox;
CREATE POLICY sync_outbox_insert_authenticated
ON public.sync_outbox
FOR INSERT
TO authenticated
WITH CHECK (
  entidad IN ('solicitudes_credito','cr_creditos','operaciones_cliente')
);

-- ============================================================================
-- Usuarios demo. Clave propuesta para los 3 usuarios: DemoBBVA2026!
-- Requiere permisos elevados sobre auth.users.
-- ============================================================================

DO $$
DECLARE
  v_cliente_auth UUID := '11111111-1111-4111-a111-111111111111';
  v_asesor_auth UUID := '1ad6c5af-d359-43a0-b317-8a4069fc412e';
  v_admin_auth UUID := 'aaaaaaaa-aaaa-4aaa-aaaa-aaaaaaaaaaaa';
  v_agencia UUID;
  v_password TEXT := crypt('DemoBBVA2026!', gen_salt('bf'));
BEGIN
  IF to_regclass('auth.users') IS NOT NULL THEN
    INSERT INTO auth.users (
      instance_id, id, aud, role, email, encrypted_password,
      email_confirmed_at, created_at, updated_at,
      raw_app_meta_data, raw_user_meta_data,
      is_super_admin, confirmation_token, email_change, phone_change
    ) VALUES
      ('00000000-0000-0000-0000-000000000000', v_cliente_auth, 'authenticated', 'authenticated',
       'client01@clientes.pe', v_password, now(), now(), now(),
       '{"provider":"email","providers":["email"]}'::jsonb, '{"role":"cliente"}'::jsonb,
       false, '', '', ''),
      ('00000000-0000-0000-0000-000000000000', v_asesor_auth, 'authenticated', 'authenticated',
       'asesor02@asesores.pe', v_password, now(), now(), now(),
       '{"provider":"email","providers":["email"]}'::jsonb, '{"role":"asesor"}'::jsonb,
       false, '', '', ''),
      ('00000000-0000-0000-0000-000000000000', v_admin_auth, 'authenticated', 'authenticated',
       'admin.demo@bbva.pe', v_password, now(), now(), now(),
       '{"provider":"email","providers":["email"]}'::jsonb, '{"role":"administrador"}'::jsonb,
       false, '', '', '')
    ON CONFLICT (id) DO UPDATE
    SET email = EXCLUDED.email,
        encrypted_password = EXCLUDED.encrypted_password,
        email_confirmed_at = now(),
        updated_at = now(),
        raw_app_meta_data = EXCLUDED.raw_app_meta_data,
        raw_user_meta_data = EXCLUDED.raw_user_meta_data;
  END IF;

  SELECT id INTO v_agencia
  FROM public.agencias
  ORDER BY created_at
  LIMIT 1;

  UPDATE public.clientes
  SET email = 'client01@clientes.pe',
      auth_user_id = v_cliente_auth,
      telefono = COALESCE(telefono, '999000111'),
      tipo_negocio = COALESCE(tipo_negocio, 'Bodega'),
      nombre_negocio = COALESCE(nombre_negocio, 'Bodega Test 1'),
      ingresos_estimados = COALESCE(ingresos_estimados, 4500),
      updated_at = now()
  WHERE id = '5636fc6e-93b0-4cf3-b30a-4188c0a6cd94'
     OR cod_cliente = 'T0001'
     OR numero_documento = '88880001';

  UPDATE public.asesores
  SET nombres = 'Asesor',
      apellidos = 'Prueba',
      perfil = 'operador',
      activo = true
  WHERE id = v_asesor_auth;

  INSERT INTO public.asesores (
    id, cod_asesor, codigo_empleado, nombres, apellidos,
    agencia_id, perfil, password_hash, activo
  ) VALUES (
    v_admin_auth, 'ADM-DEMO', 'ADMDEMO', 'Admin', 'Demo',
    v_agencia, 'administrador', v_password, true
  )
  ON CONFLICT (id) DO UPDATE
  SET codigo_empleado = EXCLUDED.codigo_empleado,
      nombres = EXCLUDED.nombres,
      apellidos = EXCLUDED.apellidos,
      agencia_id = EXCLUDED.agencia_id,
      perfil = EXCLUDED.perfil,
      activo = true;
END $$;
