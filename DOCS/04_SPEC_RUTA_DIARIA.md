# 04 — SPEC: RUTA DIARIA (Mapa)

| Campo               | Valor                                                        |
|---------------------|--------------------------------------------------------------|
| **ID**              | SDD-04                                                       |
| **Sprint**          | Sprint 3 — Ruta de Visitas                                   |
| **Estado**          | 📋 Especificado                                               |
| **Última revisión** | 2026-05-26                                                   |
| **Épica**           | E-04 Ruta de Visitas                                         |
| **Prioridad**       | 🔴 Alta                                                      |

---

## 1. Objetivo

Pantalla con **mapa interactivo** que muestra la ubicación georreferenciada de todos los clientes preaprobados de la cartera del asesor. Permite visualizar la ruta del día, planificar visitas por cercanía y navegar hacia los clientes usando GPS.

---

## 2. Diseño UX / UI

### 2.1. Wireframe

```
┌─────────────────────────────────┐
│ ← Ruta Diaria           🔄  📋 │ ← TopAppBar
├─────────────────────────────────┤
│                                 │
│     ┌──────────────────┐        │
│     │    MAPA           │       │
│     │                   │       │
│     │   🟢  pin PREMIER │       │
│     │        🔵 pin EST │       │
│     │                   │       │
│     │  📍(yo)           │       │
│     │       🟠 pin BAS  │       │
│     │                   │       │
│     │   🟢              │       │
│     │         🔵        │       │
│     └──────────────────┘        │
│                                 │
│ ┌───────────────────────────┐   │ ← BottomSheet
│ │ ▬▬▬                       │   │   (draggable)
│ │ 🟢 Juan Pérez · PREMIER   │   │
│ │ Bodega "Don Juan"         │   │
│ │ Score: 720 · S/ 4,200     │   │
│ │ 📍 450m · Estado: pend.   │   │
│ │ [Iniciar Ficha] [Navegar] │   │
│ └───────────────────────────┘   │
├──────────┬──────────┬───────────┤
│ 🏠 Cartera│ 🗺️ Ruta  │ 📊 Hist.  │
└──────────┴──────────┴───────────┘
```

### 2.2. Especificaciones de Diseño

| Elemento               | Especificación                                          |
|-------------------------|---------------------------------------------------------|
| Mapa                    | GoogleMap Composable, 100% del espacio disponible       |
| TopAppBar               | SmallTopAppBar transparente sobre el mapa               |
| Pins personalizados     | Custom markers con color del segmento + score           |
| Pin del asesor          | Marker azul con animación de pulso                      |
| BottomSheet             | ModalBottomSheet Material 3, esquinas 20dp              |
| Info Window             | Custom composable con resumen del cliente               |
| Ruta dibujada           | Polyline azul BBVA, 4dp grosor                          |
| Cluster markers         | Agrupación cuando zoom < 14                             |
| FAB ubicación           | FloatingActionButton "Mi ubicación", esquina inferior   |

### 2.3. Pins por Segmento

| Segmento        | Color Pin     | Icono          | Tamaño |
|-----------------|---------------|----------------|--------|
| PREMIER         | `#1B5E20`     | 🟢 Círculo     | 48dp   |
| ESTÁNDAR        | `#1565C0`     | 🔵 Círculo     | 44dp   |
| BÁSICO          | `#F57C00`     | 🟠 Círculo     | 40dp   |
| Visitado        | `#2E7D32` +✓  | ✅ Con check   | 44dp   |
| Descalificado   | `#D32F2F`     | ❌ Con X       | 36dp   |
| Mi ubicación    | `#004481`     | 📍 Pulso azul  | 52dp   |

### 2.4. Pin Info Window (al tap en pin)

```
┌─────────────────────────────┐
│ 🟢 PREMIER · Score: 720     │
│ Juan Pérez Mamani            │
│ Bodega "Don Juan"            │
│ Monto: S/ 4,200              │
│ Distancia: 450m              │
│ Estado: pendiente            │
│                              │
│ [Iniciar Ficha]  [Navegar 🧭]│
└─────────────────────────────┘
```

---

## 3. Integración Google Maps / MapBox

### 3.1. Configuración

```kotlin
// Dependencia
implementation("com.google.maps.android:maps-compose:4.3.0")
implementation("com.google.android.gms:play-services-maps:18.2.0")
implementation("com.google.android.gms:play-services-location:21.0.1")

// AndroidManifest.xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="${MAPS_API_KEY}" />

<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

### 3.2. Mapa Composable

```kotlin
@Composable
fun RutaMap(
    clientes: List<ClienteConUbicacion>,
    asesorLocation: LatLng?,
    rutaPolyline: List<LatLng>?,
    onPinClicked: (ClienteConUbicacion) -> Unit,
    onNavigateClicked: (ClienteConUbicacion) -> Unit,
    modifier: Modifier = Modifier
)
```

### 3.3. Permisos de Ubicación

| Permiso                    | Uso                              | Fallback              |
|----------------------------|----------------------------------|-----------------------|
| ACCESS_FINE_LOCATION       | GPS preciso del asesor           | Ubicación aproximada  |
| ACCESS_COARSE_LOCATION     | Ubicación por red/WiFi           | Solo mostrar clientes |
| Permiso denegado           | Mostrar mapa sin ubicación propia| Banner informativo    |
| Ubicación desactivada      | Solicitar activar GPS            | Dialog con settings   |

---

## 4. Cálculo de Rutas

### 4.1. Algoritmo de Ruta Óptima

```
1. Tomar ubicación actual del asesor como punto de inicio
2. Filtrar clientes con estado 'pendiente' o 'contactado'
3. Ordenar por distancia euclidiana al punto actual
4. Aplicar algoritmo greedy nearest-neighbor:
   - Seleccionar el cliente más cercano al punto actual
   - Mover punto actual al cliente seleccionado
   - Repetir hasta visitar todos o alcanzar límite (15 clientes)
5. Generar polyline de la ruta
```

### 4.2. Opciones de Ruta

| Opción                  | Descripción                              | Default |
|-------------------------|------------------------------------------|---------|
| Solo pendientes         | Mostrar solo clientes no visitados       | ✅ On   |
| Ruta automática         | Calcular ruta óptima                     | ✅ On   |
| Máx clientes en ruta    | Limitar a N clientes más cercanos        | 15      |
| Incluir visitados       | Mostrar pins de clientes ya visitados    | ✅ On   |
| Modo satélite           | Alternar vista satélite/normal           | ❌ Off  |

### 4.3. Navegación GPS

| Acción                  | Implementación                                    |
|-------------------------|---------------------------------------------------|
| "Navegar" en pin info   | Intent a Google Maps con coordenadas del cliente   |
| "Navegar ruta completa" | Intent con waypoints múltiples                     |
| Sin Google Maps         | Fallback a Waze o navegador web maps.google.com    |

```kotlin
fun navigateToClient(client: ClienteConUbicacion) {
    val uri = Uri.parse("google.navigation:q=${client.lat},${client.lng}")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }
    startActivity(intent)
}
```

---

## 5. Estados de la Pantalla

### 5.1. RutaUiState

```kotlin
data class RutaUiState(
    // Datos
    val clientes: List<ClienteConUbicacion> = emptyList(),
    val clientesFiltrados: List<ClienteConUbicacion> = emptyList(),
    val rutaOptima: List<LatLng> = emptyList(),

    // Ubicación del asesor
    val asesorLocation: LatLng? = null,
    val locationPermissionGranted: Boolean = false,
    val isLocationEnabled: Boolean = false,

    // Mapa
    val cameraPosition: CameraPosition = CameraPosition.default(),
    val mapType: MapType = MapType.NORMAL,
    val showCluster: Boolean = true,

    // Cliente seleccionado
    val selectedCliente: ClienteConUbicacion? = null,
    val showBottomSheet: Boolean = false,

    // Filtros de mapa
    val showOnlyPendientes: Boolean = true,
    val showRuta: Boolean = true,
    val maxClientesRuta: Int = 15,

    // Estado pantalla
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isCalculatingRoute: Boolean = false
)

data class ClienteConUbicacion(
    val cliente: ClientePreaprobado,
    val latLng: LatLng,
    val distanciaMetros: Double? = null
)
```

---

## 6. Navegación

### 6.1. Entradas

| Desde               | Acción                    | Params                    |
|----------------------|---------------------------|---------------------------|
| BottomNavigation     | Tab "Ruta"                | —                         |
| CarteraScreen        | FAB "Ver ruta"            | —                         |
| CarteraScreen        | "📍 Ubicación" en card    | clienteId (centrar mapa)  |

### 6.2. Salidas

| Acción                | Destino              | Params                    |
|-----------------------|----------------------|---------------------------|
| "Iniciar Ficha"       | FichaScreen          | clienteId                 |
| "Navegar 🧭"          | Google Maps (Intent) | lat, lng                  |
| Tab "Cartera"         | CarteraScreen        | —                         |
| Tab "Historial"       | HistorialScreen      | —                         |
| Icono 📋 (lista)      | CarteraScreen        | —                         |

---

## 7. ViewModel

### 7.1. RutaViewModel

```kotlin
@HiltViewModel
class RutaViewModel @Inject constructor(
    private val getCarteraDiariaUseCase: GetCarteraDiariaUseCase,
    private val locationProvider: LocationProvider,
    private val routeCalculator: RouteCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(RutaUiState())
    val uiState: StateFlow<RutaUiState> = _uiState.asStateFlow()

    fun onMapLoaded()
    fun onPinClicked(cliente: ClienteConUbicacion)
    fun onBottomSheetDismissed()
    fun onIniciarFichaClicked(clienteId: UUID)
    fun onNavegarClicked(cliente: ClienteConUbicacion)
    fun onTogglePendientes()
    fun onToggleRuta()
    fun onToggleMapType()
    fun onMyLocationClicked()
    fun onLocationPermissionResult(granted: Boolean)
    fun recalculateRoute()
}
```

---

## 8. Data Flow

```
[RutaScreen]
     │ onMapLoaded()
     ▼
[RutaViewModel]
     │
     ├── getCarteraDiariaUseCase() ──► clientes con coordenadas
     │
     ├── locationProvider.getCurrentLocation() ──► asesor LatLng
     │
     └── routeCalculator.calculateOptimalRoute(asesor, clientes)
            │
            └── List<LatLng> ──► dibujar polyline en mapa
```

---

## 9. Resumen del Cliente en Pin

Al tocar un pin del mapa, se muestra un **BottomSheet** con la información resumida del cliente:

| Campo                | Fuente                    | Display                  |
|----------------------|---------------------------|--------------------------|
| Segmento             | score_transaccional       | Badge coloreado          |
| Score                | score_transaccional       | "Score: 720"             |
| Nombre completo      | perfil_cliente            | Título                   |
| Nombre negocio       | perfil_cliente            | Subtítulo                |
| Tipo negocio         | perfil_cliente            | Label                    |
| Monto hipótesis      | score_transaccional       | "S/ 4,200"               |
| Distancia            | cálculo GPS               | "450m" o "2.3 km"        |
| Estado de visita     | credito_preaprobado       | Badge con color          |
| Dirección            | perfil_cliente            | Texto con icono 📍       |

### Acciones del BottomSheet

| Botón              | Acción                                  | Estilo          |
|--------------------|-----------------------------------------|-----------------|
| Iniciar Ficha      | Navegar a FichaScreen con clienteId     | FilledButton    |
| Navegar 🧭         | Abrir Google Maps con coordenadas       | OutlinedButton  |
| Llamar 📞          | Intent telefónico                       | IconButton      |
| Cerrar             | Dismiss BottomSheet                     | IconButton X    |

---

## 10. Estado de Visita en Pins

Los pins del mapa muestran visualmente el estado de cada cliente:

| Estado              | Visualización en Pin                          |
|---------------------|-----------------------------------------------|
| pendiente           | Pin estándar con color de segmento            |
| contactado          | Pin con borde doble                           |
| visita_agendada     | Pin con icono calendario                      |
| visita_realizada    | Pin con checkmark ✓ superpuesto               |
| en_comite           | Pin con icono especial 🏛️                     |
| aprobado            | Pin verde con doble check ✓✓                  |
| rechazado           | Pin gris con X                                |

---

## 11. Casos Edge

| Caso                                | Comportamiento                                    |
|--------------------------------------|---------------------------------------------------|
| Sin permiso de ubicación             | Mapa funciona pero sin pin del asesor ni ruta     |
| GPS desactivado                      | Dialog solicitando activar ubicación              |
| Cliente sin coordenadas              | No mostrar pin, marcar en lista como "sin ubicar" |
| Sin internet (offline)               | Mapa con tiles cacheados + pins de Room           |
| Zoom out extremo                     | Clustering de pins con número                     |
| +200 pins en mapa                    | Clustering obligatorio                            |
| Ruta con 0 pendientes                | Mostrar mensaje "No hay visitas pendientes"       |
| Error de cálculo de ruta             | Mostrar pins sin polyline                         |
| Tablet / pantalla grande             | Mapa a la izquierda, lista a la derecha           |
| Cambio de orientación                | Mantener cameraPosition en ViewModel              |
| Asesor en movimiento                 | Actualizar posición cada 30 segundos              |

---

## 12. Criterios de Aceptación

- [ ] El mapa muestra todos los clientes con coordenadas de la cartera
- [ ] Los pins tienen colores distintos según el segmento del cliente
- [ ] Al tocar un pin se abre un BottomSheet con resumen del cliente
- [ ] El botón "Iniciar Ficha" navega a la pantalla de ficha de campo
- [ ] El botón "Navegar" abre Google Maps con las coordenadas del cliente
- [ ] La ubicación del asesor se muestra con un pin azul pulsante
- [ ] La ruta óptima se dibuja como polyline en el mapa
- [ ] Los filtros de "solo pendientes" funcionan correctamente
- [ ] El mapa funciona sin internet con datos cacheados en Room
- [ ] Los permisos de ubicación se solicitan correctamente
- [ ] El clustering agrupa pins cuando hay muchos en la misma zona
- [ ] La pantalla mantiene su estado en rotación
- [ ] Las transiciones de cámara son fluidas (no saltos bruscos)
