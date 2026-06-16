# 08 — DATABASE SCHEMA

| Campo               | Valor                                                        |
|---------------------|--------------------------------------------------------------|
| **ID**              | SDD-08                                                       |
| **Sprint**          | Sprint 0 — Fundación                                         |
| **Estado**          | 📋 Especificado                                               |
| **Última revisión** | 2026-05-26                                                   |
| **Épica**           | E-01 Fundación & Documentación                               |
| **Prioridad**       | 🔴 Alta                                                      |

---

## 1. Objetivo

Definir el **schema completo** de la base de datos en Supabase (PostgreSQL) para el proyecto BBVA Fuerza de Ventas. Incluye tablas, relaciones, índices, UUIDs, RLS policies, campos de auditoría y control de sincronización offline.

---

## 2. Diagrama Entidad-Relación

```
┌─────────────┐     ┌──────────────────┐     ┌──────────────────────┐
│  auth.users  │────►│ perfiles_clientes │────►│  features_scoring    │
│  (Supabase)  │     │                  │     │                      │
└──────┬───────┘     └────────┬─────────┘     └──────────┬───────────┘
       │                      │                          │
       │              ┌───────┴─────────┐                │
       │              │                 │                │
       │         ┌────▼────┐     ┌──────▼─────────┐  ┌──▼──────────────────┐
       │         │ cuentas  │     │ movimientos_   │  │scores_transaccionales│
       │         │          │     │ mensuales      │  │                      │
       │         └────┬─────┘     └────────────────┘  └──────────┬───────────┘
       │              │                                          │
       │         ┌────▼──────────┐                               │
       │         │ transacciones │                               │
       │         │               │                               │
       │         └───────────────┘                               │
       │                                                         │
       │              ┌────────────────────────────────┐         │
       └──────────────►│       fichas_campo              │◄────────┘
                       │                                │
                       └───────────────┬────────────────┘
                                       │
                                ┌──────▼──────────────┐
                                │creditos_preaprobados │
                                │                      │
                                └──────────────────────┘

┌─────────────┐     ┌──────────────────┐
│  agencias    │────►│ asesores_negocio  │
│              │     │                  │
└──────────────┘     └──────────────────┘

┌──────────────┐
│ sync_queue    │  (Cola de sincronización offline)
│              │
└──────────────┘
```

---

## 3. Tablas del Sistema

### 3.1. `agencias` — Agencias del Banco

```sql
CREATE TABLE IF NOT EXISTS public.agencias (
  id              SERIAL PRIMARY KEY,
  codigo          TEXT NOT NULL UNIQUE,        -- 'AG-001' ... 'AG-030'
  nombre          TEXT NOT NULL,
  region          TEXT NOT NULL,               -- Centro, Sur, Norte, Lima, Oriente
  departamento    TEXT NOT NULL,
  provincia       TEXT NOT NULL,
  distrito        TEXT NOT NULL,
  direccion       TEXT,
  jefe_agencia    TEXT,
  activa          BOOLEAN DEFAULT TRUE,
  created_at      TIMESTAMPTZ DEFAULT now()
);
```

| Campo          | Tipo    | Restricción        | Descripción                   |
|----------------|---------|--------------------|-------------------------------|
| id             | SERIAL  | PK                 | ID autoincremental            |
| codigo         | TEXT    | UNIQUE, NOT NULL   | Código único AG-XXX           |
| nombre         | TEXT    | NOT NULL           | Nombre descriptivo            |
| region         | TEXT    | NOT NULL           | Región geográfica             |
| departamento   | TEXT    | NOT NULL           | Departamento                  |
| provincia      | TEXT    | NOT NULL           | Provincia                     |
| distrito       | TEXT    | NOT NULL           | Distrito                      |
| direccion      | TEXT    | —                  | Dirección física              |
| jefe_agencia   | TEXT    | —                  | Nombre del jefe               |
| activa         | BOOLEAN | DEFAULT TRUE       | Estado activa/inactiva        |
| created_at     | TIMESTAMPTZ | DEFAULT now()  | Fecha de creación             |

---

### 3.2. `asesores_negocio` — Asesores de Crédito

```sql
CREATE TABLE IF NOT EXISTS public.asesores_negocio (
  id              SERIAL PRIMARY KEY,
  codigo          TEXT NOT NULL UNIQUE,
  id_agencia      INT NOT NULL REFERENCES public.agencias(id),
  nombres         TEXT NOT NULL,
  apellidos       TEXT NOT NULL,
  dni             TEXT,
  email           TEXT,
  telefono        TEXT,
  nivel           TEXT NOT NULL
                    CHECK (nivel IN ('Junior I','Junior II','Senior I','Senior II')),
  cartera_clientes_promedio INT NOT NULL,
  meta_creditos_mes         INT NOT NULL,
  meta_monto_mes            NUMERIC(14,2) NOT NULL,
  zona_asignada             TEXT,
  activo          BOOLEAN DEFAULT TRUE,
  fecha_ingreso   DATE DEFAULT CURRENT_DATE,
  created_at      TIMESTAMPTZ DEFAULT now()
);
```

---

### 3.3. `cuentas` — Cuentas Bancarias (tabla base)

```sql
CREATE TABLE IF NOT EXISTS public.cuentas (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id         UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  numero_cuenta   TEXT NOT NULL,
  tipo_cuenta     TEXT DEFAULT 'ahorros'
                    CHECK (tipo_cuenta IN ('ahorros','corriente','plazo_fijo')),
  moneda          TEXT DEFAULT 'PEN'
                    CHECK (moneda IN ('PEN','USD')),
  saldo           NUMERIC(14,2) DEFAULT 0,
  estado          TEXT DEFAULT 'activa'
                    CHECK (estado IN ('activa','inactiva','bloqueada')),
  created_at      TIMESTAMPTZ DEFAULT now(),
  updated_at      TIMESTAMPTZ DEFAULT now()
);
```

---

### 3.4. `transacciones` — Movimientos Bancarios

```sql
CREATE TABLE IF NOT EXISTS public.transacciones (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id         UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  cuenta_id       UUID NOT NULL REFERENCES public.cuentas(id) ON DELETE CASCADE,
  tipo            TEXT NOT NULL
                    CHECK (tipo IN ('credito','debito')),
  monto           NUMERIC(14,2) NOT NULL,
  descripcion     TEXT,
  categoria       TEXT,
  fecha           DATE NOT NULL DEFAULT CURRENT_DATE,
  referencia      TEXT,
  created_at      TIMESTAMPTZ DEFAULT now()
);
```

---

### 3.5. `perfiles_clientes` — Datos del Cliente

```sql
CREATE TABLE IF NOT EXISTS public.perfiles_clientes (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id             UUID NOT NULL UNIQUE REFERENCES auth.users(id) ON DELETE CASCADE,
  dni                 TEXT,
  nombres             TEXT,
  apellidos           TEXT,
  fecha_nacimiento    DATE,
  edad                INT GENERATED ALWAYS AS (
                        EXTRACT(YEAR FROM AGE(fecha_nacimiento))::INT
                      ) STORED,
  telefono            TEXT,
  distrito            TEXT,
  provincia           TEXT,
  departamento        TEXT,
  nombre_negocio      TEXT,
  tipo_negocio        TEXT,
  direccion_negocio   TEXT,
  lat_negocio         NUMERIC(10,7),
  lng_negocio         NUMERIC(10,7),
  antiguedad_negocio_meses INT DEFAULT 0,
  tenencia_local      TEXT CHECK (tenencia_local IN
                        ('alquilado_sin_contrato','alquilado_con_contrato','propio')),
  num_entidades_sbs   SMALLINT DEFAULT 0,
  calificacion_sbs    TEXT DEFAULT 'Normal',
  deuda_total_sbs     NUMERIC(12,2) DEFAULT 0,
  estado_cliente      TEXT DEFAULT 'activo'
                        CHECK (estado_cliente IN ('activo','bloqueado','inactivo')),
  created_at          TIMESTAMPTZ DEFAULT now(),
  updated_at          TIMESTAMPTZ DEFAULT now()
);
```

---

### 3.6. `movimientos_mensuales` — Agregados Mensuales

*(Ver scoring_preaprobados.sql para DDL completo)*

---

### 3.7. `features_scoring` — Features Calculados

*(Ver scoring_preaprobados.sql para DDL completo)*

---

### 3.8. `scores_transaccionales` — Score Transaccional

*(Ver scoring_preaprobados.sql para DDL completo — max 800 pts con 5 grupos)*

---

### 3.9. `fichas_campo` — Ficha de Evaluación

*(Ver scoring_preaprobados.sql para DDL completo — F1-F5 con generated columns)*

---

### 3.10. `creditos_preaprobados` — Créditos Resultado

*(Ver scoring_preaprobados.sql para DDL completo)*

---

### 3.11. `sync_queue` — Cola de Sincronización (NUEVA)

```sql
CREATE TABLE IF NOT EXISTS public.sync_queue (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  entity_type     TEXT NOT NULL,          -- 'ficha_campo', 'credito_preaprobado', etc.
  entity_id       TEXT NOT NULL,          -- UUID de la entidad
  operation       TEXT NOT NULL           -- 'INSERT', 'UPDATE', 'DELETE'
                    CHECK (operation IN ('INSERT','UPDATE','DELETE')),
  payload         JSONB NOT NULL,         -- JSON con los datos a sincronizar
  status          TEXT DEFAULT 'pending'
                    CHECK (status IN ('pending','processing','completed','failed','conflict')),
  retry_count     SMALLINT DEFAULT 0,
  max_retries     SMALLINT DEFAULT 5,
  error_message   TEXT,
  created_at      TIMESTAMPTZ DEFAULT now(),
  processed_at    TIMESTAMPTZ,
  asesor_id       INT REFERENCES public.asesores_negocio(id),
  device_id       TEXT                    -- identificador del dispositivo
);
```

---

### 3.12. `visitas` — Registro de Visitas (NUEVA)

```sql
CREATE TABLE IF NOT EXISTS public.visitas (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  asesor_id       INT NOT NULL REFERENCES public.asesores_negocio(id),
  cliente_id      UUID NOT NULL REFERENCES auth.users(id),
  ficha_id        UUID REFERENCES public.fichas_campo(id),
  -- Ubicación de la visita
  lat_visita      NUMERIC(10,7),
  lng_visita      NUMERIC(10,7),
  -- Timestamps
  fecha_visita    DATE NOT NULL DEFAULT CURRENT_DATE,
  hora_llegada    TIME,
  hora_salida     TIME,
  duracion_minutos INT GENERATED ALWAYS AS (
    EXTRACT(EPOCH FROM (hora_salida - hora_llegada)) / 60
  ) STORED,
  -- Estado
  estado          TEXT DEFAULT 'en_progreso'
                    CHECK (estado IN ('planificada','en_progreso','completada',
                                      'cancelada','no_realizada')),
  motivo_cancelacion TEXT,
  observaciones   TEXT,
  -- Sync
  sync_status     TEXT DEFAULT 'pending'
                    CHECK (sync_status IN ('pending','synced','conflict')),
  -- Auditoría
  created_at      TIMESTAMPTZ DEFAULT now(),
  updated_at      TIMESTAMPTZ DEFAULT now()
);
```

---

## 4. Relaciones

### 4.1. Diagrama de Relaciones

| Tabla origen             | FK                     | Tabla destino          | Tipo       |
|--------------------------|------------------------|------------------------|------------|
| perfiles_clientes        | user_id                | auth.users             | 1:1        |
| cuentas                  | user_id                | auth.users             | 1:N        |
| transacciones            | user_id, cuenta_id     | auth.users, cuentas    | N:1        |
| movimientos_mensuales    | user_id, cuenta_id     | auth.users, cuentas    | N:1        |
| features_scoring         | user_id                | auth.users             | 1:1        |
| scores_transaccionales   | user_id                | auth.users             | 1:1*       |
| fichas_campo             | user_id, score_id      | auth.users, scores     | N:1        |
| creditos_preaprobados    | user_id, ficha_id      | auth.users, fichas     | N:1        |
| asesores_negocio         | id_agencia             | agencias               | N:1        |
| visitas                  | asesor_id, cliente_id  | asesores, auth.users   | N:1        |
| sync_queue               | asesor_id              | asesores_negocio       | N:1        |

> *scores_transaccionales tiene UNIQUE en user_id para ON CONFLICT UPDATE

---

## 5. Índices

```sql
-- Performance: búsquedas frecuentes
CREATE INDEX IF NOT EXISTS idx_perfiles_user_id ON public.perfiles_clientes(user_id);
CREATE INDEX IF NOT EXISTS idx_perfiles_distrito ON public.perfiles_clientes(distrito);
CREATE INDEX IF NOT EXISTS idx_perfiles_dni ON public.perfiles_clientes(dni);

CREATE INDEX IF NOT EXISTS idx_cuentas_user_id ON public.cuentas(user_id);

CREATE INDEX IF NOT EXISTS idx_transacciones_user_id ON public.transacciones(user_id);
CREATE INDEX IF NOT EXISTS idx_transacciones_fecha ON public.transacciones(fecha);
CREATE INDEX IF NOT EXISTS idx_transacciones_cuenta_id ON public.transacciones(cuenta_id);

CREATE INDEX IF NOT EXISTS idx_movimientos_user_id ON public.movimientos_mensuales(user_id);
CREATE INDEX IF NOT EXISTS idx_movimientos_periodo ON public.movimientos_mensuales(periodo);

CREATE INDEX IF NOT EXISTS idx_features_user_id ON public.features_scoring(user_id);

CREATE INDEX IF NOT EXISTS idx_scores_user_id ON public.scores_transaccionales(user_id);
CREATE INDEX IF NOT EXISTS idx_scores_segmento ON public.scores_transaccionales(segmento_preliminar);

CREATE INDEX IF NOT EXISTS idx_fichas_user_id ON public.fichas_campo(user_id);
CREATE INDEX IF NOT EXISTS idx_fichas_fecha ON public.fichas_campo(fecha_visita);
CREATE INDEX IF NOT EXISTS idx_fichas_estado ON public.fichas_campo(estado_ficha);
CREATE INDEX IF NOT EXISTS idx_fichas_asesor ON public.fichas_campo(asesor_nombre);

CREATE INDEX IF NOT EXISTS idx_creditos_user_id ON public.creditos_preaprobados(user_id);
CREATE INDEX IF NOT EXISTS idx_creditos_estado ON public.creditos_preaprobados(estado);
CREATE INDEX IF NOT EXISTS idx_creditos_segmento ON public.creditos_preaprobados(segmento);

CREATE INDEX IF NOT EXISTS idx_asesores_agencia ON public.asesores_negocio(id_agencia);
CREATE INDEX IF NOT EXISTS idx_asesores_nivel ON public.asesores_negocio(nivel);

CREATE INDEX IF NOT EXISTS idx_visitas_asesor ON public.visitas(asesor_id);
CREATE INDEX IF NOT EXISTS idx_visitas_cliente ON public.visitas(cliente_id);
CREATE INDEX IF NOT EXISTS idx_visitas_fecha ON public.visitas(fecha_visita);

CREATE INDEX IF NOT EXISTS idx_sync_status ON public.sync_queue(status);
CREATE INDEX IF NOT EXISTS idx_sync_entity ON public.sync_queue(entity_type, entity_id);
```

---

## 6. Row Level Security (RLS)

### 6.1. Políticas Existentes

```sql
-- Habilitación de RLS
ALTER TABLE public.perfiles_clientes      ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.movimientos_mensuales  ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.features_scoring       ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.scores_transaccionales ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.fichas_campo           ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.creditos_preaprobados  ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.cuentas                ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.transacciones          ENABLE ROW LEVEL SECURITY;

-- Clientes: solo ven sus propios datos
CREATE POLICY "Cliente ve su perfil"
  ON public.perfiles_clientes FOR SELECT
  USING (auth.uid() = user_id);

CREATE POLICY "Cliente ve sus cuentas"
  ON public.cuentas FOR SELECT
  USING (auth.uid() = user_id);

CREATE POLICY "Cliente ve sus transacciones"
  ON public.transacciones FOR SELECT
  USING (auth.uid() = user_id);

-- Fichas: escritura solo para asesores (service role)
CREATE POLICY "Cliente ve su ficha"
  ON public.fichas_campo FOR SELECT
  USING (auth.uid() = user_id);
```

### 6.2. Políticas para Asesores (NUEVAS — requieren custom claims)

```sql
-- Política para que asesores lean clientes de su zona
-- Nota: requiere configurar custom claims en auth.users
-- o usar una tabla intermedia asesor_cliente_asignacion

CREATE POLICY "Asesor lee cartera asignada"
  ON public.perfiles_clientes FOR SELECT
  USING (
    EXISTS (
      SELECT 1 FROM public.creditos_preaprobados cp
      WHERE cp.user_id = perfiles_clientes.user_id
      -- Aquí se filtraría por asesor_id asignado
    )
  );

-- Para el plan gratuito, usar service_role key en la app
-- y manejar la autorización a nivel de aplicación
```

### 6.3. Estrategia de Seguridad por Fase

| Fase          | Estrategia                                         |
|---------------|-----------------------------------------------------|
| Desarrollo    | Service role key + autorización en la app           |
| Piloto        | RLS básico + custom claims para asesores            |
| Producción    | RLS completo + roles + auditoría                    |

---

## 7. Campos de Auditoría

Todas las tablas principales incluyen:

| Campo        | Tipo        | Default    | Descripción                |
|--------------|-------------|------------|----------------------------|
| created_at   | TIMESTAMPTZ | now()      | Fecha de creación          |
| updated_at   | TIMESTAMPTZ | now()      | Última modificación        |

### 7.1. Trigger de Updated_at

```sql
CREATE OR REPLACE FUNCTION public.update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Aplicar a tablas principales
CREATE TRIGGER update_perfiles_updated_at
  BEFORE UPDATE ON public.perfiles_clientes
  FOR EACH ROW EXECUTE FUNCTION public.update_updated_at();

CREATE TRIGGER update_fichas_updated_at
  BEFORE UPDATE ON public.fichas_campo
  FOR EACH ROW EXECUTE FUNCTION public.update_updated_at();

CREATE TRIGGER update_creditos_updated_at
  BEFORE UPDATE ON public.creditos_preaprobados
  FOR EACH ROW EXECUTE FUNCTION public.update_updated_at();

CREATE TRIGGER update_cuentas_updated_at
  BEFORE UPDATE ON public.cuentas
  FOR EACH ROW EXECUTE FUNCTION public.update_updated_at();
```

---

## 8. Campos de Sincronización Offline

Las tablas que se modifican desde la app incluyen:

| Campo          | Tipo   | Valores                                | Descripción              |
|----------------|--------|----------------------------------------|--------------------------|
| sync_status    | TEXT   | pending, synced, conflict              | Estado de sincronización |
| local_id       | UUID   | gen_random_uuid() local                | ID generado offline      |
| server_id      | UUID   | NULL hasta que se sincronice           | ID del servidor          |
| last_modified  | BIGINT | Timestamp en millis                    | Para resolución conflicto|
| device_id      | TEXT   | Identificador del dispositivo          | Trazabilidad             |

---

## 9. Funciones y Procedimientos

### 9.1. Funciones Existentes

| Función                          | Descripción                             | Entrada    | Salida      |
|----------------------------------|-----------------------------------------|------------|-------------|
| `calcular_features_scoring()`    | Calcula features desde transacciones    | user_id    | VOID        |
| `calcular_score_transaccional()` | Calcula score de 800 pts               | user_id    | TABLE       |
| `update_updated_at()`            | Trigger para updated_at automático      | —          | TRIGGER     |

---

## 10. Vistas (Power BI & Análisis)

| Vista                       | Propósito                                    |
|-----------------------------|----------------------------------------------|
| `vw_pbi_universo_scoring`   | Clientes elegibles con score transaccional   |
| `vw_pbi_fichas_campo`       | Fichas de campo con score final              |
| `vw_pbi_embudo_campania`    | KPIs de conversión por agencia/asesor        |
| `vw_pbi_calidad_cartera`    | Seguimiento de mora y calidad crediticia     |
| `vw_pbi_kpis_piloto`        | Scorecard ejecutivo del piloto               |
| `vw_pbi_asesores`           | Resumen de asesores para reporting           |
| `vw_pbi_agencias`           | Resumen gerencial por agencia                |

---

## 11. Seed Data

| Tabla              | Registros | Script                         |
|--------------------|-----------|--------------------------------|
| agencias           | 30        | seed_agencias_asesores.sql     |
| asesores_negocio   | 360       | seed_agencias_asesores.sql     |
| cuentas            | (por generar) | setup adicional            |
| transacciones      | (por generar) | setup adicional            |

---

## 12. Criterios de Aceptación

- [ ] Todas las tablas se crean sin errores en Supabase
- [ ] Las FK constraints son correctas y no tienen referencias huérfanas
- [ ] Los índices están creados para las queries más frecuentes
- [ ] Las RLS policies están activas en tablas con datos sensibles
- [ ] Los generated columns calculan correctamente (scores, segmentos)
- [ ] El trigger de updated_at funciona en UPDATE
- [ ] Las vistas Power BI retornan datos correctos
- [ ] La tabla sync_queue permite operaciones CRUD completas
- [ ] El seed data se ejecuta sin errores
- [ ] Los conteos de verificación son correctos (30 agencias, 360 asesores)
