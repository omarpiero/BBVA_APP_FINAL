# 07 — SPEC: HISTORIAL DE VISITAS

| Campo               | Valor                                                        |
|---------------------|--------------------------------------------------------------|
| **ID**              | SDD-07                                                       |
| **Sprint**          | Sprint 6 — Historial & Reportes                              |
| **Estado**          | 📋 Especificado                                               |
| **Última revisión** | 2026-05-26                                                   |
| **Épica**           | E-08 Historial & Reportes                                    |
| **Prioridad**       | 🟡 Media                                                     |

---

## 1. Objetivo

Pantalla que muestra el **historial completo de visitas** realizadas por el asesor. Permite filtrar por fecha, estado, segmento y nombre de cliente. Incluye indicadores de **sincronización pendiente** para fichas guardadas offline.

---

## 2. Diseño UX / UI

### 2.1. Wireframe

```
┌─────────────────────────────────┐
│ ≡  Historial de Visitas    🔍   │ ← TopAppBar
├─────────────────────────────────┤
│ 🔍 Buscar por cliente...        │ ← SearchBar
├─────────────────────────────────┤
│ Filtros:                        │
│ [Todos][Completadas][Pendientes]│
│ [En comité][Descalificadas]     │
│                                 │
│ Periodo: [Esta semana ▼]        │
├─────────────────────────────────┤
│ 📊 Resumen: 45 visitas          │
│ ✅ 28 completadas · ⏳ 12 pend. │
│ 🚫 5 descalif. · 🔄 3 sin sync  │
├─────────────────────────────────┤
│                                 │
│ ── Hoy, 26 May 2026 ──         │ ← Separador por fecha
│                                 │
│ ┌───────────────────────────┐   │
│ │ ✅ Juan Pérez · PREMIER    │   │
│ │ Score: 800 · S/ 4,200      │   │
│ │ 10:30 - 11:15 · Completada │   │
│ │ 🟢 Sincronizado            │   │
│ └───────────────────────────┘   │
│                                 │
│ ┌───────────────────────────┐   │
│ │ 🚫 Pedro Quispe · DESCAL.  │   │
│ │ Motivo: Negocio no encontr.│   │
│ │ 11:30 · Cancelada           │   │
│ │ 🔄 Pendiente de sync       │   │
│ └───────────────────────────┘   │
│                                 │
│ ┌───────────────────────────┐   │
│ │ ⏳ María López · ESTÁNDAR  │   │
│ │ Score: 540 · S/ 2,100      │   │
│ │ 14:00 · En progreso        │   │
│ │ 💾 Guardado offline         │   │
│ └───────────────────────────┘   │
│                                 │
│ ── Ayer, 25 May 2026 ──        │
│ ...                             │
│                                 │
├──────────┬──────────┬───────────┤
│ 🏠 Cartera│ 🗺️ Ruta  │ 📊 Hist.  │
└──────────┴──────────┴───────────┘
```

### 2.2. Especificaciones de Diseño

| Elemento               | Especificación                                          |
|-------------------------|---------------------------------------------------------|
| TopAppBar               | SmallTopAppBar, `#004481`                               |
| SearchBar               | OutlinedTextField con icono lupa                        |
| Chips de filtro         | FilterChip Material 3, scroll horizontal                |
| Periodo selector        | ExposedDropdownMenu                                     |
| Separadores de fecha    | Sticky header con fecha formateada                      |
| VisitaCard              | ElevatedCard con icono de estado                        |
| Badge sync              | Chip pequeño: "🟢 Sincronizado" / "🔄 Pendiente"       |
| Resumen                 | Card con estadísticas del periodo                       |
| Empty state             | Ilustración Lottie + "No hay visitas en este periodo"   |

---

## 3. Filtros

### 3.1. Filtros Disponibles

| Filtro          | Tipo          | Opciones                                              |
|-----------------|---------------|--------------------------------------------------------|
| Estado ficha    | Multi-chip    | Todos, Completadas, En progreso, Canceladas            |
| Estado crédito  | Multi-chip    | Pendientes, En comité, Aprobados, Descalificados       |
| Segmento        | Multi-chip    | PREMIER, ESTÁNDAR, BÁSICO, NO_APLICA                  |
| Periodo         | Dropdown      | Hoy, Esta semana, Este mes, Último mes, Personalizado  |
| Sync status     | Chip          | Todos, Pendiente de sync, Sincronizados                |
| Búsqueda        | Texto libre   | Nombre, DNI, nombre negocio                            |

### 3.2. Periodo Personalizado

```
┌──────────────────────────┐
│ Seleccionar periodo       │
│                          │
│ Desde: [📅 01/05/2026]   │
│ Hasta: [📅 26/05/2026]   │
│                          │
│ [Cancelar]  [Aplicar]    │
└──────────────────────────┘
```

---

## 4. Búsqueda

### 4.1. Campos de Búsqueda

| Campo              | Coincidencia | Ejemplo                    |
|--------------------|--------------|----------------------------|
| Nombre             | Contiene     | "Juan" → Juan Pérez        |
| Apellido           | Contiene     | "Mamani" → Pérez Mamani    |
| DNI                | Exacto       | "45678912"                 |
| Nombre negocio     | Contiene     | "bodega" → Bodega Don Juan |

### 4.2. Comportamiento

| Acción                        | Resultado                              |
|-------------------------------|----------------------------------------|
| Escribir texto (≥ 2 chars)    | Filtro inmediato (debounce 300ms)      |
| Limpiar búsqueda              | Mostrar todos (con otros filtros)      |
| Sin resultados                | Empty state "No se encontraron visitas"|

---

## 5. Estados

### 5.1. HistorialUiState

```kotlin
data class HistorialUiState(
    // Datos
    val visitas: List<VisitaHistorial> = emptyList(),
    val visitasFiltradas: List<VisitaHistorial> = emptyList(),
    val visitasAgrupadas: Map<String, List<VisitaHistorial>> = emptyMap(),

    // Filtros
    val searchQuery: String = "",
    val selectedEstados: Set<EstadoFicha> = emptySet(),
    val selectedSegmentos: Set<Segmento> = emptySet(),
    val selectedPeriodo: Periodo = Periodo.ESTA_SEMANA,
    val fechaDesde: LocalDate? = null,
    val fechaHasta: LocalDate? = null,
    val syncFilter: SyncFilter = SyncFilter.TODOS,

    // Resumen
    val totalVisitas: Int = 0,
    val completadas: Int = 0,
    val enProgreso: Int = 0,
    val descalificadas: Int = 0,
    val pendienteSync: Int = 0,

    // Estado pantalla
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEmpty: Boolean = false
)

data class VisitaHistorial(
    val fichaId: UUID,
    val clienteId: UUID,
    val clienteNombre: String,
    val clienteNegocio: String,
    val segmento: String,
    val scoreFinal: Int?,
    val montoAprobado: Double?,
    val fechaVisita: LocalDate,
    val horaInicio: LocalTime?,
    val horaFin: LocalTime?,
    val estadoFicha: String,
    val estadoCredito: String?,
    val recomendacion: String?,
    val syncStatus: SyncStatus,
    val motivoDescalificacion: String?
)

enum class Periodo {
    HOY, ESTA_SEMANA, ESTE_MES, ULTIMO_MES, PERSONALIZADO
}

enum class SyncFilter {
    TODOS, PENDIENTE, SINCRONIZADO
}

enum class SyncStatus {
    SYNCED, PENDING, CONFLICT, ERROR
}
```

---

## 6. Tarjeta de Visita (VisitaCard)

### 6.1. Información Mostrada

| Dato                    | Display                                     |
|-------------------------|---------------------------------------------|
| Estado (icono)          | ✅ ⏳ 🚫 🏛️ según estado                      |
| Nombre cliente          | Título de la tarjeta                        |
| Segmento               | Badge coloreado                             |
| Score final             | "Score: 800" (si completada)                |
| Monto aprobado          | "S/ 4,200" (si aprobado)                    |
| Hora inicio - fin       | "10:30 - 11:15"                             |
| Estado de la ficha      | Label con color                             |
| Recomendación           | Solo si completada                          |
| Sync status             | 🟢 Sincronizado / 🔄 Pendiente / ⚠️ Error   |
| Motivo descalificación  | Solo si descalificada (1 línea truncada)    |

### 6.2. Acciones

| Acción         | Comportamiento                              |
|----------------|---------------------------------------------|
| Tap en card    | Navegar a detalle (read-only si completada) |
| Long press     | Menu: Ver ficha, Reintentar sync            |

---

## 7. Sincronización Pendiente

### 7.1. Indicadores Visuales

| Estado Sync     | Icono  | Color    | Texto                    |
|-----------------|--------|----------|--------------------------|
| Sincronizado    | 🟢     | Green    | "Sincronizado"           |
| Pendiente       | 🔄     | Orange   | "Pendiente de sync"      |
| Error           | ⚠️     | Red      | "Error de sync"          |
| Conflicto       | ❗     | Yellow   | "Conflicto de datos"     |

### 7.2. Acciones de Sync en Historial

| Acción                        | Resultado                              |
|-------------------------------|----------------------------------------|
| Pull-to-refresh               | Intentar sincronizar pendientes        |
| Tap en "⚠️ Error de sync"    | Dialog con detalle + botón "Reintentar"|
| Banner "3 fichas sin sync"    | Resumen de pendientes arriba           |
| Botón "Sincronizar todo"      | Sincronizar todas las fichas pendientes|

---

## 8. Navegación

### 8.1. Entradas

| Desde              | Acción                    |
|--------------------|---------------------------|
| BottomNavigation   | Tab "Historial"           |

### 8.2. Salidas

| Acción               | Destino                | Params          |
|-----------------------|------------------------|-----------------|
| Tap en VisitaCard     | DetalleVisitaScreen    | fichaId         |
| Tab "Cartera"         | CarteraScreen          | —               |
| Tab "Ruta"            | RutaScreen             | —               |

---

## 9. ViewModel

```kotlin
@HiltViewModel
class HistorialViewModel @Inject constructor(
    private val getHistorialVisitasUseCase: GetHistorialVisitasUseCase,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialUiState())
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    init { loadHistorial() }

    fun onSearchQueryChanged(query: String)
    fun onEstadoFilterChanged(estado: EstadoFicha)
    fun onSegmentoFilterChanged(segmento: Segmento)
    fun onPeriodoChanged(periodo: Periodo)
    fun onFechasPersonalizadas(desde: LocalDate, hasta: LocalDate)
    fun onSyncFilterChanged(filter: SyncFilter)
    fun onRefresh()
    fun onRetrySyncClicked(fichaId: UUID)
    fun onSyncAllClicked()
    fun onVisitaClicked(fichaId: UUID)
}
```

---

## 10. Data Flow

```
[HistorialScreen]
     │ init
     ▼
[HistorialViewModel]
     │ getHistorialVisitasUseCase(filtros)
     ▼
[GetHistorialVisitasUseCase]
     │ fichaRepository.getFichasByAsesor(asesorId, filtros)
     ▼
[FichaCampoRepositoryImpl]
     │
     ├── Room: fichaDao.getAll() → Flow<List<FichaCampoEntity>>
     │
     ├── Mapear entities → domain models
     │
     └── Agrupar por fecha → Map<String, List<VisitaHistorial>>
```

---

## 11. Casos Edge

| Caso                                     | Comportamiento                                    |
|------------------------------------------|---------------------------------------------------|
| Sin visitas en el periodo                | Empty state con Lottie                            |
| +200 visitas en historial                | Paginación local (20 por lote)                    |
| Filtros sin resultados                   | "No hay visitas que coincidan"                    |
| Ficha pendiente de sync hace 24h+        | Alerta: "Fichas sin sincronizar hace más de 24h" |
| Error de sync repetido                   | Mostrar botón "Reportar problema"                 |
| Scroll a fechas antiguas                 | Lazy loading al acercarse al final                |
| Cambio de timezone                       | Usar zona horaria del dispositivo                 |
| Sin internet                             | Mostrar solo datos de Room + badge offline        |

---

## 12. Criterios de Aceptación

- [ ] La pantalla muestra todas las visitas del asesor agrupadas por fecha
- [ ] Los filtros por estado de ficha funcionan correctamente
- [ ] Los filtros por segmento funcionan correctamente
- [ ] El filtro por periodo (hoy, semana, mes, personalizado) funciona
- [ ] La búsqueda por nombre/DNI filtra correctamente
- [ ] Cada tarjeta muestra: cliente, segmento, score, monto, hora, estado, sync
- [ ] El indicador de sync pendiente es visible y correcto
- [ ] Pull-to-refresh intenta sincronizar fichas pendientes
- [ ] Tap en tarjeta navega al detalle de la visita
- [ ] El resumen de estadísticas muestra conteos correctos
- [ ] La pantalla funciona offline mostrando datos de Room
- [ ] La paginación funciona para listas largas
- [ ] Las fichas descalificadas muestran el motivo resumido
