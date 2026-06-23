# Tablero Kanban - Sprint Final (22/06) - Detallado para Ejecución
## Integración End-to-End, Fuerza de Ventas, Admin & App Clientes

### SPRINT 1: Setup y Limpieza (Kotlin & Admin)
- [x] **Kotlin App:** Abrir `KOTLINUSER/Desarrollo-AppMovilBanco-main/app/src/main/java/com/example/appbanco_s8/ui/screens/PrestamoScreen.kt` and eliminate presets.
- [x] **Kotlin App:** Clean auto-fill presets function from entry points.
- [x] **Admin Dashboard:** Add "Asignar Cliente" action button and modal in `asesores_view.dart`.
- [x] **Admin Dashboard:** Insert a row in `cartera_diaria` upon assignment in Supabase.

### SPRINT 2: Criterio 3 - App Clientes (Autoservicio Kotlin)
- [x] **Kotlin App (Login):** Authenticate with Supabase Auth (JWT) and store token securely in `EncryptedSharedPreferences`.
- [x] **Kotlin App (Home/Ahorros):** Display user savings balance and details from `cr_cuentas_ahorro` filtering by resolved `cliente_id`.
- [x] **Kotlin App (Créditos):** Display credits from `cr_creditos` and schedule details from `cr_cronograma_pagos`.
- [x] **Kotlin App (Transacciones):** Register payments and transfers by inserting into `operaciones_cliente` and `sync_outbox`. Display history from `cr_movimientos`.

### SPRINT 3: Criterio 2 - App Fuerza de Ventas (Flutter)
- [x] **FVentas (Cartera):** Consult `cartera_diaria` joined with `clientes` corresponding to `asesor_id`. Support offline reads and automatic online synchronization.
- [x] **FVentas (Ficha/Mapa):** Capture GPS coordinates during visits, save locally, and sync when online. Color code risk status.
- [x] **FVentas (Buró):** Implement SBS/blacklist consultation with a signed consent pad, storing the signature in base64 within `consultas_buro`.
- [x] **FVentas (Solicitud):** Capture application data in SQLite draft table `solicitudes_borrador` when offline.
- [x] **FVentas (Transmisión):** Read pending offline applications and push them to `sync_outbox` upon connection.

### SPRINT 4: Criterio 1 - Integración End-to-End (Backend/Sync)
- [x] **Supabase (Sync Outbox):** Automatic PL/pgSQL database trigger `trg_bbva_procesar_sync_outbox` on `sync_outbox` to promote/execute operations to main tables.
- [x] **Supabase (Flujo Cruzado):** Promote credit applications to 'desembolsado', automatically generating accounts, schedules, and adjusting savings balances in database.

### SPRINT 5: Criterio 4 - Seguridad y control de acceso (JWT + RBAC)
- [x] **Supabase (RLS):** Enabled Row Level Security (RLS) on `cartera_diaria`, `solicitudes_credito`, and `cr_creditos`.
- [x] **Todas las Apps (Bloqueo):** Implement RPCs `bbva_registrar_intento_fallido`, `bbva_resetear_intentos_fallidos`, and `bbva_obtener_estado_bloqueo` to block logins after 5 failed attempts in database.
- [x] **Admin Dashboard:** JWT verification for supervisor/admin views.

### SPRINT 6: Criterio 5 - Calidad, Arquitectura y Demos
- [x] **Supabase:** Validate FKs and constraints in `bd_core_mobile`.
- [x] **Demos:** Updated credit row `CR-T005` to have active mora (`dias_mora = 45`) for risk color validation.
- [x] **Documentación:** Detailed report created in `DOCS/REPORTE_22_06.md`.
