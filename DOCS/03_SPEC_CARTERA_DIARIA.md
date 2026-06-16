# 03 — SPEC: CARTERA DIARIA

| Campo               | Valor                                                        |
|---------------------|--------------------------------------------------------------|
| **ID**              | SDD-03                                                       |
| **Sprint**          | Sprint 2 — Cartera de Clientes                               |
| **Estado**          | 📋 Especificado                                               |
| **Última revisión** | 2026-05-26                                                   |
| **Épica**           | E-03 Cartera de Clientes                                     |
| **Prioridad**       | 🔴 Alta                                                      |

---

## 1. Objetivo

Pantalla principal de la app que muestra la **lista de clientes preaprobados** asignados al asesor. Permite filtrar, ordenar y navegar hacia la ficha de campo o el mapa de rutas. Es el punto de partida operativo del asesor cada día.

---

## 2. Diseño UX / UI

### 2.1. Wireframe

```
┌─────────────────────────────────┐
│ ≡  Cartera Diaria      🔔  👤  │ ← TopAppBar
├─────────────────────────────────┤
│ 🔍 Buscar cliente o DNI...      │ ← SearchBar
├─────────────────────────────────┤
│ Hoy: 26/05/2026 · 45 clientes  │ ← Resumen
│ Visitados: 8 · Pendientes: 37  │
├─────────────────────────────────┤
│ [Todos][PREMIER][ESTÁNDAR]      │ ← Chips filtro
│ [BÁSICO][Visitados][Pendientes] │
├─────────────────────────────────┤
│ Ordenar: ▼ Score (mayor-menor)  │ ← Dropdown sort
├─────────────────────────────────┤
│ ┌───────────────────────────┐   │
│ │ 🟢 PREMIER  Score: 720    │   │ ← ClienteCard
│ │ Juan Pérez Mamani         │   │
│ │ DNI: 45678912             │   │
│ │ Bodega "Don Juan"         │   │
│ │ Monto hip: S/ 4,200       │   │
│ │ 📍 Jr. Real 423, Huancayo │   │
│ │ Estado: pendiente         │   │
│ │ [Ver Ficha]  [📍 Ubicación]│   │
│ └───────────────────────────┘   │
│                                 │
│ ┌───────────────────────────┐   │
│ │ 🔵 ESTÁNDAR  Score: 540   │   │
│ │ María López Torres        │   │
│ │ DNI: 78901234             │   │
│ │ Abarrotes "Doña María"    │   │
│ │ Monto hip: S/ 2,100       │   │
│ │ 📍 Av. Huancavelica 567   │   │
│ │ Estado: contactado        │   │
│ │ [Ver Ficha]  [📍 Ubicación]│   │
│ └───────────────────────────┘   │
│                                 │
│ ┌───────────────────────────┐   │
│ │ 🟠 BÁSICO  Score: 380     │   │
│ │ Pedro Quispe Huamán       │   │
│ │ ...                       │   │
│ └───────────────────────────┘   │
│                                 │
├──────────┬──────────┬───────────┤
│ 🏠 Cartera│ 🗺️ Ruta  │ 📊 Hist.  │ ← BottomNav
└──────────┴──────────┴───────────┘
```

### 2.2. Especificaciones de Diseño

| Elemento               | Especificación                                           |
|-------------------------|----------------------------------------------------------|
| TopAppBar               | SmallTopAppBar, `#004481`, título blanco                 |
| SearchBar               | OutlinedTextField con icono lupa, esquinas 24dp          |
| Chips de filtro         | FilterChip Material 3, scroll horizontal                 |
| Resumen del día         | Card sutil, fondo `#F4F4F4`, padding 12dp               |
| ClienteCard             | ElevatedCard, esquinas 16dp, elevación 2dp              |
| Badge de segmento       | Chip coloreado por segmento (ver paleta)                 |
| Score display           | Bold, tamaño 18sp, color del segmento                   |
| Botón "Ver Ficha"       | FilledTonalButton, `#1973B8`                             |
| Botón "Ubicación"       | OutlinedButton con icono pin                             |
| Estado de visita        | Label con color de estado                                |
| FAB                     | FloatingActionButton → Navegar a mapa ruta               |

### 2.3. Colores por Segmento

| Segmento        | Color Badge   | Color Fondo Card   |
|-----------------|---------------|---------------------|
| PREMIER         | `#1B5E20`     | `#E8F5E9`          |
| ESTÁNDAR        | `#1565C0`     | `#E3F2FD`          |
| BÁSICO          | `#F57C00`     | `#FFF3E0`          |
| NO_APLICA       | `#757575`     | `#FAFAFA`          |
| DESCALIFICADO   | `#D32F2F`     | `#FFEBEE`          |

### 2.4. Colores por Estado de Visita

| Estado              | Color     | Icono |
|---------------------|-----------|-------|
| preaprobado         | `#757575` | ⬜    |
| contactado          | `#1565C0` | 📞    |
| visita_agendada     | `#F57C00` | 📅    |
| visita_realizada    | `#1B5E20` | ✅    |
| en_comite           | `#7B1FA2` | 🏛️    |
| aprobado            | `#2E7D32` | ✅✅  |
| rechazado           | `#D32F2F` | ❌    |
| desembolsado        | `#004481` | 💰    |
| cancelado           | `#9E9E9E` | 🚫    |

---

## 3. Filtros y Ordenamiento

### 3.1. Filtros

| Filtro          | Tipo          | Opciones                                           |
|-----------------|---------------|-----------------------------------------------------|
| Segmento        | Multi-chip    | Todos, PREMIER, ESTÁNDAR, BÁSICO                   |
| Estado visita   | Multi-chip    | Pendientes, Visitados, En comité                    |
| Búsqueda        | Texto libre   | Nombre, apellido, DNI, nombre negocio               |
| Zona            | Dropdown      | Zonas asignadas al asesor                           |

### 3.2. Ordenamiento

| Criterio              | Dirección default |
|-----------------------|-------------------|
| Score (mayor-menor)   | ⬇️ Descendente    |
| Score (menor-mayor)   | ⬆️ Ascendente     |
| Nombre (A-Z)          | ⬆️ Ascendente     |
| Monto hipótesis       | ⬇️ Descendente    |
| Fecha preaprobación   | ⬇️ Descendente    |
| Distancia (más cerca) | ⬆️ Ascendente     |

---

## 4. Tarjeta de Cliente (ClienteCard)

### 4.1. Información Mostrada

| Dato                    | Fuente                              | Formato         |
|-------------------------|-------------------------------------|-----------------|
| Segmento preliminar     | scores_transaccionales              | Badge coloreado |
| Score transaccional     | scores_transaccionales              | "Score: 720"    |
| Nombre completo         | perfiles_clientes                   | Título card     |
| DNI                     | perfiles_clientes                   | "DNI: 45678912" |
| Nombre del negocio      | perfiles_clientes                   | Subtítulo       |
| Tipo de negocio         | perfiles_clientes                   | Label           |
| Monto hipótesis         | scores_transaccionales              | "S/ 4,200"      |
| Dirección               | perfiles_clientes                   | Con icono 📍    |
| Estado de visita        | creditos_preaprobados               | Label + color   |
| Última acción           | creditos_preaprobados               | Fecha relativa  |

### 4.2. Acciones por Tarjeta

| Acción           | Destino              | Condición                    |
|------------------|----------------------|------------------------------|
| Tap en tarjeta   | DetalleClienteScreen | Siempre disponible           |
| "Ver Ficha"      | FichaScreen          | Estado ≠ desembolsado/cancel |
| "📍 Ubicación"   | RutaScreen (centrado)| Si tiene coordenadas         |
| Long press        | Menu contextual      | Llamar, WhatsApp, Cancelar   |

---

## 5. Estados de la Pantalla

### 5.1. CarteraUiState

```kotlin
data class CarteraUiState(
    // Datos
    val clientes: List<ClientePreaprobado> = emptyList(),
    val clientesFiltrados: List<ClientePreaprobado> = emptyList(),

    // Filtros
    val searchQuery: String = "",
    val selectedSegmentos: Set<Segmento> = emptySet(),
    val selectedEstados: Set<EstadoVisita> = emptySet(),
    val selectedZona: String? = null,

    // Ordenamiento
    val sortBy: SortCriteria = SortCriteria.SCORE_DESC,

    // Resumen
    val totalClientes: Int = 0,
    val totalVisitados: Int = 0,
    val totalPendientes: Int = 0,
    val fechaActual: String = "",

    // Estado pantalla
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val isEmpty: Boolean = false,

    // Sync
    val lastSyncTime: String? = null,
    val hasPendingSync: Boolean = false
)

enum class SortCriteria {
    SCORE_DESC, SCORE_ASC, NAME_ASC,
    MONTO_DESC, FECHA_DESC, DISTANCIA_ASC
}
```

### 5.2. Diagrama de Estados

```
App abierta
     │
     ▼
┌──────────┐
│ LOADING  │ ──── cargar desde Room (instantáneo)
└────┬─────┘
     │
     ▼
┌──────────┐     pull-to-refresh    ┌──────────────┐
│ CONTENT  │ ─────────────────────► │  REFRESHING  │
│ (lista)  │ ◄───────────────────── │  (desde API) │
└────┬─────┘                        └──────────────┘
     │
     ├── filtro/búsqueda ──► CONTENT (filtrado)
     │
     └── sin resultados ──► EMPTY (mensaje + lottie)
```

---

## 6. Navegación

### 6.1. Entradas a la Pantalla

| Desde             | Acción                    |
|-------------------|---------------------------|
| LoginScreen       | Login exitoso             |
| SplashScreen      | Sesión válida             |
| BottomNavigation  | Tab "Cartera" seleccionado|

### 6.2. Salidas de la Pantalla

| Acción                   | Destino              |
|--------------------------|----------------------|
| Tap en ClienteCard       | DetalleClienteScreen |
| "Ver Ficha"              | FichaScreen          |
| "📍 Ubicación"           | RutaScreen           |
| FAB "Ver ruta"           | RutaScreen           |
| Tab "Ruta"               | RutaScreen           |
| Tab "Historial"          | HistorialScreen      |
| Icono perfil             | PerfilScreen         |

---

## 7. ViewModel

### 7.1. CarteraViewModel

```kotlin
@HiltViewModel
class CarteraViewModel @Inject constructor(
    private val getCarteraDiariaUseCase: GetCarteraDiariaUseCase,
    private val filterClientesUseCase: FilterClientesUseCase,
    private val sortClientesByScoreUseCase: SortClientesByScoreUseCase,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(CarteraUiState())
    val uiState: StateFlow<CarteraUiState> = _uiState.asStateFlow()

    init {
        loadCartera()
        observeNetworkStatus()
    }

    fun onSearchQueryChanged(query: String)
    fun onSegmentoFilterChanged(segmento: Segmento)
    fun onEstadoFilterChanged(estado: EstadoVisita)
    fun onZonaFilterChanged(zona: String?)
    fun onSortChanged(sort: SortCriteria)
    fun onRefresh()
    fun onClienteClicked(clienteId: UUID)
    fun onErrorDismissed()

    private fun loadCartera()
    private fun applyFiltersAndSort()
    private fun observeNetworkStatus()
}
```

---

## 8. Data Flow

```
[CarteraScreen]
     │ init
     ▼
[CarteraViewModel]
     │ getCarteraDiariaUseCase()
     ▼
[GetCarteraDiariaUseCase]
     │
     ├── Lee de Room (offline-first) ──► Flow<List<ClientePreaprobado>>
     │
     └── Si hay red, trigger sync en background
            │
            └── SyncManager → Supabase API → Room update → Flow emite nuevo valor
```

### 8.1. Flujo de Refresh

```
Pull-to-Refresh
     │
     ▼
[CarteraViewModel]
     │ clienteRepository.refreshFromRemote()
     ▼
[ClienteRepositoryImpl]
     │
     ├── GET /rest/v1/scores_transaccionales?select=*,perfiles_clientes(*)
     │
     ├── Mapear DTOs → Domain models
     │
     ├── Guardar en Room (insertOrUpdate)
     │
     └── Flow emite automáticamente → UI se actualiza
```

---

## 9. Casos Edge

| Caso                             | Comportamiento                                     |
|----------------------------------|----------------------------------------------------|
| Sin clientes asignados           | Empty state con ilustración y mensaje              |
| Sin internet + cache vacía       | Error state con botón reintentar                   |
| Sin internet + cache existente   | Mostrar cache + banner "Sin conexión"              |
| Filtros sin resultados           | "No hay clientes que coincidan con los filtros"    |
| +500 clientes en cartera         | LazyColumn con paginación local                    |
| Scroll rápido                    | Lazy loading con placeholder shimmer               |
| Dato de scoring incompleto       | Mostrar "Score: —" en gris                         |
| Cliente sin coordenadas          | Ocultar botón "📍 Ubicación"                       |
| Cambio de zona del asesor        | Refresh completo en próximo sync                    |
| Sesión expirada durante refresh  | Redirigir a LoginScreen                            |

---

## 10. Criterios de Aceptación

- [ ] La pantalla carga la lista de clientes desde Room al abrir
- [ ] Pull-to-refresh sincroniza con Supabase y actualiza la lista
- [ ] Los filtros por segmento funcionan correctamente (multi-selección)
- [ ] El ordenamiento por score ordena de mayor a menor por defecto
- [ ] La búsqueda filtra por nombre, DNI y nombre de negocio
- [ ] Cada tarjeta muestra: segmento, score, nombre, DNI, negocio, monto, dirección, estado
- [ ] Los colores de las tarjetas corresponden al segmento del cliente
- [ ] Tap en tarjeta navega a DetalleClienteScreen
- [ ] "Ver Ficha" navega a FichaScreen con el clienteId correcto
- [ ] "📍 Ubicación" navega a RutaScreen centrado en el cliente
- [ ] El resumen del día muestra totales correctos
- [ ] El estado de sincronización se muestra en la parte superior
- [ ] La app no crashea con listas grandes (+500 items)
- [ ] La pantalla funciona correctamente sin internet (datos de Room)
- [ ] Las animaciones de las tarjetas son fluidas al hacer scroll
