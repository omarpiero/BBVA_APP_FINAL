# 📱 BBVA - App Bancaria Móvil

## Visión General del Proyecto

**Nombre de la App:** BBVA  
**Plataforma:** Android (Kotlin + Jetpack Compose)  
**Backend:** Supabase (PostgreSQL + Auth + REST API)  
**Arquitectura:** MVVM (Model-View-ViewModel)  
**Metodología:** Spec Driven Development (SDD)

---

## 🏗️ Arquitectura Actual

```
com.example.appbanco_s8/
├── MainActivity.kt                    # Entry point con NavController
├── data/
│   ├── model/                         # Data classes (DTOs)
│   │   ├── AuthModels.kt             # LoginRequest, AuthResponse, UserData
│   │   ├── CuentaModels.kt           # Cuenta, Transaccion, CuentaAhorro
│   │   ├── OperaModels.kt            # Tarjeta, Pago
│   │   ├── PerfilModels.kt           # Perfil
│   │   └── PrestamoModels.kt         # Prestamo
│   ├── remote/                        # Retrofit + Supabase
│   │   ├── RetrofitClient.kt         # Singleton OkHttp + Retrofit
│   │   ├── SupabaseApi.kt            # Endpoints REST (cuentas, transacciones, etc.)
│   │   ├── SupabaseAuthApi.kt        # Endpoint auth/v1/token
│   │   └── SupabaseConfig.kt         # BASE_URL + ANON_KEY
│   └── repository/                    # Capa de abstracción de datos
│       ├── AuthRepository.kt
│       ├── CuentaRepository.kt
│       ├── PagoRepository.kt
│       ├── PrestamoRepository.kt
│       └── TarjetaRepository.kt
├── navigation/
│   ├── NavGraph.kt                    # Composable de navegación (NavHost)
│   └── Screen.kt                     # Sealed class con rutas
└── ui/
    ├── components/
    │   ├── AppScaffold.kt             # Scaffold global (drawer + bottom nav)
    │   ├── BankCard.kt                # (vacío)
    │   ├── BottomNavBar.kt            # (vacío - integrado en AppScaffold)
    │   ├── MovimientoItem.kt          # (vacío)
    │   └── QuickActions.kt            # (vacío)
    ├── screens/
    │   ├── SplashActivity.kt          # Splash nativo
    │   ├── LoginScreen.kt             # ✅ Implementado
    │   ├── HomeScreen.kt              # ✅ Implementado (parcial)
    │   ├── OperaScreen.kt             # ✅ Implementado (básico)
    │   ├── ConfigScreen.kt            # ✅ Implementado
    │   ├── ContactoScreen.kt          # ✅ Implementado
    │   ├── NotificaScreen.kt          # ✅ Implementado
    │   ├── CuentaScreen.kt            # 🔲 Stub
    │   ├── PerfilScreen.kt            # 🔲 Stub
    │   ├── PrestamoScreen.kt          # 🔲 Stub
    │   └── TarjetaScreen.kt           # 🔲 Stub
    ├── theme/
    │   ├── Color.kt                   # Paleta de colores BBVA
    │   ├── Theme.kt                   # Material3 dark/light schemes
    │   └── Type.kt                    # Tipografía
    └── viewmodel/
        ├── UiState.kt                # DataUiState sealed class genérica
        ├── AuthViewModel.kt          # Login flow
        ├── ConfigViewModel.kt        # Perfil CRUD
        ├── CuentaViewModel.kt        # Cuentas + ahorro
        ├── HomeViewModel.kt          # Cuentas + transacciones
        ├── OperaViewModel.kt         # Pagos
        ├── PrestamoViewModel.kt      # Préstamos
        └── TarjetaViewModel.kt       # Tarjetas
```

---

## 🗄️ Base de Datos (Supabase / PostgreSQL)

### Tablas Existentes
| Tabla | Descripción | Estado |
|-------|-------------|--------|
| `cuentas` | Cuentas corriente/ahorro del usuario | ✅ Activa |
| `cuentas_ahorro` | Detalle de cuentas de ahorro | ✅ Activa |
| `transacciones` | Movimientos débito/crédito | ✅ Activa |
| `tarjetas` | Tarjetas débito/crédito | ✅ Activa |
| `pagos` | Pagos de servicios | ✅ Activa |
| `perfiles` | Datos del perfil de usuario | ✅ Activa |
| `solicitudes_prestamo` | Solicitudes de préstamos | ✅ Activa |

### Tablas Nuevas Requeridas
| Tabla | Descripción | Sprint |
|-------|-------------|--------|
| `plin_contactos` | Contactos PLIN del usuario | Sprint 1 |
| `plin_transacciones` | Historial de pagos PLIN | Sprint 1 |
| `comercios` | Catálogo de comercios para pago en línea | Sprint 1 |
| `pagos_en_linea` | Registro de pagos en línea realizados | Sprint 1 |

---

## 🔗 Conexión con Supabase

- **URL Base:** `https://moiacpafbhhpvlflkfpg.supabase.co/`
- **Autenticación:** Bearer token vía `auth/v1/token` (email + password)
- **API REST:** Endpoints `rest/v1/{tabla}` con RLS habilitado
- **Cliente HTTP:** Retrofit2 + OkHttp3 + Gson

---

## 📋 Dependencias Principales

| Dependencia | Versión | Uso |
|------------|---------|-----|
| Jetpack Compose BOM | latest | UI declarativa |
| Material3 | latest | Componentes y theming |
| Navigation Compose | 2.7.7 | Navegación entre pantallas |
| Retrofit2 | 2.9.0 | HTTP client para Supabase |
| Coroutines | 1.7.3 | Operaciones asíncronas |
| ViewModel Compose | 2.7.0 | State management MVVM |
| Material Icons Extended | 1.6.7 | Iconografía |
| SplashScreen API | 1.0.1 | Splash nativo |

---

## 📝 Documentación SDD

| Documento | Descripción |
|-----------|-------------|
| [00_PROJECT_OVERVIEW.md](./00_PROJECT_OVERVIEW.md) | Este documento - Visión general |
| [01_KANBAN_BOARD.md](./01_KANBAN_BOARD.md) | Tablero Kanban / Sprints |
| [02_SPEC_HOME_SCREEN.md](./02_SPEC_HOME_SCREEN.md) | Spec: Pantalla Home (BBVA) |
| [03_SPEC_PLIN_SCREEN.md](./03_SPEC_PLIN_SCREEN.md) | Spec: Pantalla PLIN |
| [04_SPEC_PAGAR_EN_LINEA.md](./04_SPEC_PAGAR_EN_LINEA.md) | Spec: Pagar en Línea |
| [05_SPEC_CONFIRMACION_PAGO.md](./05_SPEC_CONFIRMACION_PAGO.md) | Spec: Confirmación de Pago |
| [06_DATABASE_CHANGES.md](./06_DATABASE_CHANGES.md) | Cambios en BD para el flujo |
| [database.md](./database.md) | Schema original de la BD |

---

*Última actualización: Sesión 1 - 19/05/2026*
