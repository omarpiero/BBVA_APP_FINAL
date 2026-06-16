# 🗄️ Database Changes — Sprint 1: Flujo PLIN + Pago en Línea

> **Sprint:** 1  
> **Estado:** 🔲 TODO  
> **Última actualización:** Sesión 1 - 19/05/2026

---

## 📋 Resumen de Cambios

Este documento contiene todas las queries SQL necesarias para soportar el flujo PLIN → Pagar en Línea → Confirmación de Pago.

---

## 🆕 Tabla: `comercios`

Catálogo de comercios disponibles para pagos en línea.

```sql
CREATE TABLE public.comercios (
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    nombre text NOT NULL,
    categoria text NOT NULL DEFAULT 'general',
    logo_url text,
    activo boolean NOT NULL DEFAULT true,
    created_at timestamp with time zone DEFAULT now(),
    CONSTRAINT comercios_pkey PRIMARY KEY (id)
);

-- Datos iniciales de ejemplo
INSERT INTO public.comercios (nombre, categoria) VALUES
    ('Adidas', 'ropa'),
    ('Nike', 'ropa'),
    ('Samsung', 'tecnologia'),
    ('Apple', 'tecnologia'),
    ('Rappi', 'delivery'),
    ('PedidosYa', 'delivery'),
    ('Netflix', 'entretenimiento'),
    ('Spotify', 'entretenimiento'),
    ('Falabella', 'tienda'),
    ('Ripley', 'tienda');
```

---

## 🆕 Tabla: `pagos_en_linea`

Registro de pagos en línea realizados por los usuarios.

```sql
CREATE TABLE public.pagos_en_linea (
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL,
    comercio_id uuid NOT NULL,
    comercio_nombre text NOT NULL,
    cuenta_origen_id uuid NOT NULL,
    monto numeric NOT NULL,
    comision numeric NOT NULL DEFAULT 0,
    medio_pago text NOT NULL DEFAULT 'Plin',
    estado text NOT NULL DEFAULT 'completado' CHECK (estado = ANY (ARRAY['completado', 'pendiente', 'fallido'])),
    numero_operacion text NOT NULL,
    fecha timestamp with time zone DEFAULT now(),
    CONSTRAINT pagos_en_linea_pkey PRIMARY KEY (id),
    CONSTRAINT pagos_en_linea_user_id_fkey FOREIGN KEY (user_id) REFERENCES auth.users(id),
    CONSTRAINT pagos_en_linea_comercio_id_fkey FOREIGN KEY (comercio_id) REFERENCES public.comercios(id),
    CONSTRAINT pagos_en_linea_cuenta_origen_id_fkey FOREIGN KEY (cuenta_origen_id) REFERENCES public.cuentas(id)
);
```

---

## 🆕 Tabla: `plin_transacciones`

Historial de todas las transacciones PLIN (envíos, recepciones, pagos).

```sql
CREATE TABLE public.plin_transacciones (
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL,
    tipo text NOT NULL CHECK (tipo = ANY (ARRAY['envio', 'recepcion', 'pago_linea'])),
    destinatario text,
    comercio text,
    monto numeric NOT NULL,
    estado text NOT NULL DEFAULT 'completado' CHECK (estado = ANY (ARRAY['completado', 'pendiente', 'fallido'])),
    medio_pago text NOT NULL DEFAULT 'Plin',
    cuenta_origen_id uuid,
    numero_operacion text NOT NULL,
    fecha timestamp with time zone DEFAULT now(),
    CONSTRAINT plin_transacciones_pkey PRIMARY KEY (id),
    CONSTRAINT plin_transacciones_user_id_fkey FOREIGN KEY (user_id) REFERENCES auth.users(id),
    CONSTRAINT plin_transacciones_cuenta_origen_id_fkey FOREIGN KEY (cuenta_origen_id) REFERENCES public.cuentas(id)
);
```

---

## 🔒 Row Level Security (RLS) Policies

### Para `comercios` (lectura pública, escritura admin)

```sql
-- Habilitar RLS
ALTER TABLE public.comercios ENABLE ROW LEVEL SECURITY;

-- Permitir lectura a usuarios autenticados
CREATE POLICY "Usuarios autenticados pueden ver comercios"
    ON public.comercios
    FOR SELECT
    TO authenticated
    USING (true);
```

### Para `pagos_en_linea` (solo datos propios)

```sql
-- Habilitar RLS
ALTER TABLE public.pagos_en_linea ENABLE ROW LEVEL SECURITY;

-- Lectura: solo sus propios pagos
CREATE POLICY "Usuarios ven sus propios pagos en linea"
    ON public.pagos_en_linea
    FOR SELECT
    TO authenticated
    USING (auth.uid() = user_id);

-- Inserción: solo puede crear pagos para sí mismo
CREATE POLICY "Usuarios crean sus propios pagos en linea"
    ON public.pagos_en_linea
    FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = user_id);
```

### Para `plin_transacciones` (solo datos propios)

```sql
-- Habilitar RLS
ALTER TABLE public.plin_transacciones ENABLE ROW LEVEL SECURITY;

-- Lectura: solo sus propias transacciones
CREATE POLICY "Usuarios ven sus propias transacciones plin"
    ON public.plin_transacciones
    FOR SELECT
    TO authenticated
    USING (auth.uid() = user_id);

-- Inserción: solo puede crear transacciones para sí mismo
CREATE POLICY "Usuarios crean sus propias transacciones plin"
    ON public.plin_transacciones
    FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = user_id);
```

---

## 🔄 Función: Procesar Pago en Línea

Función SQL que ejecuta el pago de forma atómica (descuenta saldo + registra pago + registra transacción PLIN):

```sql
-- Función para procesar un pago en línea
CREATE OR REPLACE FUNCTION public.procesar_pago_en_linea(
    p_user_id uuid,
    p_comercio_id uuid,
    p_comercio_nombre text,
    p_cuenta_origen_id uuid,
    p_monto numeric,
    p_numero_operacion text
)
RETURNS uuid
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_saldo_actual numeric;
    v_pago_id uuid;
BEGIN
    -- 1. Verificar saldo suficiente
    SELECT saldo INTO v_saldo_actual
    FROM public.cuentas
    WHERE id = p_cuenta_origen_id AND user_id = p_user_id;

    IF v_saldo_actual IS NULL THEN
        RAISE EXCEPTION 'Cuenta no encontrada';
    END IF;

    IF v_saldo_actual < p_monto THEN
        RAISE EXCEPTION 'Saldo insuficiente. Saldo actual: %', v_saldo_actual;
    END IF;

    -- 2. Descontar saldo
    UPDATE public.cuentas
    SET saldo = saldo - p_monto
    WHERE id = p_cuenta_origen_id AND user_id = p_user_id;

    -- 3. Registrar pago en línea
    INSERT INTO public.pagos_en_linea (
        user_id, comercio_id, comercio_nombre, cuenta_origen_id,
        monto, numero_operacion, estado
    ) VALUES (
        p_user_id, p_comercio_id, p_comercio_nombre, p_cuenta_origen_id,
        p_monto, p_numero_operacion, 'completado'
    ) RETURNING id INTO v_pago_id;

    -- 4. Registrar transacción general (débito)
    INSERT INTO public.transacciones (
        user_id, cuenta_id, tipo, descripcion, monto
    ) VALUES (
        p_user_id, p_cuenta_origen_id, 'debito',
        'Pago en línea - ' || p_comercio_nombre, p_monto
    );

    -- 5. Registrar en historial PLIN
    INSERT INTO public.plin_transacciones (
        user_id, tipo, comercio, monto, estado,
        cuenta_origen_id, numero_operacion
    ) VALUES (
        p_user_id, 'pago_linea', p_comercio_nombre, p_monto,
        'completado', p_cuenta_origen_id, p_numero_operacion
    );

    RETURN v_pago_id;
END;
$$;
```

---

## 📊 Diagrama de Relaciones (Sprint 1)

```
auth.users
    │
    ├── 1:N ── cuentas
    │              │
    │              ├── 1:N ── transacciones
    │              ├── 1:N ── pagos_en_linea ── N:1 ── comercios
    │              └── 1:N ── plin_transacciones
    │
    ├── 1:N ── perfiles
    ├── 1:N ── tarjetas
    ├── 1:N ── pagos (servicios)
    └── 1:N ── solicitudes_prestamo
```

---

## 📝 Notas

- La función `procesar_pago_en_linea` usa `SECURITY DEFINER` para que pueda actualizar saldos aunque el usuario tenga RLS restringido.
- El `numero_operacion` se genera en el cliente (ViewModel) y se envía al servidor.
- La comisión es 0 (gratis) en esta versión inicial.

---

## 🧪 Datos de Prueba para Cliente 1

Para poder probar toda la funcionalidad, ejecuta la siguiente query en el editor SQL de tu panel de Supabase. Ésta asocia el perfil de usuario, cuentas de prueba y saldos al correo de tu cliente de pruebas (por defecto `cliente1@prueba.test` o puedes cambiarlo en la primera línea).

```sql
-- Buscar el UUID del usuario cliente de pruebas por su email en Supabase Auth
DO $$
DECLARE
    v_user_id uuid;
BEGIN
    -- Busca el ID del usuario en auth.users
    SELECT id INTO v_user_id FROM auth.users WHERE email = 'cliente1@prueba.test' LIMIT 1;
    
    IF v_user_id IS NOT NULL THEN
        -- 1. Insertar perfil si no existe, o actualizar el nombre
        INSERT INTO public.perfiles (user_id, nombre, email, telefono)
        VALUES (v_user_id, 'Cliente Uno', 'cliente1@prueba.test', '987654321')
        ON CONFLICT (user_id) DO UPDATE SET nombre = EXCLUDED.nombre;
        
        -- 2. Insertar cuenta corriente (independencia) con saldo de prueba
        INSERT INTO public.cuentas (user_id, tipo, numero_cuenta, saldo, moneda)
        VALUES (v_user_id, 'corriente', '191-12345678-0-2339', 9999.99, 'PEN')
        ON CONFLICT DO NOTHING;
        
        -- 3. Insertar cuenta de ahorro si no existe
        INSERT INTO public.cuentas_ahorro (user_id, saldo, meta_ahorro, tasa_interes)
        VALUES (v_user_id, 5000.00, 10000.00, 3.50)
        ON CONFLICT DO NOTHING;
        
        -- 4. Opcional: Registrar alguna transacción inicial
        INSERT INTO public.transacciones (user_id, cuenta_id, tipo, descripcion, monto)
        SELECT v_user_id, id, 'credito', 'Abono de Prueba BBVA', 500.00
        FROM public.cuentas
        WHERE user_id = v_user_id AND tipo = 'corriente'
        LIMIT 1;

        RAISE NOTICE 'Datos de prueba insertados/actualizados correctamente para el usuario %', v_user_id;
    ELSE
        RAISE EXCEPTION 'No se encontró ningún usuario con el correo "cliente1@prueba.test" en auth.users. Regístralo o inicia sesión primero en la app para crearlo.';
    END IF;
END $$;
```

---

*Documento SDD — Spec Driven Development*
