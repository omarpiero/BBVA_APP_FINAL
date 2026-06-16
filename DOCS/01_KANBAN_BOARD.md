# 01 — KANBAN BOARD & ROADMAP

| Campo               | Valor                                                        |
|---------------------|--------------------------------------------------------------|
| **ID**              | SDD-01                                                       |
| **Sprint**          | Sprint 0 — Fundación                                         |
| **Estado**          | ✅ Completo                                                   |
| **Última revisión** | 2026-05-26                                                   |
| **Autor**           | Equipo Arquitectura                                          |

---

## 1. Objetivo

Definir el roadmap completo del proyecto **BBVA Fuerza de Ventas**, organizado en sprints con épicas, backlog priorizado, dependencias, milestones y estados.

---

## 2. Épicas del Proyecto

| Épica  | Nombre                     | Descripción                                                   |
|--------|----------------------------|---------------------------------------------------------------|
| E-01   | Fundación & Documentación  | Documentación SDD, setup de proyecto y base de datos          |
| E-02   | Autenticación              | Login/logout de asesores, manejo de sesiones                  |
| E-03   | Cartera de Clientes        | Descarga, visualización y gestión de clientes preaprobados    |
| E-04   | Ruta de Visitas            | Mapa, planificación de rutas, geolocalización                 |
| E-05   | Ficha de Campo             | Evaluación crediticia F1-F5, scoring, propuesta               |
| E-06   | Motor de Scoring           | Cálculos de score transaccional, campo y final                |
| E-07   | Gestión Offline            | Room database, sync queue, conflict resolution                |
| E-08   | Historial & Reportes       | Historial de visitas, filtros, búsqueda                       |
| E-09   | Sincronización             | Sync manager, retry policy, network monitoring                |
| E-10   | Documentos & Propuestas    | Generación de propuestas de crédito, comité                   |
| E-11   | QA & Producción            | Testing, optimización, preparación release                    |

---

## 3. Roadmap por Sprints

### Sprint 0 — Fundación (Semana 1)

| ID       | Tarea                                      | Prioridad | Estado      | Dependencia | Épica |
|----------|--------------------------------------------|-----------|-------------|-------------|-------|
| S0-001   | Generar documentos SDD (00-10)             | 🔴 Alta   | `[x]`       | —           | E-01  |
| S0-002   | Setup base de datos Supabase               | 🔴 Alta   | `[x]`       | —           | E-01  |
| S0-003   | Ejecutar seed data (agencias + asesores)   | 🔴 Alta   | `[x]`       | S0-002      | E-01  |
| S0-004   | Configurar proyecto Android (Hilt, Ktor)   | 🔴 Alta   | `[x]`       | —           | E-01  |
| S0-005   | Crear design system (colores, tipografía)  | 🟡 Media  | `[x]`       | S0-004      | E-01  |
| S0-006   | Implementar core/network (SupabaseClient)  | 🔴 Alta   | `[x]`       | S0-004      | E-01  |
| S0-007   | Crear modelos de dominio base              | 🟡 Media  | `[ ]`       | S0-004      | E-01  |
| S0-008   | Configurar Room database inicial           | 🟡 Media  | `[ ]`       | S0-004      | E-01  |

**Milestone:** ✨ Proyecto base compilando con conexión Supabase

---

### Sprint 1 — Autenticación (Semana 2)

| ID       | Tarea                                       | Prioridad | Estado    | Dependencia | Épica |
|----------|---------------------------------------------|-----------|-----------|-------------|-------|
| S1-001   | Implementar LoginScreen (Compose + M3)      | 🔴 Alta   | `[x]`     | S0-005      | E-02  |
| S1-002   | Crear LoginViewModel + LoginUiState         | 🔴 Alta   | `[x]`     | S0-006      | E-02  |
| S1-003   | Implementar LoginUseCase                    | 🔴 Alta   | `[x]`     | S0-007      | E-02  |
| S1-004   | Implementar AuthRepository (Supabase Auth)  | 🔴 Alta   | `[x]`     | S0-006      | E-02  |
| S1-005   | Crear SplashScreen + verificar sesión       | 🟡 Media  | `[x]`     | S1-004      | E-02  |
| S1-006   | Implementar LogoutUseCase                   | 🟡 Media  | `[ ]`     | S1-004      | E-02  |
| S1-007   | Manejo de errores de autenticación          | 🟡 Media  | `[x]`     | S1-002      | E-02  |
| S1-008   | Configurar AuthNavigation                   | 🟡 Media  | `[x]`     | S1-001      | E-02  |
| S1-009   | Test unitarios LoginUseCase                 | 🟢 Baja   | `[ ]`     | S1-003      | E-02  |

**Milestone:** ✨ Login funcional con Supabase Auth

---

### Sprint 2 — Cartera de Clientes (Semana 3-4)

| ID       | Tarea                                        | Prioridad | Estado    | Dependencia | Épica |
|----------|----------------------------------------------|-----------|-----------|-------------|-------|
| S2-001   | Implementar CarteraScreen                    | 🔴 Alta   | `[ ]`     | S1-008      | E-03  |
| S2-002   | Crear ClienteCard composable                 | 🔴 Alta   | `[ ]`     | S0-005      | E-03  |
| S2-003   | Crear CarteraViewModel + CarteraUiState      | 🔴 Alta   | `[ ]`     | S0-007      | E-03  |
| S2-004   | Implementar GetCarteraDiariaUseCase          | 🔴 Alta   | `[ ]`     | S0-007      | E-03  |
| S2-005   | Implementar ClienteRepository (remote)       | 🔴 Alta   | `[ ]`     | S0-006      | E-03  |
| S2-006   | Implementar filtros por segmento             | 🟡 Media  | `[ ]`     | S2-003      | E-03  |
| S2-007   | Implementar ordenamiento por score           | 🟡 Media  | `[ ]`     | S2-003      | E-03  |
| S2-008   | Implementar búsqueda por nombre/DNI         | 🟡 Media  | `[ ]`     | S2-003      | E-03  |
| S2-009   | Crear estados de visita en tarjetas          | 🟡 Media  | `[ ]`     | S2-002      | E-03  |
| S2-010   | Implementar pull-to-refresh                  | 🟢 Baja   | `[ ]`     | S2-001      | E-03  |
| S2-011   | Crear DetalleClienteScreen                   | 🟡 Media  | `[ ]`     | S2-001      | E-03  |
| S2-012   | Room entities para clientes (offline cache)  | 🟡 Media  | `[ ]`     | S0-008      | E-07  |
| S2-013   | Test unitarios cartera use cases             | 🟢 Baja   | `[ ]`     | S2-004      | E-03  |

**Milestone:** ✨ Cartera diaria con clientes visibles y filtros operativos

---

### Sprint 3 — Ruta Diaria / Mapa (Semana 5)

| ID       | Tarea                                        | Prioridad | Estado    | Dependencia | Épica |
|----------|----------------------------------------------|-----------|-----------|-------------|-------|
| S3-001   | Integrar Google Maps SDK / MapBox            | 🔴 Alta   | `[ ]`     | S0-004      | E-04  |
| S3-002   | Implementar RutaScreen con mapa              | 🔴 Alta   | `[ ]`     | S3-001      | E-04  |
| S3-003   | Crear pins personalizados por segmento       | 🔴 Alta   | `[ ]`     | S3-002      | E-04  |
| S3-004   | Implementar resumen de cliente en pin info   | 🟡 Media  | `[ ]`     | S3-003      | E-04  |
| S3-005   | Crear RutaViewModel + RutaUiState            | 🔴 Alta   | `[ ]`     | S0-007      | E-04  |
| S3-006   | Cálculo de ruta óptima entre visitas         | 🟡 Media  | `[ ]`     | S3-005      | E-04  |
| S3-007   | Navegación desde pin a ficha de campo        | 🟡 Media  | `[ ]`     | S3-003      | E-04  |
| S3-008   | Mostrar estado de visita en pins             | 🟡 Media  | `[ ]`     | S3-003      | E-04  |
| S3-009   | Geolocalización del asesor (ubicación actual)| 🟢 Baja   | `[ ]`     | S3-001      | E-04  |
| S3-010   | Test de integración mapas                    | 🟢 Baja   | `[ ]`     | S3-002      | E-04  |

**Milestone:** ✨ Mapa con clientes geolocalizados y rutas calculadas

---

### Sprint 4 — Ficha de Campo (Semana 6-7)

| ID       | Tarea                                        | Prioridad | Estado    | Dependencia | Épica |
|----------|----------------------------------------------|-----------|-----------|-------------|-------|
| S4-001   | Implementar FichaScreen (wizard multi-paso)  | 🔴 Alta   | `[ ]`     | S2-011      | E-05  |
| S4-002   | Crear FichaF1Screen — Verificación negocio   | 🔴 Alta   | `[ ]`     | S4-001      | E-05  |
| S4-003   | Crear FichaF2Screen — Capacidad de pago      | 🔴 Alta   | `[ ]`     | S4-001      | E-05  |
| S4-004   | Crear FichaF3Screen — Deuda informal         | 🔴 Alta   | `[ ]`     | S4-001      | E-05  |
| S4-005   | Crear FichaF4Screen — Activos y respaldo     | 🔴 Alta   | `[ ]`     | S4-001      | E-05  |
| S4-006   | Crear FichaF5Screen — Carácter del cliente   | 🔴 Alta   | `[ ]`     | S4-001      | E-05  |
| S4-007   | Implementar FichaResumenScreen               | 🔴 Alta   | `[ ]`     | S4-006      | E-05  |
| S4-008   | Crear FichaViewModel + FichaUiState          | 🔴 Alta   | `[ ]`     | S0-007      | E-05  |
| S4-009   | Implementar CalcularScoreCampoUseCase        | 🔴 Alta   | `[ ]`     | S0-007      | E-06  |
| S4-010   | Implementar CalcularScoreFinalUseCase        | 🔴 Alta   | `[ ]`     | S4-009      | E-06  |
| S4-011   | Implementar CalcularCuotaUseCase             | 🟡 Media  | `[ ]`     | S4-010      | E-06  |
| S4-012   | Implementar DeterminarSegmentoUseCase        | 🟡 Media  | `[ ]`     | S4-010      | E-06  |
| S4-013   | Implementar validaciones por sección         | 🟡 Media  | `[ ]`     | S4-008      | E-05  |
| S4-014   | Implementar flujo de descalificación         | 🟡 Media  | `[ ]`     | S4-002      | E-05  |
| S4-015   | Guardar ficha en Room (offline)              | 🔴 Alta   | `[ ]`     | S0-008      | E-07  |
| S4-016   | Adjuntar fotos de visita                     | 🟢 Baja   | `[ ]`     | S4-001      | E-05  |
| S4-017   | Tests unitarios scoring use cases            | 🟡 Media  | `[ ]`     | S4-009      | E-06  |

**Milestone:** ✨ Ficha de campo completa F1-F5 con cálculo de scoring

---

### Sprint 5 — Offline & Sincronización (Semana 8)

| ID       | Tarea                                        | Prioridad | Estado    | Dependencia | Épica |
|----------|----------------------------------------------|-----------|-----------|-------------|-------|
| S5-001   | Diseñar schema Room completo                 | 🔴 Alta   | `[ ]`     | S0-008      | E-07  |
| S5-002   | Implementar DAOs para todas las entities     | 🔴 Alta   | `[ ]`     | S5-001      | E-07  |
| S5-003   | Crear SyncQueue (tabla + entidad)            | 🔴 Alta   | `[ ]`     | S5-001      | E-07  |
| S5-004   | Implementar SyncManager con WorkManager      | 🔴 Alta   | `[ ]`     | S5-003      | E-09  |
| S5-005   | Implementar NetworkMonitor (ConnectivityMgr) | 🔴 Alta   | `[ ]`     | S0-006      | E-09  |
| S5-006   | Implementar ConflictResolver (server-wins)   | 🟡 Media  | `[ ]`     | S5-004      | E-09  |
| S5-007   | Implementar retry policy con backoff         | 🟡 Media  | `[ ]`     | S5-004      | E-09  |
| S5-008   | Crear UUID local para entidades offline      | 🟡 Media  | `[ ]`     | S5-001      | E-07  |
| S5-009   | Implementar SyncDataUseCase                  | 🔴 Alta   | `[ ]`     | S5-004      | E-09  |
| S5-010   | Crear SyncStatusScreen (indicador visual)    | 🟡 Media  | `[ ]`     | S5-004      | E-09  |
| S5-011   | Tests de sincronización                      | 🟢 Baja   | `[ ]`     | S5-004      | E-09  |

**Milestone:** ✨ App funcional 100% offline con sincronización automática

---

### Sprint 6 — Historial de Visitas (Semana 9)

| ID       | Tarea                                        | Prioridad | Estado    | Dependencia | Épica |
|----------|----------------------------------------------|-----------|-----------|-------------|-------|
| S6-001   | Implementar HistorialScreen                  | 🔴 Alta   | `[ ]`     | S4-001      | E-08  |
| S6-002   | Crear HistorialViewModel + UiState           | 🔴 Alta   | `[ ]`     | S0-007      | E-08  |
| S6-003   | Implementar filtros (fecha, estado, segmento)| 🟡 Media  | `[ ]`     | S6-002      | E-08  |
| S6-004   | Implementar búsqueda por cliente             | 🟡 Media  | `[ ]`     | S6-002      | E-08  |
| S6-005   | Mostrar indicador de sync pendiente          | 🟡 Media  | `[ ]`     | S5-003      | E-08  |
| S6-006   | Detalle de visita pasada (read-only)         | 🟢 Baja   | `[ ]`     | S6-001      | E-08  |
| S6-007   | Paginación de historial                      | 🟢 Baja   | `[ ]`     | S6-001      | E-08  |

**Milestone:** ✨ Historial de visitas con filtros y estado de sincronización

---

### Sprint 7 — Sincronización Avanzada (Semana 10)

| ID       | Tarea                                        | Prioridad | Estado    | Dependencia | Épica |
|----------|----------------------------------------------|-----------|-----------|-------------|-------|
| S7-001   | Sync bidireccional completo                  | 🔴 Alta   | `[ ]`     | S5-004      | E-09  |
| S7-002   | Descarga incremental de cartera              | 🟡 Media  | `[ ]`     | S7-001      | E-09  |
| S7-003   | Upload de fichas completadas                 | 🔴 Alta   | `[ ]`     | S7-001      | E-09  |
| S7-004   | Notificaciones de sync exitoso/fallido       | 🟡 Media  | `[ ]`     | S7-001      | E-09  |
| S7-005   | Manejo de quota del plan gratuito Supabase   | 🟡 Media  | `[ ]`     | S7-001      | E-09  |

**Milestone:** ✨ Sincronización bidireccional robusta

---

### Sprint 8 — Documentos y Propuestas (Semana 11)

| ID       | Tarea                                        | Prioridad | Estado    | Dependencia | Épica |
|----------|----------------------------------------------|-----------|-----------|-------------|-------|
| S8-001   | Generar propuesta de crédito desde ficha     | 🔴 Alta   | `[ ]`     | S4-007      | E-10  |
| S8-002   | Enviar ficha al comité (cambio de estado)    | 🔴 Alta   | `[ ]`     | S8-001      | E-10  |
| S8-003   | Recibir resolución del comité                | 🟡 Media  | `[ ]`     | S8-002      | E-10  |
| S8-004   | Exportar resumen en PDF (futuro)             | 🟢 Baja   | `[ ]`     | S8-001      | E-10  |

**Milestone:** ✨ Flujo completo de propuesta → comité → resolución

---

### Sprint 9 — QA y Producción (Semana 12)

| ID       | Tarea                                        | Prioridad | Estado    | Dependencia | Épica |
|----------|----------------------------------------------|-----------|-----------|-------------|-------|
| S9-001   | Test end-to-end del flujo completo           | 🔴 Alta   | `[ ]`     | S8-002      | E-11  |
| S9-002   | Optimización de performance                  | 🟡 Media  | `[ ]`     | S9-001      | E-11  |
| S9-003   | Revisión de seguridad (RLS, tokens)          | 🔴 Alta   | `[ ]`     | S9-001      | E-11  |
| S9-004   | Fix bugs reportados en QA                    | 🔴 Alta   | `[ ]`     | S9-001      | E-11  |
| S9-005   | Configuración ProGuard para release          | 🟡 Media  | `[ ]`     | S9-004      | E-11  |
| S9-006   | Generar APK/AAB de release                   | 🔴 Alta   | `[ ]`     | S9-005      | E-11  |
| S9-007   | Documentación de deployment                  | 🟢 Baja   | `[ ]`     | S9-006      | E-11  |

**Milestone:** ✨ App lista para piloto en producción

---

## 4. Dependencias entre Sprints

```
Sprint 0 (Fundación)
  ├──► Sprint 1 (Auth)
  │       └──► Sprint 2 (Cartera)
  │               ├──► Sprint 3 (Ruta)
  │               └──► Sprint 4 (Ficha + Scoring)
  │                       ├──► Sprint 5 (Offline)
  │                       │       └──► Sprint 7 (Sync Avanzado)
  │                       ├──► Sprint 6 (Historial)
  │                       └──► Sprint 8 (Documentos)
  │                               └──► Sprint 9 (Producción)
```

---

## 5. Resumen de Estados

| Estado       | Símbolo  | Descripción                |
|--------------|----------|----------------------------|
| Pendiente    | `[ ]`    | No iniciado                |
| En progreso  | `[/]`    | Desarrollo activo          |
| Completado   | `[x]`    | Terminado y verificado     |
| Bloqueado    | `[!]`    | Bloqueado por dependencia  |

---

## 6. Prioridades

| Nivel    | Símbolo   | Criterio                                        |
|----------|-----------|-------------------------------------------------|
| Alta     | 🔴        | Bloquea otras tareas o es funcionalidad core    |
| Media    | 🟡        | Mejora significativa pero no bloquea            |
| Baja     | 🟢        | Nice-to-have, puede posponerse                  |

---

## 7. Métricas del Backlog

| Métrica                  | Valor   |
|--------------------------|---------|
| Total de tareas          | 82      |
| Sprints planificados     | 10 (0-9)|
| Épicas                   | 11      |
| Tareas críticas (🔴)     | 38      |
| Semanas estimadas        | 12      |
