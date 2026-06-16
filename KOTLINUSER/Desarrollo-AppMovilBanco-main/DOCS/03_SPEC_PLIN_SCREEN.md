# 📱 SPEC: PlinScreen — Pantalla PLIN

> **ID:** S1-06  
> **Sprint:** 1  
> **Estado:** 🔲 TODO  
> **Última actualización:** Sesión 1 - 19/05/2026

---

## 📋 Descripción

Pantalla de PLIN que permite al usuario acceder a las funcionalidades de pago PLIN: enviar a contactos, enviar a otro número, ver/pagar con QR, y operaciones adicionales como recargas, pago en línea, pago de impuestos, etc.

---

## 🎨 Diseño Visual (basado en mockup)

### Top Bar
- Fondo blanco
- Flecha de retroceso `<` a la izquierda (azul BBVA)
- Título centrado: **"Plin"**
- Iconos a la derecha: **(?)** ayuda y **(≡)** menú

### Hero Card PLIN
- Card con fondo azul oscuro (#004481) y bordes redondeados
- Título grande: **"Plin"** en blanco/bold
- Descripción: **"Paga más rápido a negocios y personas con la opción 'Ver / Pagar con QR'."**
- Link: **"Configuración de Plin"** en texto azul claro

### Acciones Principales (3 botones)
Fila de 3 botones circulares con fondo blanco/gris claro:
1. **Enviar a contactos** — icono de persona con flecha
2. **Enviar a otro número** — icono de billete con flecha
3. **Ver / Pagar con QR** — icono de QR

### Sección "Otras operaciones"
Título: **"Otras operaciones"**

Grid de 5 botones en fila (iconos circulares):
1. 🚌 **Recarga de bus** — icono de bus
2. 💻 **Pagar en línea** — icono de laptop (**← Este navega a PagarEnLineaScreen**)
3. 📋 **Pagar impuestos** — icono de documento
4. 🎰 **Raspa y gana** — icono de ticket
5. 📱 **Recarga de celular** — icono de celular

### Sección "Últimos movimientos"
- Título: **"Últimos movimientos"** + botón **"Buscar"** a la derecha
- Tabs: **"Finalizados"** | **"Pendientes"**
- Lista vacía o con movimientos PLIN del usuario

---

## 🎨 Paleta de Colores

| Color | Hex | Uso |
|-------|-----|-----|
| Azul BBVA Oscuro | `#004481` | Hero card fondo |
| Azul BBVA Medio | `#1973B8` | Links, iconos |
| Blanco | `#FFFFFF` | Fondo principal |
| Gris Claro | `#F4F4F4` | Fondo de iconos |
| Gris Texto | `#666666` | Texto secundario |
| Negro | `#1D252D` | Texto principal |

---

## 📐 Componentes Compose

```kotlin
// PlinScreen.kt
@Composable fun PlinScreen(...)           // Contenedor principal
@Composable fun PlinTopBar(...)           // Top bar con flecha y título
@Composable fun PlinHeroCard(...)         // Card azul con info de PLIN
@Composable fun PlinMainActions(...)      // 3 botones principales
@Composable fun PlinOtrasOperaciones(...) // Grid de operaciones
@Composable fun PlinMovimientos(...)      // Sección últimos movimientos
```

---

## 🔗 Navegación

| Acción | Destino |
|--------|---------|
| Click flecha `<` | Volver a Home |
| Click "Enviar a contactos" | `Screen.PlinEnviar` (Sprint 2) |
| Click "Enviar a otro número" | `Screen.PlinEnviarNumero` (Sprint 2) |
| Click "Ver / Pagar con QR" | `Screen.PlinQR` (Sprint 2) |
| **Click "Pagar en línea"** | **`Screen.PagarEnLinea`** ← Sprint 1 |
| Click "Recarga de bus" | `Screen.RecargaBus` (futuro) |
| Click "Pagar impuestos" | `Screen.PagarImpuestos` (futuro) |
| Click "Raspa y gana" | `Screen.RaspaGana` (futuro) |
| Click "Recarga de celular" | `Screen.RecargaCelular` (futuro) |

---

## 📊 Datos del ViewModel

```kotlin
// PlinViewModel.kt
data class PlinUiState(
    val movimientosFinalizados: List<PlinTransaccion>,
    val movimientosPendientes: List<PlinTransaccion>,
    val isLoading: Boolean = false,
    val error: String? = null
)
```

---

## 🗄️ Modelo de Datos

```kotlin
// PlinModels.kt
data class PlinTransaccion(
    val id: String,
    val userId: String,
    val tipo: String,           // "envio", "recepcion", "pago_linea"
    val destinatario: String?,  // nombre o número
    val comercio: String?,      // para pagos en línea
    val monto: Double,
    val estado: String,         // "completado", "pendiente", "fallido"
    val medioPago: String,      // "plin"
    val cuentaOrigenId: String,
    val fecha: String,
    val numeroOperacion: String
)
```

---

## ✅ Criterios de Aceptación

- [ ] Fondo blanco, diseño fiel al mockup BBVA
- [ ] Hero card azul con info de PLIN
- [ ] 3 botones de acciones principales visibles
- [ ] Grid de "Otras operaciones" con 5 iconos
- [ ] Click en "Pagar en línea" navega a `PagarEnLineaScreen`
- [ ] Sección de últimos movimientos con tabs Finalizados/Pendientes
- [ ] Top bar con botón de retroceso funcional
- [ ] Datos cargados desde Supabase vía ViewModel

---

*Documento SDD — Spec Driven Development*
