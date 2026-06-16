-- WARNING: This schema is for context only and is not meant to be run.
-- Table order and constraints may not be valid for execution.

CREATE TABLE public.cuentas (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL,
  tipo text NOT NULL CHECK (tipo = ANY (ARRAY['corriente'::text, 'ahorro'::text])),
  numero_cuenta text NOT NULL,
  saldo numeric NOT NULL DEFAULT 0,
  moneda text NOT NULL DEFAULT 'PEN'::text,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT cuentas_pkey PRIMARY KEY (id),
  CONSTRAINT cuentas_user_id_fkey FOREIGN KEY (user_id) REFERENCES auth.users(id)
);
CREATE TABLE public.cuentas_ahorro (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL,
  saldo numeric NOT NULL DEFAULT 0,
  meta_ahorro numeric NOT NULL DEFAULT 10000,
  tasa_interes numeric NOT NULL DEFAULT 3.5,
  fecha_apertura date DEFAULT CURRENT_DATE,
  CONSTRAINT cuentas_ahorro_pkey PRIMARY KEY (id),
  CONSTRAINT cuentas_ahorro_user_id_fkey FOREIGN KEY (user_id) REFERENCES auth.users(id)
);
CREATE TABLE public.pagos (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL,
  servicio text NOT NULL CHECK (servicio = ANY (ARRAY['agua'::text, 'luz'::text, 'cable'::text, 'telefono'::text, 'gas'::text])),
  numero_contrato text NOT NULL,
  monto numeric NOT NULL,
  estado text NOT NULL DEFAULT 'completado'::text,
  fecha timestamp with time zone DEFAULT now(),
  CONSTRAINT pagos_pkey PRIMARY KEY (id),
  CONSTRAINT pagos_user_id_fkey FOREIGN KEY (user_id) REFERENCES auth.users(id)
);
CREATE TABLE public.perfiles (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL UNIQUE,
  nombre text,
  telefono text,
  email text,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT perfiles_pkey PRIMARY KEY (id),
  CONSTRAINT perfiles_user_id_fkey FOREIGN KEY (user_id) REFERENCES auth.users(id)
);
CREATE TABLE public.solicitudes_prestamo (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL,
  monto numeric NOT NULL,
  plazo_meses integer NOT NULL,
  tasa_anual numeric NOT NULL,
  cuota_mensual numeric NOT NULL,
  proposito text,
  estado text NOT NULL DEFAULT 'pendiente'::text,
  created_at timestamp with time zone DEFAULT now(),
  numero_enmascarado text,
  capital_total numeric DEFAULT 0,
  capital_pendiente numeric DEFAULT 0,
  cuota_numero integer DEFAULT 1,
  cuotas_total integer DEFAULT 1,
  fecha_limite date,
  capital_cuota numeric DEFAULT 0,
  intereses_cuota numeric DEFAULT 0,
  seguros_cuota numeric DEFAULT 0,
  tipo text DEFAULT 'personal'::text,
  CONSTRAINT solicitudes_prestamo_pkey PRIMARY KEY (id),
  CONSTRAINT solicitudes_prestamo_user_id_fkey FOREIGN KEY (user_id) REFERENCES auth.users(id)
);
CREATE TABLE public.tarjetas (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL,
  tipo text NOT NULL CHECK (tipo = ANY (ARRAY['debito'::text, 'credito'::text])),
  numero_enmascarado text NOT NULL,
  estado text NOT NULL DEFAULT 'activa'::text CHECK (estado = ANY (ARRAY['activa'::text, 'bloqueada'::text, 'apagada'::text])),
  saldo_disponible numeric NOT NULL DEFAULT 0,
  cuenta_asociada text,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT tarjetas_pkey PRIMARY KEY (id),
  CONSTRAINT tarjetas_user_id_fkey FOREIGN KEY (user_id) REFERENCES auth.users(id)
);
CREATE TABLE public.transacciones (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL,
  cuenta_id uuid,
  tipo text NOT NULL CHECK (tipo = ANY (ARRAY['debito'::text, 'credito'::text])),
  descripcion text NOT NULL,
  monto numeric NOT NULL,
  fecha timestamp with time zone DEFAULT now(),
  CONSTRAINT transacciones_pkey PRIMARY KEY (id),
  CONSTRAINT transacciones_user_id_fkey FOREIGN KEY (user_id) REFERENCES auth.users(id),
  CONSTRAINT transacciones_cuenta_id_fkey FOREIGN KEY (cuenta_id) REFERENCES public.cuentas(id)
);