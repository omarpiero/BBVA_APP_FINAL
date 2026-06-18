# KANBAN Y ROADMAP — UNIFICACIÓN CORE & DASHBOARD WEB
**Fecha:** 16 de Junio de 2026
**Objetivo:** Integrar la App de Asesores (Ventas) y la App de Usuarios (Kotlin) bajo una misma Base de Datos Core, y construir un Dashboard de Administrador en Flutter Web.

---

## 🏃 Sprint 1: Unificación de Base de Datos Core
**Objetivo:** Consolidar `user_database_schema.sql` dentro de `bd_core_mobile.sql` y cargar datos semilla.

- [ ] **TODO**
  - *(Vacío)*
- [ ] **IN PROGRESS**
  - *(Vacío)*
- [x] **DONE**
  - [x] Analizar conflictos de IDs: `auth.users` (Supabase) vs `clientes` (Core).
  - [x] Migrar tablas de pagos, transferencias Plin, y comercios al esquema core.
  - [x] Actualizar llaves foráneas (`user_id` -> `cliente_id`).
  - [x] Adaptar `seed_agencias_asesores.sql` y `scoring_preaprobados.sql` al nuevo esquema unificado.

---

## 🏃 Sprint 2: Adaptación de Aplicaciones Móviles
**Objetivo:** Asegurar que la App de Ventas y la App de Usuarios Kotlin apunten y consuman la base de datos core unificada.

- [ ] **TODO**
  - *(Vacío)*
- [ ] **IN PROGRESS**
  - *(Vacío)*
- [x] **DONE**
  - [x] Modificar queries en la App Kotlin para consumir la nueva estructura (Ej: unificación de perfiles y usuarios).
  - [x] Validar que la sincronización Offline-First de la App Fuerza de Ventas funcione con las tablas core modificadas.
  - [x] Probar flujos de autenticación en ambas aplicaciones.
  - [x] Implementar la función unificada de simulación de cuotas (TEA 43.92%).

---

## 🏃 Sprint 3: Inicialización del Dashboard Web (Flutter)
**Objetivo:** Crear la estructura base del administrador web y aplicar diseño premium.

- [ ] **TODO**
  - *(Vacío)*
- [ ] **IN PROGRESS**
  - *(Vacío)*
- [x] **DONE**
  - [x] Inicializar proyecto Flutter Web (`flutter create --platforms web admin_dashboard`).
  - [x] Definir el Design System (colores BBVA, tipografía, glassmorphism, modo oscuro opcional).
  - [x] Implementar Sidebar Navigation y Layout principal responsivo.
  - [x] Configurar ruteo web (GoRouter u otro).

---

## 🏃 Sprint 4: Desarrollo de Features del Dashboard
**Objetivo:** Conectar el dashboard a la BD core y visualizar los datos.

- [ ] **TODO**
  - [ ] **Monitoreo de Operaciones:** Historial unificado de pagos, transacciones Plin y cobranzas.
- [ ] **IN PROGRESS**
  - *(Vacío)*
- [x] **DONE**
  - [x] **Vista Home/Resumen:** KPIs generales (total asesores, créditos aprobados, mora).
  - [x] **Gestión de Asesores:** Lista de asesores, agencias y sus metas.
  - [x] **Gestión de Clientes:** Tabla con clientes, segmentos y estado de solicitudes.

---

## Sprint 5: Flujo E2E minimo testeable de creditos
**Objetivo:** Permitir probar el circuito cliente -> asesor -> admin usando la base core de Supabase y priorizando originacion/estado de solicitudes.

- [ ] **TODO**
  - [ ] Ejecutar prueba manual E2E completa: cliente solicita credito -> asesor gestiona en `Estado de solicitudes` -> admin revisa en dashboard.
  - [ ] Confirmar si existe un cuarto caso externo: `DOCS/CASOS.md` contiene 3 casos explicitos y la app cliente deja modo Manual para un caso adicional.
  - [ ] Reintentar verificacion REST/visual del flujo tras el bloqueo de uso de la sesion Codex: solicitud cliente, solicitud asesor, cambio de perfil, nota interna, RPC de estados y vista admin.
  - [ ] Revisar/aplicar el endurecimiento final de RLS y RPC de transicion si se decide llevar la demo a un estado mas cercano a produccion.
  - [ ] Cargar los 30 casos completos del enunciado cuando el flujo minimo ya este estable.
- [ ] **IN PROGRESS**
  - *(Vacio)*
- [x] **DONE**
  - [x] App Clientes Kotlin: solicitud de credito conectada a `clientes`, `asesores`, `solicitudes_credito` y `sync_outbox`, con listado de estados del cliente.
  - [x] App Fuerza de Ventas Flutter: tablero `Estado de solicitudes` con acciones de flujo `recibir`, `evaluar`, `aprobar`, `condicionar`, `rechazar` y `desembolsar`.
  - [x] Dashboard Admin Flutter Web: login Supabase, vista `/creditos` con KPIs/filtros/detalle y vistas de clientes/asesores conectadas al core.
  - [x] SQL preparado: `DOCS/BASE DE DATOS/05_FLUJO_CREDITOS_DEMO.sql` con RLS por identidad, usuarios demo y funciones de calculo/transicion.
  - [x] Supabase Auth remoto: credenciales demo verificadas por endpoint real para `client.demo@bbva.pe`, `asesor02@asesores.pe` y `admin.demo@bbva.pe`.
  - [x] App Fuerza de Ventas: nombre Android actualizado a `BBVA Ventas`.
  - [x] App Clientes Kotlin: APK debug reinstalado y crash post-splash resuelto; `logcat` ya no reporta `FATAL EXCEPTION` al abrir `MainActivity`.
  - [x] App Clientes Kotlin: corregida consulta de cuentas (`tipo_cuenta`) y expuesto acceso directo `Credito` en Home/barra inferior.
  - [x] Supabase demo data: `client.demo@bbva.pe` tiene cuenta y movimientos; `asesor02@asesores.pe` tiene 3 clientes en cartera de hoy.
  - [x] Supabase remoto: migracion `enable_demo_rls_credit_flow` aplicada con RLS/RPC para `perfiles`, `clientes`, `cartera_diaria`, `solicitudes_credito`, `solicitudes_notas_internas`, `sync_outbox`, `cr_creditos` y `cr_cronograma_pagos`.
  - [x] Supabase remoto: casos 1-3 de `DOCS/CASOS.md` sembrados en `bbva_casos_credito`, `auth.users`, `clientes`, `perfiles` y `cuentas`.
  - [x] Supabase remoto: trigger seguro crea `cartera_diaria` al insertar solicitud desde App Clientes; prueba RLS transaccional confirmo 1 cartera, 1 credito y 12 cuotas para Caso 1.
  - [x] Supabase remoto: credenciales verificadas por Auth real para `caso01.cliente@bbva.pe`, `caso02.cliente@bbva.pe`, `caso03.cliente@bbva.pe`, `asesor02@asesores.pe` y `admin.demo@bbva.pe`.
  - [x] App Clientes Kotlin: pantalla `Credito` con presets Caso 1-3, TEA/gastos variables, solicitud core y listado de `cr_creditos` + primeras cuotas de `cr_cronograma_pagos`.
  - [x] App Fuerza de Ventas Flutter: buro y pre-evaluacion migrados de API local a Supabase (`consultas_buro`, `fichas_campo`) con resultados deterministas por DNI/casos.
  - [x] App Fuerza de Ventas Flutter: documentos suben al bucket `documentos_cliente` por carpeta de asesor y registran metadata en `solicitudes_documentos`.
  - [x] App Fuerza de Ventas Flutter: detalle de `Estado de solicitudes` abre carga de documentos por expediente y PDF actualizado a marca BBVA.
