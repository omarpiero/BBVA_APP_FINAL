# 📋 BBVA App - Tablero Kanban / Sprints

> **Última actualización:** Sesión 1 - 19/05/2026  
> **Metodología:** Spec Driven Development (SDD)

---

## 🏃 Sprint 1 — Flujo PLIN + Pago en Línea *(Sesión Actual)*

### Objetivo
Implementar el flujo completo: **Home → PLIN → Pagar en Línea → Confirmación de Pago**

| ID | Tarea | Estado | Prioridad | Asignado |
|----|-------|--------|-----------|----------|
| S1-01 | Actualizar `HomeScreen` al diseño BBVA (mockup fondo blanco) | ✅ DONE | 🔴 Alta | Sesión 1 |
| S1-02 | Crear tabla `comercios` en Supabase | ✅ DONE | 🔴 Alta | Sesión 1 |
| S1-03 | Crear tabla `plin_transacciones` en Supabase | ✅ DONE | 🔴 Alta | Sesión 1 |
| S1-04 | Crear tabla `pagos_en_linea` en Supabase | ✅ DONE | 🔴 Alta | Sesión 1 |
| S1-05 | Crear modelos Kotlin para PLIN y Pago en Línea | ✅ DONE | 🔴 Alta | Sesión 1 |
| S1-06 | Crear `PlinScreen` con diseño BBVA | ✅ DONE | 🔴 Alta | Sesión 1 |
| S1-07 | Crear `PagarEnLineaScreen` con diseño BBVA | ✅ DONE | 🔴 Alta | Sesión 1 |
| S1-08 | Crear `ConfirmacionPagoScreen` con diseño BBVA | ✅ DONE | 🔴 Alta | Sesión 1 |
| S1-09 | Crear repositorios y API para PLIN/Pago en Línea | ✅ DONE | 🔴 Alta | Sesión 1 |
| S1-10 | Crear ViewModels para PLIN/Pago en Línea | ✅ DONE | 🔴 Alta | Sesión 1 |
| S1-11 | Agregar rutas de navegación (Screen.kt + NavGraph.kt) | ✅ DONE | 🔴 Alta | Sesión 1 |
| S1-12 | Conectar flujo completo Home → PLIN → Pagar → Confirmación | ✅ DONE | 🔴 Alta | Sesión 1 |
| S1-13 | Agregar RLS policies en Supabase | ✅ DONE | 🟡 Media | Sesión 1 |
| S1-14 | Actualizar documentación SDD | ✅ DONE | 🟡 Media | Sesión 1 |

---

## 📅 Sprint 2 — Transferencias y PLIN Envío *(Próxima Sesión)*

| ID | Tarea | Estado | Prioridad |
|----|-------|--------|-----------|
| S2-01 | Crear `TransferScreen` (transferencia entre cuentas) | 🔲 TODO | 🔴 Alta |
| S2-02 | Crear `PlinEnviarScreen` (enviar dinero a contactos) | 🔲 TODO | 🔴 Alta |
| S2-03 | Crear `PlinContactosScreen` (lista de contactos PLIN) | 🔲 TODO | 🔴 Alta |
| S2-04 | Crear tabla `plin_contactos` en Supabase | 🔲 TODO | 🔴 Alta |
| S2-05 | Implementar lógica de descuento de saldo | 🔲 TODO | 🔴 Alta |
| S2-06 | Historial de movimientos PLIN en `PlinScreen` | 🔲 TODO | 🟡 Media |
| S2-07 | Notificaciones push de transferencias | 🔲 TODO | 🟢 Baja |

---

## 📅 Sprint 3 — Cuentas y Tarjetas *(Futuro)*

| ID | Tarea | Estado | Prioridad |
|----|-------|--------|-----------|
| S3-01 | Implementar `CuentaScreen` completa | 🔲 TODO | 🔴 Alta |
| S3-02 | Implementar `TarjetaScreen` completa | 🔲 TODO | 🔴 Alta |
| S3-03 | Detalle de movimientos por cuenta | 🔲 TODO | 🟡 Media |
| S3-04 | Bloquear/desbloquear tarjeta | 🔲 TODO | 🟡 Media |
| S3-05 | Cambio de contraseña de tarjeta | 🔲 TODO | 🟢 Baja |

---

## 📅 Sprint 4 — Préstamos y Pagos de Servicios *(Futuro)*

| ID | Tarea | Estado | Prioridad |
|----|-------|--------|-----------|
| S4-01 | Implementar `PrestamoScreen` completa | 🔲 TODO | 🔴 Alta |
| S4-02 | Detalle de cuotas y cronograma | 🔲 TODO | 🟡 Media |
| S4-03 | Pago de servicios (agua, luz, gas, etc.) | 🔲 TODO | 🟡 Media |
| S4-04 | Historial de pagos de servicios | 🔲 TODO | 🟢 Baja |

---

## 📅 Sprint 5 — Perfil y Configuración *(Futuro)*

| ID | Tarea | Estado | Prioridad |
|----|-------|--------|-----------|
| S5-01 | Implementar `PerfilScreen` completa | 🔲 TODO | 🟡 Media |
| S5-02 | Cambio de contraseña de acceso | 🔲 TODO | 🟡 Media |
| S5-03 | Configuración de notificaciones | 🔲 TODO | 🟢 Baja |
| S5-04 | Datos biométricos (huella/face) | 🔲 TODO | 🟢 Baja |
| S5-05 | T-Cambio (tipo de cambio) | 🔲 TODO | 🟢 Baja |

---

## 📊 Leyenda de Estados

| Emoji | Estado |
|-------|--------|
| 🔲 | TODO - Pendiente |
| 🔄 | IN PROGRESS - En desarrollo |
| ✅ | DONE - Completado |
| ⏸️ | BLOCKED - Bloqueado |
| 🚫 | CANCELLED - Cancelado |

## 📊 Leyenda de Prioridades

| Emoji | Prioridad |
|-------|-----------|
| 🔴 | Alta - Crítico |
| 🟡 | Media - Importante |
| 🟢 | Baja - Deseable |

---

## 📝 Registro de Sesiones

### Sesión 1 - 19/05/2026
- **Objetivo:** Flujo Home → PLIN → Pagar en Línea → Confirmación
- **Estado:** ✅ Completado
- **Notas:**
  - Análisis del proyecto completado.
  - Documentación SDD creada y actualizada.
  - Se estructuró e implementó toda la lógica de negocio (Models, Repositories, ViewModels).
  - Se diseñó toda la interfaz de usuario en base al branding de BBVA (HomeScreen, PlinScreen, PagarEnLineaScreen, ConfirmacionPagoScreen y AppScaffold).
  - Se configuró la decodificación automática del token JWT (extracción del user_id UUID real de Supabase Auth) para asegurar la compatibilidad con RLS y la función de base de datos.
  - Se proporcionaron los scripts SQL y los datos de prueba en `DOCS/06_DATABASE_CHANGES.md`.

---

*Este documento se actualiza al inicio y final de cada sesión de desarrollo.*
