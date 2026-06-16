# 00 — PROJECT OVERVIEW

| Campo               | Valor                                                        |
|---------------------|--------------------------------------------------------------|
| **ID**              | SDD-00                                                       |
| **Sprint**          | Sprint 0 — Fundación                                         |
| **Estado**          | ✅ Completo                                                   |
| **Última revisión** | 2026-05-26                                                   |
| **Autor**           | Equipo Arquitectura                                          |

---

## 1. Visión General

**BBVA Fuerza de Ventas** es una aplicación Android enterprise diseñada para **asesores de crédito** de un banco. Permite gestionar el ciclo completo de evaluación crediticia de clientes preaprobados mediante visitas de campo.

### Propósito
Dotar a la fuerza de ventas de una herramienta móvil que permita:
- Descargar y gestionar cartera de clientes preaprobados
- Planificar y ejecutar rutas de visitas de campo
- Evaluar scoring crediticio en tiempo real
- Registrar fichas de evaluación completas (F1-F5)
- Generar propuestas de crédito con cálculo automático
- Operar **sin conexión a internet** durante las visitas
- Sincronizar datos cuando se recupere la conectividad

### Público Objetivo
- **Usuarios primarios:** Asesores de negocios (Junior I, Junior II, Senior I, Senior II)
- **Usuarios secundarios:** Jefes de agencia (revisión y comité)
- **Stakeholders:** Gerencia de créditos, Riesgos, TI

### Diferenciadores Clave
> ⚠️ Esta NO es una app bancaria tradicional. Es una app de **fuerza de ventas**, **scoring financiero**, **visitas de campo** y **evaluación crediticia**.

- Enfoque **operativo y de productividad**
- Diseño **offline-first** obligatorio
- Interfaz **rápida y orientada a tareas**
- Independiente de la app bancaria del cliente

---

## 2. Stack Tecnológico

| Capa               | Tecnología                          | Versión / Notas               |
|---------------------|-------------------------------------|-------------------------------|
| Plataforma          | Android                            | minSdk 24 · targetSdk 34     |
| Lenguaje            | Kotlin                             | 1.9+                         |
| UI Framework        | Jetpack Compose                    | Material 3                   |
| Arquitectura        | MVVM + Clean Architecture ligera   | —                            |
| Backend / BaaS      | Supabase                           | Plan gratuito (desarrollo)   |
| Base de datos       | PostgreSQL (Supabase)              | Remota                       |
| Base de datos local | Room                               | Offline-first                |
| Networking          | Ktor Client                        | HTTP + JSON                  |
| Async               | Coroutines + Flow                  | StateFlow en ViewModels      |
| Mapas               | Google Maps SDK / MapBox           | Rutas y geolocalización      |
| Metodología         | Spec Driven Development (SDD)     | Docs → Code                  |
| DI                  | Hilt (Dagger)                      | Inyección de dependencias    |
| Serialización       | Kotlinx Serialization              | JSON mapping                 |
| Imágenes            | Coil                               | Carga async de imágenes      |
| Testing             | JUnit + Compose Testing            | Unit + UI tests              |

---

## 3. Arquitectura del Proyecto

### 3.1. Estructura de Carpetas

```
app/
├── src/main/java/com/example/bbvafuerzadeventas/
│   │
│   ├── core/                          # Módulo transversal
│   │   ├── network/                   # Configuración Supabase/Ktor
│   │   │   ├── SupabaseClient.kt
│   │   │   ├── NetworkMonitor.kt
│   │   │   └── ApiConstants.kt
│   │   ├── ui/                        # Componentes UI reutilizables
│   │   │   ├── components/
│   │   │   ├── theme/
│   │   │   └── animations/
│   │   ├── designsystem/              # Tokens de diseño
│   │   │   ├── Colors.kt
│   │   │   ├── Typography.kt
│   │   │   └── Spacing.kt
│   │   ├── navigation/                # Navegación global
│   │   │   ├── AppNavHost.kt
│   │   │   └── Routes.kt
│   │   ├── common/                    # Extensiones, constantes
│   │   └── utils/                     # Utilidades generales
│   │       ├── DateUtils.kt
│   │       ├── FormatUtils.kt
│   │       └── ValidationUtils.kt
│   │
│   ├── domain/                        # Capa de dominio (pura)
│   │   ├── model/                     # Modelos de dominio
│   │   │   ├── Cliente.kt
│   │   │   ├── FichaCampo.kt
│   │   │   ├── ScoreTransaccional.kt
│   │   │   ├── ScoreCampo.kt
│   │   │   ├── CreditoPreaprobado.kt
│   │   │   ├── Asesor.kt
│   │   │   ├── Agencia.kt
│   │   │   └── Visita.kt
│   │   ├── usecase/                   # Casos de uso
│   │   │   ├── auth/
│   │   │   │   ├── LoginUseCase.kt
│   │   │   │   ├── LogoutUseCase.kt
│   │   │   │   └── GetSessionUseCase.kt
│   │   │   ├── cartera/
│   │   │   │   ├── GetCarteraDiariaUseCase.kt
│   │   │   │   ├── FilterClientesUseCase.kt
│   │   │   │   └── SortClientesByScoreUseCase.kt
│   │   │   ├── scoring/
│   │   │   │   ├── CalcularScoreCampoUseCase.kt
│   │   │   │   ├── CalcularScoreFinalUseCase.kt
│   │   │   │   ├── CalcularCuotaUseCase.kt
│   │   │   │   └── DeterminarSegmentoUseCase.kt
│   │   │   ├── ficha/
│   │   │   │   ├── CrearFichaCampoUseCase.kt
│   │   │   │   ├── ActualizarFichaUseCase.kt
│   │   │   │   └── ValidarFichaUseCase.kt
│   │   │   ├── visita/
│   │   │   │   ├── IniciarVisitaUseCase.kt
│   │   │   │   ├── FinalizarVisitaUseCase.kt
│   │   │   │   └── DescalificarClienteUseCase.kt
│   │   │   └── sync/
│   │   │       ├── SyncDataUseCase.kt
│   │   │       └── GetPendingSyncUseCase.kt
│   │   └── repository/                # Interfaces de repositorio
│   │       ├── AuthRepository.kt
│   │       ├── ClienteRepository.kt
│   │       ├── FichaCampoRepository.kt
│   │       ├── VisitaRepository.kt
│   │       └── SyncRepository.kt
│   │
│   ├── data/                          # Capa de datos
│   │   ├── remote/                    # Supabase API
│   │   │   ├── dto/                   # Data Transfer Objects
│   │   │   ├── api/                   # Servicios remotos
│   │   │   └── mapper/                # DTO ↔ Domain mappers
│   │   ├── local/                     # Room database
│   │   │   ├── database/
│   │   │   │   └── AppDatabase.kt
│   │   │   ├── dao/                   # Data Access Objects
│   │   │   ├── entity/                # Room entities
│   │   │   └── mapper/                # Entity ↔ Domain mappers
│   │   └── repository/                # Implementaciones
│   │       ├── AuthRepositoryImpl.kt
│   │       ├── ClienteRepositoryImpl.kt
│   │       ├── FichaCampoRepositoryImpl.kt
│   │       ├── VisitaRepositoryImpl.kt
│   │       └── SyncRepositoryImpl.kt
│   │
│   ├── feature_auth/                  # Feature: Autenticación
│   │   ├── presentation/
│   │   │   ├── ui/
│   │   │   │   └── LoginScreen.kt
│   │   │   ├── viewmodel/
│   │   │   │   └── LoginViewModel.kt
│   │   │   └── state/
│   │   │       └── LoginUiState.kt
│   │   └── navigation/
│   │       └── AuthNavigation.kt
│   │
│   ├── feature_cartera/               # Feature: Cartera Diaria
│   │   ├── presentation/
│   │   │   ├── ui/
│   │   │   │   ├── CarteraScreen.kt
│   │   │   │   └── ClienteCard.kt
│   │   │   ├── viewmodel/
│   │   │   │   └── CarteraViewModel.kt
│   │   │   └── state/
│   │   │       └── CarteraUiState.kt
│   │   └── navigation/
│   │       └── CarteraNavigation.kt
│   │
│   ├── feature_ruta/                  # Feature: Ruta/Mapa
│   │   ├── presentation/
│   │   │   ├── ui/
│   │   │   │   ├── RutaScreen.kt
│   │   │   │   └── MapOverlay.kt
│   │   │   ├── viewmodel/
│   │   │   │   └── RutaViewModel.kt
│   │   │   └── state/
│   │   │       └── RutaUiState.kt
│   │   └── navigation/
│   │       └── RutaNavigation.kt
│   │
│   ├── feature_ficha/                 # Feature: Ficha de Campo
│   │   ├── presentation/
│   │   │   ├── ui/
│   │   │   │   ├── FichaScreen.kt
│   │   │   │   ├── FichaF1Screen.kt
│   │   │   │   ├── FichaF2Screen.kt
│   │   │   │   ├── FichaF3Screen.kt
│   │   │   │   ├── FichaF4Screen.kt
│   │   │   │   ├── FichaF5Screen.kt
│   │   │   │   └── FichaResumenScreen.kt
│   │   │   ├── viewmodel/
│   │   │   │   └── FichaViewModel.kt
│   │   │   └── state/
│   │   │       └── FichaUiState.kt
│   │   └── navigation/
│   │       └── FichaNavigation.kt
│   │
│   ├── feature_historial/             # Feature: Historial
│   │   ├── presentation/
│   │   │   ├── ui/
│   │   │   │   └── HistorialScreen.kt
│   │   │   ├── viewmodel/
│   │   │   │   └── HistorialViewModel.kt
│   │   │   └── state/
│   │   │       └── HistorialUiState.kt
│   │   └── navigation/
│   │       └── HistorialNavigation.kt
│   │
│   └── feature_sync/                  # Feature: Sincronización
│       ├── presentation/
│       │   ├── ui/
│       │   │   └── SyncStatusScreen.kt
│       │   └── viewmodel/
│       │       └── SyncViewModel.kt
│       ├── worker/
│       │   └── SyncWorker.kt
│       └── navigation/
│           └── SyncNavigation.kt
│
├── src/main/res/                      # Recursos Android
└── src/test/                          # Tests unitarios
```

### 3.2. Diagrama de Capas

```
┌─────────────────────────────────────────────────────┐
│                    PRESENTATION                      │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌───────────┐ │
│  │  Auth    │ │ Cartera │ │  Ruta   │ │   Ficha   │ │
│  │ Feature  │ │ Feature │ │ Feature │ │  Feature  │ │
│  └────┬─────┘ └────┬────┘ └────┬────┘ └─────┬─────┘ │
│       │            │           │             │       │
│  ┌────┴────────────┴───────────┴─────────────┴────┐  │
│  │              ViewModels (StateFlow)             │  │
│  └────────────────────┬───────────────────────────┘  │
├───────────────────────┼──────────────────────────────┤
│                  DOMAIN LAYER                        │
│  ┌────────────────────┴───────────────────────────┐  │
│  │              Use Cases (suspend fun)            │  │
│  ├─────────────────────────────────────────────────┤  │
│  │          Repository Interfaces                  │  │
│  ├─────────────────────────────────────────────────┤  │
│  │          Domain Models (data class)             │  │
│  └────────────────────┬───────────────────────────┘  │
├───────────────────────┼──────────────────────────────┤
│                   DATA LAYER                         │
│  ┌────────────┐       │       ┌───────────────────┐  │
│  │   Remote   │◄──────┴──────►│      Local        │  │
│  │  Supabase  │               │      Room         │  │
│  │  (Ktor)    │               │   (SQLite)        │  │
│  └────────────┘               └───────────────────┘  │
│       ↕                              ↕               │
│  ┌────────────────────────────────────────────────┐   │
│  │          Repository Implementations             │  │
│  │     (Offline-first: Room → Supabase sync)       │  │
│  └─────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────┘
```

---

## 4. Módulos del Sistema

### 4.1. Módulos Feature

| Módulo              | Descripción                                            | Sprint  |
|---------------------|--------------------------------------------------------|---------|
| `feature_auth`      | Login/logout de asesores, manejo de sesión             | Sprint 1|
| `feature_cartera`   | Lista de clientes preaprobados, filtros, ordenamiento  | Sprint 2|
| `feature_ruta`      | Mapa con rutas, pins por segmento, navegación GPS      | Sprint 3|
| `feature_ficha`     | Ficha de evaluación F1-F5, scoring, propuesta          | Sprint 4|
| `feature_historial` | Historial de visitas, filtros, búsqueda                | Sprint 6|
| `feature_sync`      | Sincronización offline/online, cola de sync            | Sprint 5|

### 4.2. Módulos Core

| Módulo              | Responsabilidad                                        |
|---------------------|--------------------------------------------------------|
| `core/network`      | Configuración Supabase, Ktor client, interceptors      |
| `core/ui`           | Componentes UI compartidos (cards, loaders, dialogs)   |
| `core/designsystem` | Tokens de diseño: colores BBVA, tipografía, spacing    |
| `core/navigation`   | NavHost central, rutas, deep links                     |
| `core/common`       | Extensiones, constantes, Result wrapper                |
| `core/utils`        | Formateo de moneda, fechas, validaciones               |

---

## 5. Navegación General

### 5.1. Flujo de Navegación Principal

```
┌────────────┐     ┌──────────────┐     ┌──────────────┐
│   SPLASH   │────►│    LOGIN     │────►│   CARTERA    │
│            │     │              │     │   DIARIA     │
└────────────┘     └──────────────┘     └──────┬───────┘
                                               │
                          ┌────────────────────┼────────────────────┐
                          │                    │                    │
                   ┌──────▼──────┐     ┌──────▼──────┐     ┌──────▼──────┐
                   │  RUTA MAPA  │     │   FICHA     │     │  HISTORIAL  │
                   │             │     │   CAMPO     │     │             │
                   └──────┬──────┘     └──────┬──────┘     └─────────────┘
                          │                   │
                          │            ┌──────▼──────┐
                          └───────────►│  DETALLE    │
                                       │  CLIENTE    │
                                       └──────┬──────┘
                                              │
                                       ┌──────▼──────┐
                                       │  DESCALIF.  │
                                       │  / PROPUESTA│
                                       └─────────────┘
```

### 5.2. Bottom Navigation

```
┌─────────────────────────────────────────────────────┐
│                    APP CONTENT                       │
├──────────┬──────────┬──────────┬──────────┬─────────┤
│ 🏠       │ 🗺️       │ 📋       │ 📊       │ 🔄      │
│ Cartera  │  Ruta    │ Ficha    │ Historial│  Sync   │
└──────────┴──────────┴──────────┴──────────┴─────────┘
```

### 5.3. Rutas (Route Definitions)

| Route                     | Screen               | Args                    |
|---------------------------|-----------------------|-------------------------|
| `auth/login`              | LoginScreen           | —                       |
| `cartera`                 | CarteraScreen         | —                       |
| `cartera/{clienteId}`     | DetalleClienteScreen  | clienteId: UUID         |
| `ruta`                    | RutaScreen            | —                       |
| `ruta/{clienteId}`        | RutaNavScreen         | clienteId: UUID         |
| `ficha/{clienteId}`       | FichaScreen           | clienteId: UUID         |
| `ficha/{clienteId}/f{n}`  | FichaFnScreen         | clienteId, n: 1-5       |
| `ficha/{fichaId}/resumen` | FichaResumenScreen    | fichaId: UUID           |
| `historial`               | HistorialScreen       | —                       |
| `sync`                    | SyncStatusScreen      | —                       |

---

## 6. Estrategia Offline-First

### 6.1. Principios

1. **Room como fuente de verdad** — La UI siempre lee de Room, nunca directamente de la red
2. **Escrituras locales primero** — Toda escritura se guarda en Room inmediatamente
3. **Cola de sincronización** — Los cambios pendientes se encolan en `sync_queue`
4. **Sync oportunista** — Cuando hay conectividad, se sincronizan los cambios automáticamente
5. **Resolución de conflictos** — Server wins con merge inteligente para campos no conflictivos

### 6.2. Flujo de Datos

```
[UI] ──read──► [Room DB] ◄──sync──► [Supabase]
  │                ▲                      ▲
  └──write──►  [Room DB]                  │
                   │                      │
                   └──► [Sync Queue] ─────┘
                          (cuando hay red)
```

### 6.3. Componentes Offline

| Componente        | Responsabilidad                                    |
|-------------------|----------------------------------------------------|
| `AppDatabase`     | Room database con todas las entities               |
| `SyncQueue`       | Cola FIFO de operaciones pendientes                |
| `SyncManager`     | Orquesta la sincronización con WorkManager         |
| `NetworkMonitor`  | Observa conectividad (ConnectivityManager + Flow)  |
| `ConflictResolver`| Resuelve conflictos server-wins + merge            |

---

## 7. Integración Supabase

### 7.1. Configuración

| Parámetro   | Valor                                                                     |
|-------------|---------------------------------------------------------------------------|
| Project ID  | `srxoisgexbcifdpwetxo`                                                   |
| API URL     | `https://srxoisgexbcifdpwetxo.supabase.co`                               |
| Anon Key    | `sb_publishable_lYyLWaJxbM-lCJ3eH_wrgg_t-UnR_lC`                         |

### 7.2. Servicios Utilizados

| Servicio        | Uso                                                |
|-----------------|----------------------------------------------------|
| **Auth**        | Autenticación de asesores (email/password)          |
| **Database**    | Tablas PostgreSQL con RLS                          |
| **Storage**     | Fotos de visitas de campo (futuro)                 |
| **Realtime**    | Notificaciones de cambios en cartera (futuro)      |

### 7.3. Tablas Principales

| Tabla                       | Descripción                                  |
|-----------------------------|----------------------------------------------|
| `agencias`                  | 30 agencias distribuidas en 5 regiones       |
| `asesores_negocio`          | 360 asesores con niveles y metas             |
| `perfiles_clientes`         | Datos demográficos y de negocio del cliente  |
| `cuentas`                   | Cuentas bancarias de clientes                |
| `transacciones`             | Movimientos transaccionales                  |
| `movimientos_mensuales`     | Agregados mensuales para scoring             |
| `features_scoring`          | Features calculados por cliente              |
| `scores_transaccionales`    | Score transaccional (800 pts)                |
| `fichas_campo`              | Ficha de evaluación F1-F5 (200 pts)          |
| `creditos_preaprobados`     | Créditos aprobados con seguimiento           |

---

## 8. Reglas Generales de Negocio

### 8.1. Score del Sistema

El scoring es **híbrido** con dos componentes:

| Componente              | Puntaje Máximo | Fuente              |
|-------------------------|----------------|---------------------|
| Score Transaccional     | 800 pts        | Datos del sistema   |
| Score de Campo          | 200 pts        | Visita del asesor   |
| **Score Final**         | **1000 pts**   | Suma de ambos       |

### 8.2. Segmentos

| Segmento        | Score Final    | Monto Máximo | Característica         |
|-----------------|----------------|--------------|------------------------|
| PREMIER         | ≥ 750          | S/ 5,000     | Mejores condiciones    |
| ESTÁNDAR        | 550 – 749      | S/ 2,500     | Condiciones normales   |
| BÁSICO          | 350 – 549      | S/ 1,000     | Montos conservadores   |
| NO_APLICA       | < 350          | —            | No califica            |
| DESCALIFICADO   | —              | —            | Veto o no verificado   |

### 8.3. Niveles de Asesores

| Nivel       | Cartera Promedio | Meta Créditos/Mes | Meta Monto/Mes  |
|-------------|------------------|--------------------|-----------------|
| Senior II   | 400 clientes     | 16 créditos        | S/ 28,800       |
| Senior I    | 300 clientes     | 12 créditos        | S/ 21,600       |
| Junior II   | 180 clientes     | 7 créditos         | S/ 12,600       |
| Junior I    | 90 clientes      | 4 créditos         | S/ 7,200        |

### 8.4. Reglas de Descalificación

Un cliente es **DESCALIFICADO** si:
1. Negocio no verificado / no encontrado
2. Carácter del cliente = **veto**
3. Score final < 350 (NO_APLICA)

### 8.5. Estados de una Visita

```
preaprobado → contactado → visita_agendada → visita_realizada
    → en_comite → aprobado → desembolsado
    → rechazado
    → cancelado
```

---

## 9. Design System (BBVA Brand)

### 9.1. Paleta de Colores

| Token                  | Color       | Uso                           |
|------------------------|-------------|-------------------------------|
| `primary`              | `#004481`   | BBVA Blue principal           |
| `primaryDark`          | `#002A4D`   | AppBar, elementos destacados  |
| `secondary`            | `#1973B8`   | Acciones secundarias          |
| `accent`               | `#5BBEFF`   | Highlights, badges            |
| `surface`              | `#F4F4F4`   | Fondos de tarjetas            |
| `background`           | `#FFFFFF`   | Fondo principal               |
| `error`                | `#D32F2F`   | Errores, descalificaciones    |
| `success`              | `#2E7D32`   | Aprobado, verificado          |
| `warning`              | `#F57C00`   | Alertas, pendientes           |
| `segmentoPremier`      | `#1B5E20`   | Verde oscuro                  |
| `segmentoEstandar`     | `#1565C0`   | Azul medio                    |
| `segmentoBasico`       | `#F57C00`   | Naranja                       |
| `segmentoNoAplica`     | `#757575`   | Gris                          |
| `segmentoDescalif`     | `#D32F2F`   | Rojo                          |

### 9.2. Tipografía

- **Display:** Roboto Bold 28sp
- **Title:** Roboto Medium 22sp
- **Subtitle:** Roboto Medium 16sp
- **Body:** Roboto Regular 14sp
- **Caption:** Roboto Regular 12sp
- **Label:** Roboto Medium 11sp

### 9.3. Spacing

| Token  | Valor  |
|--------|--------|
| xs     | 4dp    |
| sm     | 8dp    |
| md     | 16dp   |
| lg     | 24dp   |
| xl     | 32dp   |
| xxl    | 48dp   |

---

## 10. Criterios de Aceptación del Proyecto

- [ ] Todos los 11 documentos SDD generados y consistentes
- [ ] Base de datos Supabase con todas las tablas, funciones y vistas
- [ ] Seed data cargado (30 agencias, 360 asesores)
- [ ] Proyecto Android compila sin errores
- [ ] Arquitectura MVVM + Clean implementada
- [ ] Offline-first funcional con Room
- [ ] Scoring engine implementado en domain/usecases
- [ ] Login con Supabase Auth operativo
- [ ] Cartera diaria con filtros y ordenamiento
- [ ] Ficha de campo F1-F5 completa con cálculos
- [ ] Sincronización básica operativa
