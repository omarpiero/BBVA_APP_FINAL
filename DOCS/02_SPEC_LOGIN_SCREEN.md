# 02 — SPEC: LOGIN SCREEN

| Campo               | Valor                                                        |
|---------------------|--------------------------------------------------------------|
| **ID**              | SDD-02                                                       |
| **Sprint**          | Sprint 1 — Autenticación                                     |
| **Estado**          | 📋 Especificado                                               |
| **Última revisión** | 2026-05-26                                                   |
| **Épica**           | E-02 Autenticación                                           |
| **Prioridad**       | 🔴 Alta                                                      |

---

## 1. Objetivo

Implementar la pantalla de inicio de sesión para **asesores de negocios** del banco. Debe autenticar contra **Supabase Auth**, manejar sesiones persistentes y proporcionar una UX profesional acorde a la identidad BBVA.

---

## 2. Diseño UX / UI

### 2.1. Wireframe Conceptual

```
┌─────────────────────────────────┐
│                                 │
│         [Logo BBVA]             │
│                                 │
│     FUERZA DE VENTAS            │
│     ─────────────────           │
│                                 │
│  ┌─────────────────────────┐    │
│  │  📧 Email corporativo   │    │
│  └─────────────────────────┘    │
│                                 │
│  ┌─────────────────────────┐    │
│  │  🔒 Contraseña      👁️  │    │
│  └─────────────────────────┘    │
│                                 │
│  ☐ Recordar sesión              │
│                                 │
│  ┌─────────────────────────┐    │
│  │     INICIAR SESIÓN      │    │
│  └─────────────────────────┘    │
│                                 │
│  ¿Olvidaste tu contraseña?      │
│                                 │
│  ─────────────────────────      │
│  Versión 1.0.0                  │
│  © 2026 BBVA Fuerza de Ventas   │
└─────────────────────────────────┘
```

### 2.2. Especificaciones de Diseño

| Elemento               | Especificación                                          |
|-------------------------|---------------------------------------------------------|
| Fondo                   | Gradiente vertical `#004481` → `#002A4D`               |
| Logo                    | Logo BBVA blanco, centrado, 120dp ancho                |
| Título                  | "FUERZA DE VENTAS" — Roboto Bold 20sp, blanco          |
| Campos de texto         | OutlinedTextField con iconos, fondo blanco 90% alpha   |
| Botón principal         | FilledButton — `#5BBEFF`, texto blanco, 56dp alto      |
| Link "olvidó contraseña"| TextButton — blanco 70% alpha, 14sp                    |
| Footer                  | Versión app — gris claro, 12sp                         |
| Corner radius           | 12dp en campos, 28dp en botón                          |

### 2.3. Animaciones

| Animación                     | Tipo                        | Duración |
|-------------------------------|-----------------------------|----------|
| Logo aparece                  | FadeIn + SlideUp            | 600ms    |
| Campos de texto aparecen      | FadeIn secuencial           | 400ms    |
| Botón aparece                 | Scale + FadeIn              | 300ms    |
| Error shake                   | Horizontal shake            | 400ms    |
| Loading                       | CircularProgressIndicator   | Infinito |
| Transición a cartera          | SharedAxisX                 | 300ms    |

---

## 3. Validaciones

### 3.1. Validaciones de Email

| Regla                          | Mensaje de error                              |
|--------------------------------|-----------------------------------------------|
| Campo vacío                    | "Ingresa tu email corporativo"                |
| Formato inválido               | "Formato de email no válido"                  |
| Sin dominio corporativo        | "Usa tu email corporativo (@asesores.pe)"     |

### 3.2. Validaciones de Contraseña

| Regla                          | Mensaje de error                              |
|--------------------------------|-----------------------------------------------|
| Campo vacío                    | "Ingresa tu contraseña"                       |
| Menos de 6 caracteres          | "La contraseña debe tener al menos 6 caracteres"|

### 3.3. Validaciones de Negocio

| Regla                          | Mensaje de error                              |
|--------------------------------|-----------------------------------------------|
| Credenciales incorrectas       | "Email o contraseña incorrectos"              |
| Cuenta desactivada             | "Tu cuenta ha sido desactivada. Contacta a tu jefe de agencia" |
| Error de red                   | "Sin conexión. Verifica tu internet"          |
| Supabase caído                 | "Servicio temporalmente no disponible"        |
| Demasiados intentos            | "Demasiados intentos. Intenta en X minutos"   |

---

## 4. Estados de la Pantalla

### 4.1. LoginUiState

```kotlin
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val rememberSession: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val loginSuccess: Boolean = false,
    val asesorNombre: String? = null
)
```

### 4.2. Diagrama de Estados

```
┌──────────┐     click      ┌────────────┐
│  IDLE    │───────────────►│ VALIDATING │
│ (form)   │                └──────┬─────┘
└──────────┘                       │
     ▲                    ┌────────┴────────┐
     │                    │                 │
     │              válido │           inválido
     │                    ▼                 ▼
     │            ┌────────────┐    ┌────────────┐
     │            │  LOADING   │    │   ERROR    │
     │            │ (spinner)  │    │ (campos)   │
     │            └──────┬─────┘    └────────────┘
     │                   │
     │          ┌────────┴────────┐
     │          │                 │
     │     éxito │           fallo
     │          ▼                 ▼
     │   ┌────────────┐    ┌────────────┐
     │   │  SUCCESS   │    │  ERROR     │
     │   │ (navegar)  │    │ (snackbar) │
     │   └────────────┘    └──────┬─────┘
     │                            │
     └────────────────────────────┘
```

---

## 5. Navegación

### 5.1. Flujo de Navegación

```
App Launch
  │
  ├─ Sesión activa ──────► CarteraScreen
  │
  └─ Sin sesión ──────► LoginScreen
                           │
                           ├─ Login exitoso ──► CarteraScreen
                           │
                           └─ ¿Olvidó contraseña? ──► (futuro: recovery)
```

### 5.2. Route

| Route           | Destino         | Params |
|-----------------|-----------------|--------|
| `auth/login`    | LoginScreen     | —      |
| `auth/splash`   | SplashScreen    | —      |

### 5.3. Transiciones

| De → A                    | Animación                |
|---------------------------|--------------------------|
| Splash → Login            | CrossFade (500ms)        |
| Splash → Cartera          | CrossFade (500ms)        |
| Login → Cartera           | SlideInHorizontal (300ms)|

---

## 6. ViewModel

### 6.1. LoginViewModel

```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val getSessionUseCase: GetSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Eventos
    fun onEmailChanged(email: String)
    fun onPasswordChanged(password: String)
    fun onTogglePasswordVisibility()
    fun onToggleRememberSession()
    fun onLoginClicked()
    fun onErrorDismissed()

    // Verificar sesión existente
    fun checkExistingSession()
}
```

### 6.2. Intents / Eventos

| Evento                     | Acción                                  |
|----------------------------|-----------------------------------------|
| `onEmailChanged`           | Actualizar email, limpiar error email   |
| `onPasswordChanged`        | Actualizar password, limpiar error pwd  |
| `onTogglePasswordVisibility`| Toggle isPasswordVisible               |
| `onToggleRememberSession`  | Toggle rememberSession                  |
| `onLoginClicked`           | Validar → llamar loginUseCase           |
| `onErrorDismissed`         | Limpiar errorMessage                    |
| `checkExistingSession`     | Verificar token en SharedPrefs/DataStore|

---

## 7. Integración Supabase Auth

### 7.1. Flujo de Autenticación

```
LoginViewModel
  │
  └──► LoginUseCase
         │
         └──► AuthRepository (interface)
                │
                └──► AuthRepositoryImpl
                       │
                       ├──► Supabase Auth API (signInWithEmail)
                       │
                       └──► DataStore (guardar sesión)
```

### 7.2. API Calls

| Operación          | Supabase Method                           | Endpoint               |
|--------------------|-------------------------------------------|------------------------|
| Login              | `auth.signInWith(Email) { email, password }`| POST /auth/v1/token   |
| Verificar sesión   | `auth.currentSessionOrNull()`             | GET /auth/v1/user      |
| Refresh token      | `auth.refreshCurrentSession()`            | POST /auth/v1/token    |
| Logout             | `auth.signOut()`                          | POST /auth/v1/logout   |

### 7.3. Manejo de Sesiones

| Escenario                  | Comportamiento                                          |
|----------------------------|---------------------------------------------------------|
| App abierta, sesión válida | Navegar directo a CarteraScreen                         |
| App abierta, sesión expirada| Intentar refresh token; si falla → LoginScreen         |
| Recordar sesión = true     | Guardar tokens en EncryptedSharedPreferences            |
| Recordar sesión = false    | Tokens solo en memoria (sesión volátil)                 |
| Token refresh exitoso      | Actualizar tokens almacenados                           |
| Token refresh fallido      | Limpiar sesión → LoginScreen                            |

### 7.4. Configuración del Cliente Supabase

```kotlin
object SupabaseConfig {
    const val SUPABASE_URL = "https://srxoisgexbcifdpwetxo.supabase.co"
    const val SUPABASE_KEY = "sb_publishable_lYyLWaJxbM-lCJ3eH_wrgg_t-UnR_lC"
}

val supabaseClient = createSupabaseClient(
    supabaseUrl = SupabaseConfig.SUPABASE_URL,
    supabaseKey = SupabaseConfig.SUPABASE_KEY
) {
    install(Auth) {
        flowType = FlowType.PKCE
        scheme = "bbvafv"
        host = "login"
    }
    install(Postgrest)
    install(Storage)
}
```

---

## 8. Data Flow

```
[LoginScreen]
     │ onLoginClicked()
     ▼
[LoginViewModel]
     │ loginUseCase(email, password)
     ▼
[LoginUseCase]
     │ validate(email, password)
     │ authRepository.signIn(email, password)
     ▼
[AuthRepositoryImpl]
     │ supabaseClient.auth.signInWith(Email)
     │ sessionStore.save(session)
     ▼
[Result<AsesorSession>]
     │
     ├── Success → uiState.copy(loginSuccess = true)
     │              → Navegar a CarteraScreen
     │
     └── Error → uiState.copy(errorMessage = error.message)
```

---

## 9. Casos Edge

| Caso                               | Comportamiento esperado                          |
|------------------------------------|--------------------------------------------------|
| Doble click en "Iniciar Sesión"    | Ignorar segundo click (isLoading = true)         |
| Rotación de pantalla durante login | Mantener estado (StateFlow en ViewModel)         |
| Sin internet                       | Mostrar error "Sin conexión"                     |
| Supabase timeout (>10s)            | Mostrar error "Servicio no disponible"           |
| Email con espacios                 | Trim automático                                  |
| Contraseña con caracteres unicode  | Aceptar sin modificaciones                       |
| Back button en login               | Cerrar la app (no navegar atrás)                 |
| Campo vacío + click login          | Mostrar errores inline en campos                 |

---

## 10. Criterios de Aceptación

- [ ] El asesor puede iniciar sesión con email y contraseña corporativa
- [ ] La validación de campos muestra errores inline debajo de cada campo
- [ ] El botón de login muestra loading spinner durante la petición
- [ ] Los errores de autenticación se muestran como Snackbar o banner
- [ ] La sesión se mantiene entre cierres de la app (si "recordar" está activo)
- [ ] Al abrir la app con sesión válida, se navega directo a Cartera
- [ ] El toggle de visibilidad de contraseña funciona correctamente
- [ ] La pantalla sigue el design system BBVA (colores, tipografía)
- [ ] Las animaciones de entrada son fluidas
- [ ] La pantalla funciona correctamente en orientación portrait
- [ ] El soft keyboard no oculta elementos importantes
- [ ] No hay memory leaks en rotación de pantalla
