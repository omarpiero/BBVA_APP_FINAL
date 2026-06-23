# Checklist de Tareas

## Sprint 1: Setup y Limpieza
- [x] Kotlin App: Eliminar presets `casosCredito` en `PrestamoScreen.kt`
- [x] Kotlin App: Limpiar cualquier botón o función de auto-fill vinculada a presets
- [x] Admin Dashboard: Modificar `asesores_view.dart` para agregar modal "Asignar Cliente"
- [x] Admin Dashboard: Registrar la asignación insertando fila en `cartera_diaria`

## Sprint 2: Criterio 3 - App Clientes (Autoservicio Kotlin)
- [x] Kotlin App: Autenticación real contra `usuarios_cliente` usando Supabase Auth (JWT) y almacenar token en `EncryptedSharedPreferences`
- [x] Kotlin App: Conectar perfil y ahorros consultando la tabla `cr_cuentas_ahorro` filtrando por `cliente_id`
- [x] Kotlin App: Conectar vista de créditos leyendo de `cr_creditos` y cronograma desde `cr_cronograma_pagos`
- [x] Kotlin App: Implementar operaciones de pago/transferencia insertando en `operaciones_cliente` y `sync_outbox`

## Sprint 3: Criterio 2 - App Fuerza de Ventas (Flutter)
- [x] FVentas: Modificar repositorios para consultar `cartera_diaria` unida a `clientes` y soportar modo offline/online
- [x] FVentas: Capturar ubicación GPS de la visita y guardarla en local/remoto, visualizar semáforos/ofertas de campaña
- [x] FVentas: Flujo de consentimiento firmado en consulta de buró guardando en `consultas_buro` (firma en base64)
- [x] FVentas: Completar stepper de solicitud guardando en SQLite local (`solicitudes_borrador`)
- [x] FVentas: Motor de sincronización para enviar borradores a `sync_outbox` de Supabase

## Sprint 4: Criterio 1 - Integración End-to-End
- [x] Supabase: Crear trigger/función en `sync_outbox` para procesar y promover solicitudes/operaciones al esquema principal
- [x] Supabase: Validar flujo cruzado (solicitud promovida a 'desembolsado' impacta `cr_creditos` y saldos de cuentas)

## Sprint 5: Criterio 4 - Seguridad y Control de Acceso
- [x] Supabase: Configurar políticas RLS en `cartera_diaria`, `solicitudes_credito`, y `cr_creditos`
- [x] BD & Apps: Control de bloqueo persistente por 5 intentos fallidos de inicio de sesión (RPCs y lógica cliente)
- [x] Admin Dashboard: Validar rol del JWT para control de accesos

## Sprint 6: Criterio 5 - Calidad, Arquitectura y Demos
- [x] Supabase: Validar FKs y restricciones de integridad
- [x] Demos: Configurar datos de prueba con días de mora activos para visualizar semáforo de riesgo
- [x] Documentación: Registrar cambios en `DOCS/REPORTE_22_06.md` y actualizar `DOCS/KANBAN_22_06.md`
