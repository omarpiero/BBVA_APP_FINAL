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
