# Guía Académica — AppMiBanco S8
## Arquitectura MVVM en Android con Kotlin, Jetpack Compose y Supabase

**Repositorio:** [github.com/u2008113935/devappmov_appbanco_s8](https://github.com/u2008113935/devappmov_appbanco_s8)  
**Curso:** Desarrollo de Aplicaciones Móviles  
**Stack:** Kotlin · Jetpack Compose · MVVM · Retrofit · Supabase · PostgreSQL  
**Package base:** `com.example.appbanco_s8`

---

## Parte 1 — ¿Qué problema resuelve MVVM?

Imagina que escribes todo en una sola pantalla: la llamada a internet, la lógica del negocio y el dibujo de los botones. Cuando algo falla no sabes en qué parte está el error. Cuando quieres reutilizar la lógica en otra pantalla, no puedes. Cuando el dispositivo rota, pierdes los datos. Eso es lo que pasa sin arquitectura.

**MVVM divide la aplicación en tres capas con responsabilidades distintas:**

```
┌─────────────────────────────────────────┐
│  VIEW (Jetpack Compose)                 │
│  Solo dibuja lo que el ViewModel dice.  │
│  No sabe nada de internet ni de la BD.  │
└──────────────┬──────────────────────────┘
               │ observa StateFlow
               ▼
┌─────────────────────────────────────────┐
│  VIEWMODEL                              │
│  Procesa la lógica de la pantalla.      │
│  Sobrevive a la rotación del dispositivo│
│  Expone estados sellados (sealed class) │
└──────────────┬──────────────────────────┘
               │ llama funciones suspend
               ▼
┌─────────────────────────────────────────┐
│  MODEL (Repository + Remote + Data)     │
│  Obtiene y transforma los datos.        │
│  No sabe nada de pantallas ni vistas.   │
└─────────────────────────────────────────┘
               │ HTTPS + JWT
               ▼
┌─────────────────────────────────────────┐
│  SUPABASE (PostgreSQL + Auth + RLS)     │
│  Base de datos remota en la nube.       │
└─────────────────────────────────────────┘
```

**La regla de oro:** cada capa solo habla con la capa inmediatamente inferior. Una pantalla nunca llama a Retrofit directamente. Un ViewModel nunca dibuja nada en pantalla.

---

## Parte 2 — Estructura completa del proyecto

```
com.example.appbanco_s8/
│
├── data/                        ← Capa MODEL
│   ├── model/                   ← Clases de datos puras
│   │   ├── AuthModels.kt
│   │   ├── CuentaModels.kt
│   │   ├── OperaModels.kt
│   │   └── PrestamoModels.kt
│   ├── remote/                  ← Comunicación con la API
│   │   ├── SupabaseConfig.kt
│   │   ├── SupabaseAuthApi.kt
│   │   ├── SupabaseApi.kt
│   │   └── RetrofitClient.kt
│   └── repository/              ← Intermediario entre ViewModel y API
│       ├── AuthRepository.kt
│       ├── CuentaRepository.kt
│       ├── PagoRepository.kt
│       ├── PrestamoRepository.kt
│       └── TarjetaRepository.kt
│
├── navigation/                  ← Sistema de navegación
│   ├── Screen.kt
│   └── NavGraph.kt
│
├── ui/                          ← Capa VIEW
│   ├── components/              ← Componentes reutilizables
│   │   ├── AppScaffold.kt
│   │   ├── BankCard.kt
│   │   ├── BottomNavBar.kt
│   │   ├── MovimientoItem.kt
│   │   └── QuickActions.kt
│   ├── screens/                 ← Pantallas completas
│   │   ├── LoginScreen.kt
│   │   ├── HomeScreen.kt
│   │   ├── CuentaScreen.kt
│   │   ├── TarjetaScreen.kt
│   │   ├── PrestamoScreen.kt
│   │   ├── OperaScreen.kt
│   │   ├── NotificaScreen.kt
│   │   ├── ContactoScreen.kt
│   │   ├── PerfilScreen.kt
│   │   └── SplashActivity.kt
│   ├── theme/                   ← Identidad visual
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   └── viewmodel/               ← Capa VIEWMODEL
│       ├── UiState.kt
│       ├── AuthViewModel.kt
│       ├── HomeViewModel.kt
│       ├── CuentaViewModel.kt
│       ├── TarjetaViewModel.kt
│       ├── PrestamoViewModel.kt
│       └── OperaViewModel.kt
│
└── MainActivity.kt              ← Punto de entrada único
```

---

## Parte 3 — Explicación archivo por archivo

---

### 3.1 Capa DATA — Model

#### `data/model/AuthModels.kt`

**¿Qué es?** Clases de datos que representan lo que se envía y recibe durante el proceso de autenticación.

**¿Por qué existe?** Retrofit necesita clases concretas para serializar (convertir a JSON) y deserializar (convertir de JSON a objeto Kotlin) los mensajes de la API.

```kotlin
data class LoginRequest(val email: String, val password: String)
// Se convierte a: {"email":"...","password":"..."}

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String = "",
    val user: UserData? = null,
    val error: String? = null
)
// Gson convierte el JSON de respuesta en este objeto
```

**Concepto clave:** `@SerializedName` mapea el nombre del campo en el JSON con el nombre del campo en Kotlin. La API devuelve `access_token` (con guion bajo) pero en Kotlin usamos `accessToken` (camelCase) por convención.

---

#### `data/model/CuentaModels.kt`

**¿Qué es?** Clases que representan las entidades bancarias: cuenta, transacción y cuenta de ahorro.

**¿Por qué existe?** Cada tabla de Supabase necesita una clase Kotlin correspondiente. Supabase devuelve JSON y Gson lo convierte en estas clases.

```kotlin
data class Transaccion(...) {
    fun esDebito() = tipo == "debito"
    fun montoFormateado() = "S/ %,.2f".format(monto)
}
```

**Concepto clave:** Las funciones dentro de un `data class` son lógica de presentación de los datos, no de la pantalla. `montoFormateado()` sabe cómo mostrarse, pero no sabe dónde mostrarse. Eso lo decide la pantalla.

---

#### `data/model/PrestamoModels.kt` y `data/model/OperaModels.kt`

**¿Qué son?** Modelos para préstamos y tarjetas respectivamente.

**¿Por qué existen?** Cada módulo de negocio tiene sus propias entidades. Separar los modelos evita que un archivo crezca indefinidamente y hace más fácil encontrar qué clase corresponde a qué módulo.

```kotlin
data class Prestamo(...) {
    fun totalCuota() = capitalCuota + interesesCuota + segurosCuota
    fun progreso()   = (1.0 - capitalPendiente / capitalTotal).toFloat()
}
```

---

### 3.2 Capa DATA — Remote

#### `data/remote/SupabaseConfig.kt`

**¿Qué es?** Un objeto singleton con las constantes de configuración de la conexión a Supabase.

**¿Por qué existe?** Centraliza las credenciales en un solo lugar. Si la URL o la clave cambia, se modifica aquí y toda la app se actualiza automáticamente.

```kotlin
object SupabaseConfig {
    const val BASE_URL = "https://[proyecto].supabase.co/"
    const val ANON_KEY = "eyJhbGci..."
}
```

**Concepto clave:** `object` en Kotlin es un singleton — existe una sola instancia en toda la app. Es el patrón apropiado para configuraciones globales.

**Advertencia de seguridad:** La `ANON_KEY` es la clave pública del proyecto. No es secreta — está pensada para ser usada en clientes. Lo que protege los datos no es la clave sino las políticas RLS de Supabase.

---

#### `data/remote/SupabaseAuthApi.kt`

**¿Qué es?** Una interfaz de Retrofit que define los endpoints de autenticación.

**¿Por qué existe?** Retrofit usa interfaces para generar automáticamente el código HTTP. El desarrollador solo declara qué endpoint existe y qué parámetros recibe; Retrofit genera el cliente HTTP.

```kotlin
interface SupabaseAuthApi {
    @POST("auth/v1/token")
    suspend fun login(
        @Query("grant_type") grantType: String = "password",
        @Body body: LoginRequest
    ): Response<AuthResponse>
}
```

**Concepto clave:** `suspend` indica que esta función es una coroutine — puede pausarse mientras espera la respuesta de internet sin bloquear el hilo principal. El `@POST` indica que es una petición HTTP POST al endpoint `auth/v1/token`.

---

#### `data/remote/SupabaseApi.kt`

**¿Qué es?** Interfaz de Retrofit para todos los endpoints de datos del negocio.

**¿Por qué existe?** Separa la autenticación (SupabaseAuthApi) de los datos (SupabaseApi). Son responsabilidades distintas: una gestiona el JWT, la otra lo usa.

```kotlin
interface SupabaseApi {
    @GET("rest/v1/cuentas")
    suspend fun getCuentas(
        @Header("Authorization") token: String,  // Bearer JWT
        @Query("select") select: String = "*",
        @Query("order")  order:  String = "tipo.asc"
    ): Response<List<Cuenta>>
}
```

**Concepto clave:** El `@Header("Authorization")` adjunta el JWT a cada petición. Supabase usa ese token para identificar al usuario y aplicar las políticas RLS — cada usuario solo ve sus propios datos.

---

#### `data/remote/RetrofitClient.kt`

**¿Qué es?** El cliente HTTP singleton que configura Retrofit con interceptores y crea las instancias de las interfaces.

**¿Por qué existe?** Crear un cliente Retrofit es costoso en términos de memoria y tiempo. `object` garantiza que se crea una sola vez y se reutiliza en toda la app.

```kotlin
object RetrofitClient {
    private val baseClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor { chain ->
            // Agrega la ANON_KEY a TODAS las peticiones automáticamente
            val request = chain.request().newBuilder()
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .build()
            chain.proceed(request)
        }
        .build()
}
```

**Concepto clave:** El interceptor de OkHttp funciona como un middleware — intercepta cada petición antes de enviarla y le agrega el header `apikey`. Sin este header, Supabase rechaza todas las peticiones.

---

### 3.3 Capa DATA — Repository

#### ¿Por qué existen los repositorios?

Los repositorios son el patrón más importante de la arquitectura. Su función es ser el **único punto de verdad** de los datos para el ViewModel.

Sin repositorio, el ViewModel llamaría directamente a Retrofit. Eso tiene tres problemas:

1. Si mañana cambias de Supabase a Firebase, tienes que modificar TODOS los ViewModels.
2. No puedes hacer pruebas unitarias del ViewModel sin internet real.
3. Si necesitas combinar datos de dos fuentes (local y remota), el ViewModel se vuelve complejo.

Con repositorio, el ViewModel no sabe de dónde vienen los datos. El repositorio decide.

---

#### `data/repository/AuthRepository.kt`

**¿Qué hace?** Envuelve la llamada de login en un `Result<T>` para manejar errores de forma elegante.

```kotlin
class AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.login(body = LoginRequest(email, password))
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Credenciales incorrectas"))
        } catch (e: Exception) {
            Result.failure(Exception("Sin conexión: ${e.message}"))
        }
    }
}
```

**Concepto clave:** `Result<T>` es un contenedor que puede ser éxito (`Result.success`) o fracaso (`Result.failure`). El ViewModel no necesita saber si falló por internet o por credenciales incorrectas — solo necesita saber si fue éxito o error.

---

#### `data/repository/CuentaRepository.kt`

**¿Qué hace?** Obtiene cuentas, transacciones y cuenta de ahorro desde Supabase.

```kotlin
suspend fun getCuentas(token: String): Result<List<Cuenta>> = try {
    val r = api.getCuentas("Bearer $token")
    if (r.isSuccessful) Result.success(r.body() ?: emptyList())
    else Result.failure(Exception("Error ${r.code()}"))
} catch (e: Exception) { Result.failure(e) }
```

**Concepto clave:** El `"Bearer $token"` construye el header de autorización JWT. `Bearer` es el tipo de token que Supabase espera — sin esa palabra, la autenticación falla aunque el token sea correcto.

---

### 3.4 Capa VIEWMODEL

#### `ui/viewmodel/UiState.kt`

**¿Qué es?** Una sealed class compartida que representa los tres estados posibles de cualquier carga de datos.

**¿Por qué existe como archivo separado?** Todos los ViewModels de datos usan `DataUiState`. Si estuviera dentro de un ViewModel específico, los demás no podrían importarla sin crear una dependencia circular. Al estar en su propio archivo dentro del mismo package, Kotlin la comparte automáticamente.

```kotlin
sealed class DataUiState<out T> {
    object Loading                        : DataUiState<Nothing>()
    data class Success<T>(val data: T)    : DataUiState<T>()
    data class Error(val mensaje: String) : DataUiState<Nothing>()
}
```

**Concepto clave:** `sealed class` garantiza que no existen otros estados posibles además de los declarados. El compilador puede verificar en un `when` que todos los casos están cubiertos. Esto elimina una categoría entera de bugs en tiempo de compilación.

---

#### `ui/viewmodel/AuthViewModel.kt`

**¿Qué hace?** Gestiona el estado de la pantalla de login.

```kotlin
sealed class AuthUiState {
    object Idle    : AuthUiState()   // Estado inicial
    object Loading : AuthUiState()   // Esperando respuesta
    data class Success(val token: String, val email: String) : AuthUiState()
    data class Error(val mensaje: String) : AuthUiState()
}

class AuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState   // Solo lectura para la UI

    fun login(email: String, password: String) {
        viewModelScope.launch {           // Coroutine ligada al ciclo de vida
            _uiState.value = AuthUiState.Loading
            val result = repository.login(email, password)
            _uiState.value = if (result.isSuccess)
                AuthUiState.Success(...)
            else
                AuthUiState.Error(...)
        }
    }
}
```

**Conceptos clave:**

`MutableStateFlow` vs `StateFlow`: la versión mutable (`_uiState`) es privada — solo el ViewModel puede cambiarla. La versión de solo lectura (`uiState`) es pública — la pantalla puede observarla pero no modificarla. Este patrón se llama encapsulamiento de estado.

`viewModelScope.launch`: lanza una coroutine en el contexto del ViewModel. Cuando el usuario navega fuera de la pantalla, el ViewModel cancela automáticamente todas sus coroutines, evitando memory leaks.

---

#### `ui/viewmodel/HomeViewModel.kt`

**¿Qué hace?** Carga las cuentas y transacciones, y calcula ingresos y gastos del mes.

```kotlin
class HomeViewModel : ViewModel() {
    private val _cuentas = MutableStateFlow<DataUiState<List<Cuenta>>>(DataUiState.Loading)
    private val _transacciones = MutableStateFlow<DataUiState<List<Transaccion>>>(DataUiState.Loading)

    // Propiedades calculadas — no almacenan datos, los calculan al instante
    val ingresosMes: Double
        get() = when (val s = _transacciones.value) {
            is DataUiState.Success -> s.data.filter { !it.esDebito() }.sumOf { it.monto }
            else -> 0.0
        }

    fun cargarDatos(token: String) {
        viewModelScope.launch {
            // Primero carga cuentas
            val resCuentas = repository.getCuentas(token)
            // Luego usa el ID de la cuenta corriente para cargar transacciones
            val corriente = resCuentas.getOrNull()?.firstOrNull { it.tipo == "corriente" }
            if (corriente != null) {
                repository.getTransacciones(token, corriente.id)
            }
        }
    }
}
```

**Concepto clave:** Las propiedades calculadas (`val ingresosMes get() = ...`) no almacenan un valor — lo calculan cada vez que se leen. Esto asegura que siempre reflejen el estado actual de `_transacciones` sin necesitar un StateFlow adicional.

---

### 3.5 Capa VIEW — Navigation

#### `navigation/Screen.kt`

**¿Qué es?** Una sealed class que define todas las rutas de navegación de la app.

**¿Por qué existe?** Sin esta clase, las rutas serían strings literales dispersos por todo el código como `"home/token123/user@mail.com"`. Un error tipográfico en cualquier parte causaría que la navegación silenciosamente fallara. Con `Screen`, el compilador verifica que las rutas sean correctas.

```kotlin
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home  : Screen("home/{token}/{email}") {
        fun createRoute(token: String, email: String) = "home/$token/$email"
    }
}
```

**Concepto clave:** El método `createRoute()` construye la ruta con parámetros concretos. La pantalla de login navega así: `navController.navigate(Screen.Home.createRoute(token, email))`. Si cambias el patrón de la ruta, solo lo cambias en un lugar.

---

#### `navigation/NavGraph.kt`

**¿Qué es?** El mapa completo de navegación de la app. Define qué pantalla se muestra para cada ruta y cómo se extraen los argumentos.

**¿Por qué existe?** Centraliza toda la navegación. Sin este archivo, cada pantalla tendría que saber cómo navegar a las demás, creando un acoplamiento fuerte entre pantallas.

```kotlin
@Composable
fun AppNavGraph(navController: NavHostController) {
    var tokenGlobal by remember { mutableStateOf("") }

    NavHost(startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { token, email ->
                    tokenGlobal = token
                    navController.navigate(Screen.Home.createRoute(token, email))
                }
            )
        }
        composable(Screen.Home.route, arguments = listOf(...)) { back ->
            val token = back.arguments?.getString("token") ?: tokenGlobal
            AppScaffold(...) { HomeScreen(token = token, ...) }
        }
    }
}
```

**Concepto clave:** `tokenGlobal` almacena el JWT en memoria durante la sesión. Cuando el usuario navega entre pantallas, el token se pasa por las rutas o se recupera de esta variable. Si el usuario cierra la app, se pierde — eso es correcto en el diseño actual (sin persistencia de sesión).

---

### 3.6 Capa VIEW — Components

#### `ui/components/AppScaffold.kt`

**¿Qué es?** El componente contenedor que envuelve todas las pantallas autenticadas, proveyendo la barra de navegación inferior y el drawer lateral.

**¿Por qué existe?** Sin este componente, cada pantalla tendría su propio `BottomNavBar` y su propio `Drawer`. Si cambias el diseño de la barra, tendrías que modificar 8 pantallas. Con `AppScaffold`, lo cambias una vez.

```kotlin
@Composable
fun AppScaffold(
    token: String,
    email: String,
    navController: NavHostController,
    onLogout: () -> Unit,
    onOpenDrawer: ((openFn: () -> Unit) -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope       = rememberCoroutineScope()

    SideEffect {
        onOpenDrawer?.invoke { scope.launch { drawerState.open() } }
    }

    ModalNavigationDrawer(drawerState = drawerState, ...) {
        Scaffold(bottomBar = { BottomNavBar(...) }) { padding ->
            content(padding)   // Aquí va la pantalla real
        }
    }
}
```

**Concepto clave:** `content: @Composable (PaddingValues) -> Unit` es un parámetro de tipo función composable — el patrón Slot. `AppScaffold` no sabe qué contenido va adentro; solo provee el contenedor. La pantalla concreta se inyecta desde `NavGraph`.

**Por qué `SideEffect` y no `LaunchedEffect`:**
`SideEffect` se ejecuta después de cada recomposición exitosa en el hilo principal — no es una coroutine. Por eso puede llamar `scope.launch { }`. `LaunchedEffect` lanza su propio contexto de coroutine — dentro de él ya tienes un scope implícito y usar `scope.launch` anidado sería un error de compilación.

---

### 3.7 Capa VIEW — Screens

#### `ui/screens/LoginScreen.kt`

**¿Qué hace?** Muestra el formulario de login y observa el estado del `AuthViewModel`.

```kotlin
@Composable
fun LoginScreen(
    onLoginSuccess: (token: String, email: String) -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            val s = uiState as AuthUiState.Success
            onLoginSuccess(s.token, s.email)
        }
    }
    // ... UI
}
```

**Conceptos clave:**

`collectAsStateWithLifecycle()` convierte el `StateFlow` del ViewModel en un `State` de Compose. Cada vez que el ViewModel emite un nuevo estado, Compose redibuja solo los elementos afectados.

`LaunchedEffect(uiState)` se ejecuta cada vez que `uiState` cambia. Cuando detecta un `Success`, llama `onLoginSuccess` para navegar. La navegación no la hace la pantalla directamente — la delega hacia arriba mediante la lambda.

`viewModel: AuthViewModel = viewModel()` tiene un valor por defecto. En producción, Compose inyecta el ViewModel real. En pruebas, puedes pasar un ViewModel falso.

---

#### `ui/screens/HomeScreen.kt`

**¿Qué hace?** Pantalla principal que muestra resumen financiero, cuentas, movimientos recientes y accesos rápidos.

```kotlin
@Composable
fun HomeScreen(
    token: String,
    email: String,
    navController: NavHostController,
    onLogout: () -> Unit,
    onMenuClick: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val cuentasState       by viewModel.cuentas.collectAsStateWithLifecycle()
    val transaccionesState by viewModel.transacciones.collectAsStateWithLifecycle()

    LaunchedEffect(token) { viewModel.cargarDatos(token) }

    LazyColumn {
        item { HeaderSection(onMenuClick = onMenuClick) }
        item { ResumenSection(ingresos = viewModel.ingresosMes, ...) }
        when (val state = cuentasState) {
            is DataUiState.Loading -> item { LoadingCard() }
            is DataUiState.Error   -> item { ErrorCard(state.mensaje) }
            is DataUiState.Success -> items(state.data) { CuentaCard(it) }
        }
    }
}
```

**Concepto clave:** El `when` sobre `DataUiState` garantiza que los tres estados (Loading, Error, Success) siempre estén manejados. Si añades un nuevo estado a la sealed class, el compilador exigirá manejarlo aquí también.

---

### 3.8 Capa VIEW — Theme

#### `ui/theme/Color.kt`

**¿Qué es?** Define la paleta de colores del banco.

**¿Por qué existe?** Sin este archivo, los colores estarían dispersos como `Color(0xFF1A5DC8)` en 30 lugares distintos. Con el archivo centralizado, cambiar el azul del banco es una sola línea.

```kotlin
val AzulBanco    = Color(0xFF1A5DC8)   // Azul principal
val AzulMarino   = Color(0xFF020B18)   // Fondo oscuro
val DoradoBanco  = Color(0xFFF5C842)   // Acento dorado (botón Salir)
val VerdeExito   = Color(0xFF2ECC71)   // Créditos
val RojoError    = Color(0xFFE74C3C)   // Débitos y errores
```

---

#### `ui/theme/Theme.kt`

**¿Qué es?** Define el `MaterialTheme` completo con los colores del banco para modo oscuro y claro.

**¿Por qué se desactivó `dynamicColor`?** El color dinámico de Android 12+ cambiaría el azul del banco por el color del wallpaper del usuario. Una app bancaria debe mantener su identidad visual en todos los dispositivos.

---

### 3.9 Punto de entrada

#### `MainActivity.kt`

**¿Qué es?** El único `Activity` de la app. Su única responsabilidad es montar el `AppNavGraph` dentro del tema.

**¿Por qué tiene solo ~15 líneas?** Una `Activity` con más código que eso indica que algo que debería estar en otra capa terminó aquí. Si `MainActivity` crece, es una señal de que la arquitectura se está violando.

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppBancoS8Theme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }
}
```

---

#### `ui/screens/SplashActivity.kt`

**¿Qué es?** Una `Activity` separada que muestra el splash screen antes de cargar `MainActivity`.

**¿Por qué existe como Activity y no como pantalla Compose?** El splash screen debe aparecer antes de que Compose esté inicializado. La API `core-splashscreen` de Android requiere una `Activity` con un tema especial declarado en `AndroidManifest.xml`. No hay otra forma de lograrlo en Android moderno.

```kotlin
@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, MainActivity::class.java))
        finish()   // Se destruye — no vuelve al splash con botón atrás
    }
}
```

---

## Parte 4 — Flujo de datos end-to-end

Este es el recorrido completo de un login exitoso:

```
1. Usuario escribe email y contraseña en LoginScreen
   └─ LoginScreen llama viewModel.login(email, password)

2. AuthViewModel recibe la llamada
   └─ Emite AuthUiState.Loading (la pantalla muestra spinner)
   └─ Llama repository.login(email, password) en una coroutine

3. AuthRepository hace la petición HTTP
   └─ Retrofit → POST https://[proyecto].supabase.co/auth/v1/token
   └─ Supabase valida credenciales en auth.users
   └─ Devuelve { access_token: "eyJ...", user: { id, email } }

4. AuthRepository envuelve la respuesta
   └─ Devuelve Result.success(AuthResponse) al ViewModel

5. AuthViewModel procesa el resultado
   └─ Emite AuthUiState.Success(token, email)
   └─ LoginScreen detecta el Success con LaunchedEffect
   └─ Llama onLoginSuccess(token, email)

6. NavGraph navega a HomeScreen
   └─ Pasa token y email como argumentos de ruta

7. HomeViewModel.cargarDatos(token) se ejecuta
   └─ GET /rest/v1/cuentas con header Authorization: Bearer JWT
   └─ Supabase aplica RLS: solo devuelve cuentas donde user_id = auth.uid()
   └─ HomeScreen recibe DataUiState.Success(listOf(cuenta1, cuenta2))
   └─ Compose redibuja las cards de cuentas
```

---

## Parte 5 — Preguntas de comprensión

Estas preguntas verifican comprensión real, no memorización:

1. ¿Por qué `_uiState` es privado y `uiState` es público en el ViewModel? ¿Qué problema evita esto?

2. Si quisieras agregar una pantalla de "Transferencias" nueva, ¿qué archivos tendrías que crear y en qué orden?

3. ¿Qué pasaría si el `LoginScreen` llamara directamente a `RetrofitClient.authApi.login()` sin pasar por el ViewModel ni el Repository? ¿Qué problemas surgiría?

4. La función `cargarDatos(token)` en `HomeViewModel` hace dos llamadas en secuencia: primero cuentas, luego transacciones. ¿Por qué no se pueden hacer en paralelo sin modificar el código?

5. ¿Por qué `SplashActivity` llama `finish()` después de iniciar `MainActivity`? ¿Qué ocurriría si no lo hiciera?

6. Explica qué hace `@SerializedName("access_token")` y por qué es necesario si Supabase devuelve `access_token` con guion bajo.

7. ¿Qué es la política RLS `auth.uid() = user_id` y cómo garantiza que un usuario no vea los datos de otro aunque tenga el JWT de ese otro usuario?

---

## Parte 6 — Glosario técnico

| Término | Definición en contexto del proyecto |
|---|---|
| `@Composable` | Función que describe una parte de la UI. Compose la redibuja automáticamente cuando su estado cambia. |
| `StateFlow` | Flujo de datos que siempre tiene un valor actual. La UI lo observa y se actualiza en cada cambio. |
| `suspend` | Función que puede pausarse y reanudarse sin bloquear el hilo. Necesaria para operaciones de red. |
| `CoroutineScope` | Contexto que agrupa coroutines. Cuando el scope se cancela, todas sus coroutines se cancelan también. |
| `sealed class` | Clase con un número finito de subclases conocidas en tiempo de compilación. Permite exhaustividad en `when`. |
| `data class` | Clase cuyo propósito es almacenar datos. Genera automáticamente `equals`, `hashCode` y `copy`. |
| `object` | Singleton en Kotlin. Una sola instancia existe en toda la app. |
| `JWT` | JSON Web Token. String codificado que contiene la identidad del usuario. Supabase lo genera en el login. |
| `RLS` | Row Level Security. Política de PostgreSQL que filtra filas según el usuario autenticado. |
| `Retrofit` | Librería que convierte interfaces Kotlin en clientes HTTP. |
| `Gson` | Librería que convierte objetos Kotlin a JSON y viceversa. |
| `NavHostController` | Controlador de navegación de Compose. Mantiene el back stack de pantallas. |
| `LaunchedEffect` | Efecto secundario en Compose que lanza una coroutine ligada a una clave. |
| `SideEffect` | Efecto secundario en Compose que se ejecuta en el hilo principal después de cada recomposición. |

---

*Guía generada para el Desarrollo de Aplicaciones Móviles 
*Repositorio: [github.com/u2008113935/devappmov_appbanco_s8](https://github.com/u2008113935/devappmov_appbanco_s8)*
