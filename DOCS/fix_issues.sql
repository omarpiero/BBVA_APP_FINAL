-- 1. Añadir bloqueado_hasta a usuarios_cliente
ALTER TABLE public.usuarios_cliente ADD COLUMN IF NOT EXISTS bloqueado_hasta TIMESTAMPTZ;

-- 2. Modificar bbva_obtener_estado_bloqueo
CREATE OR REPLACE FUNCTION public.bbva_obtener_estado_bloqueo(p_username text, p_tipo_usuario text)
 RETURNS jsonb
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
    v_bloqueado boolean := false;
    v_bloqueado_hasta timestamptz := null;
    v_intentos integer := 0;
BEGIN
    IF p_tipo_usuario = 'asesor' THEN
        SELECT intentos_fallidos, bloqueado_hasta
        INTO v_intentos, v_bloqueado_hasta
        FROM asesores
        WHERE codigo_empleado = p_username OR email = p_username;
        
        IF v_bloqueado_hasta IS NOT NULL AND v_bloqueado_hasta > now() THEN
            v_bloqueado := true;
        END IF;
    ELSIF p_tipo_usuario = 'cliente' THEN
        SELECT uc.intentos_fallidos, uc.bloqueado_hasta
        INTO v_intentos, v_bloqueado_hasta
        FROM usuarios_cliente uc
        LEFT JOIN clientes c ON c.id = uc.cliente_id
        WHERE uc.username = p_username OR c.email = p_username;
        
        IF v_bloqueado_hasta IS NOT NULL AND v_bloqueado_hasta > now() THEN
            v_bloqueado := true;
        END IF;
    ELSE
        RAISE EXCEPTION 'Tipo de usuario invalido: %', p_tipo_usuario;
    END IF;

    RETURN jsonb_build_object(
        'intentos_fallidos', COALESCE(v_intentos, 0),
        'bloqueado', v_bloqueado,
        'bloqueado_hasta', v_bloqueado_hasta
    );
END;
$function$;

-- 3. Modificar bbva_registrar_intento_fallido
CREATE OR REPLACE FUNCTION public.bbva_registrar_intento_fallido(p_username text, p_tipo_usuario text)
 RETURNS jsonb
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
    v_intentos integer;
    v_bloqueado boolean := false;
    v_bloqueado_hasta timestamptz := null;
BEGIN
    IF p_tipo_usuario = 'asesor' THEN
        UPDATE asesores
        SET intentos_fallidos = intentos_fallidos + 1,
            bloqueado_hasta = CASE WHEN intentos_fallidos + 1 >= 5 THEN now() + interval '5 minutes' ELSE bloqueado_hasta END
        WHERE codigo_empleado = p_username OR email = p_username
        RETURNING intentos_fallidos, bloqueado_hasta INTO v_intentos, v_bloqueado_hasta;
        
        IF v_bloqueado_hasta IS NOT NULL AND v_bloqueado_hasta > now() THEN
            v_bloqueado := true;
        END IF;
    ELSIF p_tipo_usuario = 'cliente' THEN
        UPDATE usuarios_cliente
        SET intentos_fallidos = intentos_fallidos + 1,
            bloqueado_hasta = CASE WHEN intentos_fallidos + 1 >= 5 THEN now() + interval '5 minutes' ELSE bloqueado_hasta END
        WHERE id = (SELECT uc.id FROM usuarios_cliente uc LEFT JOIN clientes c ON c.id = uc.cliente_id WHERE uc.username = p_username OR c.email = p_username LIMIT 1)
        RETURNING intentos_fallidos, bloqueado_hasta INTO v_intentos, v_bloqueado_hasta;
        
        IF v_bloqueado_hasta IS NOT NULL AND v_bloqueado_hasta > now() THEN
            v_bloqueado := true;
            UPDATE usuarios_cliente SET bloqueado = true WHERE id = (SELECT uc.id FROM usuarios_cliente uc LEFT JOIN clientes c ON c.id = uc.cliente_id WHERE uc.username = p_username OR c.email = p_username LIMIT 1);
        END IF;
    ELSE
        RAISE EXCEPTION 'Tipo de usuario invalido: %', p_tipo_usuario;
    END IF;

    RETURN jsonb_build_object(
        'intentos_fallidos', COALESCE(v_intentos, 0),
        'bloqueado', v_bloqueado,
        'bloqueado_hasta', v_bloqueado_hasta
    );
END;
$function$;

-- 4. Modificar bbva_resetear_intentos_fallidos
CREATE OR REPLACE FUNCTION public.bbva_resetear_intentos_fallidos(p_username text, p_tipo_usuario text)
 RETURNS void
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
    IF p_tipo_usuario = 'asesor' THEN
        UPDATE asesores
        SET intentos_fallidos = 0,
            bloqueado_hasta = null
        WHERE codigo_empleado = p_username OR email = p_username;
    ELSIF p_tipo_usuario = 'cliente' THEN
        UPDATE usuarios_cliente
        SET intentos_fallidos = 0,
            bloqueado_hasta = null,
            bloqueado = false
        WHERE id = (SELECT uc.id FROM usuarios_cliente uc LEFT JOIN clientes c ON c.id = uc.cliente_id WHERE uc.username = p_username OR c.email = p_username LIMIT 1);
    ELSE
        RAISE EXCEPTION 'Tipo de usuario invalido: %', p_tipo_usuario;
    END IF;
END;
$function$;

-- 5. Modificar bbva_solicitud_defaults para limite de 24 horas
CREATE OR REPLACE FUNCTION public.bbva_solicitud_defaults()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY INVOKER
SET search_path = public
AS $function$
DECLARE
  v_count INTEGER;
BEGIN
  IF TG_OP = 'INSERT' THEN
    SELECT COUNT(*) INTO v_count 
    FROM solicitudes_credito 
    WHERE cliente_id = NEW.cliente_id 
    AND created_at >= (now() - interval '24 hours');
    
    IF v_count > 0 THEN
      RAISE EXCEPTION 'El cliente ya cuenta con una solicitud registrada en las ultimas 24 horas.';
    END IF;
  END IF;

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
$function$;

-- 6. Modificar bbva_procesar_desembolso para que sea SECURITY DEFINER y haga el credito
CREATE OR REPLACE FUNCTION public.bbva_procesar_desembolso()
 RETURNS trigger
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
    v_cuenta_ahorro_id UUID;
    v_cod_credito VARCHAR(30);
    v_cuota DECIMAL(10,2);
    v_fecha_vencimiento DATE;
BEGIN
    IF NEW.estado = 'desembolsado' AND (OLD.estado IS NULL OR OLD.estado <> 'desembolsado') THEN
        SELECT id INTO v_cuenta_ahorro_id FROM cr_cuentas_ahorro WHERE cliente_id = NEW.cliente_id LIMIT 1;
        
        IF v_cuenta_ahorro_id IS NULL THEN
            INSERT INTO cr_cuentas_ahorro (cliente_id, cod_cuenta_ahorro, tipo_cuenta, moneda, saldo_capital, saldo_interes, tea, estado)
            VALUES (
                NEW.cliente_id,
                'AH-' || substring(NEW.id::text, 1, 8),
                'AHORRO SOL BBVA',
                'PEN',
                NEW.monto_solicitado, 
                0.0,
                0.5,
                'activa'
            ) RETURNING id INTO v_cuenta_ahorro_id;
        ELSE
            UPDATE cr_cuentas_ahorro 
            SET saldo_capital = saldo_capital + NEW.monto_solicitado 
            WHERE id = v_cuenta_ahorro_id;
        END IF;

        v_cod_credito := left('CR-' || replace(COALESCE(NEW.numero_expediente, NEW.id::text), '-', ''), 30);
        INSERT INTO cr_creditos (cod_cuenta_credito, cliente_id, producto, monto_desembolsado, saldo_capital, saldo_total, dias_mora, calificacion_interna, estado, fecha_desembolso, tea, cuotas_total, cuotas_pagadas)
        VALUES (
            v_cod_credito,
            NEW.cliente_id,
            'PRESTAMO NEGOCIO BBVA',
            NEW.monto_solicitado,
            NEW.monto_solicitado,
            NEW.monto_solicitado * 1.1, 
            0,
            'NORMAL',
            'vigente',
            CURRENT_DATE,
            NEW.tea_referencial,
            NEW.plazo_meses,
            0
        ) ON CONFLICT(cod_cuenta_credito) DO NOTHING;

        v_cuota := (NEW.monto_solicitado * 1.1) / NEW.plazo_meses;
        FOR i IN 1..NEW.plazo_meses LOOP
            v_fecha_vencimiento := CURRENT_DATE + (i * INTERVAL '1 month');
            INSERT INTO cr_cronograma_pagos (cod_cuenta_credito, nro_cuota, fecha_vencimiento, monto_cuota, monto_capital, monto_interes, saldo, estado_cuota)
            VALUES (
                v_cod_credito,
                i,
                v_fecha_vencimiento,
                v_cuota,
                v_cuota * 0.9,
                v_cuota * 0.1,
                (NEW.monto_solicitado * 1.1) - (i * v_cuota),
                'pendiente'
            ) ON CONFLICT(cod_cuenta_credito, nro_cuota) DO NOTHING;
        END LOOP;
    END IF;
    RETURN NEW;
END;
$function$;

-- 7. Modificar bbva_actualizar_solicitud para NO insertar el credito de nuevo (ya lo hace el trigger)
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
AS $function$
DECLARE
  v_sol public.solicitudes_credito%ROWTYPE;
  v_monto NUMERIC;
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

  INSERT INTO public.sync_outbox(entidad, entidad_id, operacion, payload)
  VALUES ('solicitudes_credito', v_sol.id, 'update', to_jsonb(v_sol));

  RETURN QUERY
  SELECT v_sol.id, v_sol.numero_expediente, v_sol.estado, v_sol.monto_solicitado,
         v_sol.monto_aprobado, v_sol.cliente_id, v_sol.asesor_id;
END;
$function$;
