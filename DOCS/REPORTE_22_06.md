# Reporte de Cambios e Implementaciones - Sprint Final (22/06)
Este reporte resume todas las implementaciones y validaciones realizadas en la base de datos Supabase, la aplicación de Fuerza de Ventas (Flutter), la App de Clientes (Kotlin) y el Admin Dashboard (Flutter).

---

## 🛠️ Sprint 1: Setup y Limpieza
- **Kotlin App (Limpieza de presets):**
  - Se eliminó el listado estático `casosCredito` y la clase `CasoCreditoPreset` en [PrestamoScreen.kt](file:///c:/Users/user%2001/Downloads/BBVA_Ventas_AppCore-main/KOTLINUSER/Desarrollo-AppMovilBanco-main/app/src/main/java/com/example/appbanco_s8/ui/screens/PrestamoScreen.kt).
  - Se eliminó el auto-fill de presets para forzar la carga dinámica de datos del core.
- **Admin Dashboard (Asignación de clientes y corrección de advertencias):**
  - Se modificó [asesores_view.dart](file:///c:/Users/user%2001/Downloads/BBVA_Ventas_AppCore-main/admin_dashboard/lib/ui/views/asesores_view.dart) agregando un botón y diálogo modal para **Asignar Cliente**.
  - Al realizar la asignación, se inserta una nueva fila en `cartera_diaria` en Supabase con rol de asesor, cliente, fecha de asignación y el tipo de gestión.
  - Se corrigió la advertencia de miembro obsoleto en `DropdownButtonFormField` reemplazando la propiedad `value` por `initialValue`.
  - Se solucionó la advertencia de uso de `BuildContext` a través de brechas asíncronas capturando la instancia de `ScaffoldMessenger` antes de realizar las llamadas asíncronas y operaciones de navegación (`pop`).

---

## 📱 Sprint 2: App Clientes (Kotlin)
- **Autenticación Real & Seguridad:**
  - Integración de Supabase Auth con almacenamiento seguro en `EncryptedSharedPreferences` en la clase [PrefsManager.kt](file:///c:/Users/user%2001/Downloads/BBVA_Ventas_AppCore-main/KOTLINUSER/Desarrollo-AppMovilBanco-main/app/src/main/java/com/example/appbanco_s8/data/local/PrefsManager.kt).
  - Cierre de sesión y auto-login dinámicos en [NavGraph.kt](file:///c:/Users/user%2001/Downloads/BBVA_Ventas_AppCore-main/KOTLINUSER/Desarrollo-AppMovilBanco-main/app/src/main/java/com/example/appbanco_s8/navigation/NavGraph.kt).
- **Home de Ahorros, Créditos y Movimientos:**
  - Conexión real de ahorros mapeados de `cr_cuentas_ahorro` filtrando por el ID de cliente resuelto.
  - Carga real de cronograma de pagos de `cr_cronograma_pagos` y créditos de `cr_creditos`.
  - Se reemplazó la UI de marcadores de posición en [CuentaScreen.kt](file:///c:/Users/user%2001/Downloads/BBVA_Ventas_AppCore-main/KOTLINUSER/Desarrollo-AppMovilBanco-main/app/src/main/java/com/example/appbanco_s8/ui/screens/CuentaScreen.kt) con un diseño premium que carga balances y movimientos.
  - Se corrigió un error de sintaxis (sintaxis Flutter de tipo `Expanded(child: ...)`) reemplazándolo por el estándar de Jetpack Compose: `Box(modifier = Modifier.weight(1f))`.
- **Operaciones:**
  - Registro en `operaciones_cliente` y `sync_outbox` ante transferencias.

---

## 🤝 Sprint 3: App Fuerza de Ventas (Flutter)
- **Consulta de Buró con Consentimiento Firmado (M7):**
  - Modificación del controlador de firma en [signature_pad.dart](file:///c:/Users/user%2001/Downloads/BBVA_Ventas_AppCore-main/lib/shared/widgets/signature_pad.dart) para serializar los trazos a PNG en formato Base64 de forma asíncrona mediante el método `toPngBase64()`.
  - Modificación de [buro_repository.dart](file:///c:/Users/user%2001/Downloads/BBVA_Ventas_AppCore-main/lib/features/buro/data/buro_repository.dart) y [buro_screen.dart](file:///c:/Users/user%2001/Downloads/BBVA_Ventas_AppCore-main/lib/features/buro/presentation/buro_screen.dart) para capturar y guardar la firma digital en la columna `firma_consentimiento_base64` de la tabla `consultas_buro` en Supabase.
- **Soporte Offline-First & Sincronización:**
  - Modificación de [solicitud_repository.dart](file:///c:/Users/user%2001/Downloads/BBVA_Ventas_AppCore-main/lib/features/solicitud/data/solicitud_repository.dart) para interceptar el estado sin red: guarda la solicitud localmente en `solicitudes_borrador` (SQLite) con `paso_actual = 4`.
  - Modificación de [transmision_screen.dart](file:///c:/Users/user%2001/Downloads/BBVA_Ventas_AppCore-main/lib/features/transmision/presentation/transmision_screen.dart) para mostrar estados de carga offline y redirigir al usuario adecuadamente.
  - Modificación de [cartera_repository.dart](file:///c:/Users/user%2001/Downloads/BBVA_Ventas_AppCore-main/lib/features/cartera/data/cartera_repository.dart) para sincronizar en segundo plano tanto las visitas pendientes como las solicitudes offline marcadas para sincronización cuando se detecta conexión.

---

## ⚡ Sprint 4: Integración End-to-End (Sync Backend)
- **Triggers Automáticos en la Base de Datos (Supabase):**
  - **`trg_bbva_procesar_sync_outbox`**: Procesa la cola de `sync_outbox` de manera atómica. Promueve las solicitudes al esquema principal y ejecuta transferencias o pagos de cuotas.
  - **`trg_bbva_solicitudes_credito_desembolso`**: Al desembolsar un préstamo, genera automáticamente una cuenta de ahorro (si no existe), incrementa el saldo del cliente, genera su registro de préstamo en `cr_creditos` y autocalcula su cronograma de pagos en `cr_cronograma_pagos` en la réplica del core.

---

## 🔒 Sprint 5: Seguridad y Control de Acceso
- **Políticas de RLS Activas:**
  - Verificación y validación de RLS activo en `cartera_diaria`, `solicitudes_credito` y `cr_creditos` restringiendo accesos por JWT (`auth.uid()`).
- **Bloqueo Persistente por Intentos Fallidos (Lockouts):**
  - Creación de las funciones RPC `bbva_obtener_estado_bloqueo`, `bbva_registrar_intento_fallido` y `bbva_resetear_intentos_fallidos` como `SECURITY DEFINER` en Supabase.
  - Integración en [login_viewmodel.dart](file:///c:/Users/user%2001/Downloads/BBVA_Ventas_AppCore-main/lib/features/auth/presentation/login_viewmodel.dart) (Flutter) y [AuthViewModel.kt](file:///c:/Users/user%2001/Downloads/BBVA_Ventas_AppCore-main/KOTLINUSER/Desarrollo-AppMovilBanco-main/app/src/main/java/com/example/appbanco_s8/ui/viewmodel/AuthViewModel.kt) (Kotlin) para realizar validaciones a nivel de base de datos antes, durante y después del flujo de autenticación, bloqueando la cuenta al quinto intento.

---

## 📊 Sprint 6: Demos y Validación
- **Mora Activa para Semáforo de Riesgo:**
  - Se actualizó el registro `CR-T005` en Supabase para tener `dias_mora = 45`, lo que permite validar que los semáforos de riesgo en el frontend se rendericen con color de alerta roja/morosa de manera correcta.