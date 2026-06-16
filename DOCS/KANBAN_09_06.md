# KANBAN Board - BBVA Flutter Migration

Este tablero tiene como objetivo organizar los sprints y tareas para la migración de la app a Flutter, su conexión a Supabase y la adopción del branding de BBVA.

## Sprint 1: Fundación y Configuración (Actual)
- [x] Análisis del modelo de datos de la app actual.
- [x] Incorporación del nuevo esquema de BD de los documentos en Supabase.
- [x] Configuración de inicialización de Supabase en Flutter.
- [x] Creación de usuario de prueba (`asesor02@asesores.pe`).
- [x] Adopción de la paleta de colores corporativa de BBVA.
- [x] Configuración y generación de iconos de app y splash screen con el logo de BBVA.

## Sprint 2: Autenticación y Offline First
- [x] Conectar el `login_viewmodel.dart` para validar credenciales contra Supabase.
- [x] Habilitar la persistencia offline con Sqflite usando la tabla de asesores locales.
- [x] Implementar servicio de refresco de token y control de sesión con Supabase.

## Sprint 3: Sincronización de Cartera y Consultas
- [x] Obtener y sincronizar la tabla `cartera_diaria` para el asesor logueado.
- [x] Implementar la sincronización bidireccional mediante `sync_outbox`.
- [x] Listado de créditos y detalles de mora en la interfaz adaptada a BBVA.

## Sprint 4: Flujo de Gestión de Solicitudes (Originación)
- [x] Desarrollo de UI para crear `solicitudes_credito`.
- [x] Captura de documentos y evaluación de nitidez usando el motor `image`.
- [x] Sincronización de nuevas solicitudes al core.

## Sprint 5: Mapa, Geolocalización y Notificaciones
- [x] Integración de Google Maps para pintar la ruta de visitas diarias.
- [x] Habilitar notificaciones Push (vía Supabase Realtime como alternativa a FCM).
- [x] Tareas en background (Workmanager) para sincronización nocturna.
