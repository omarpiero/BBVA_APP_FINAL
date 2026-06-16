# 📱 SPEC: HomeScreen — Pantalla de Inicio BBVA

> **ID:** S1-01  
> **Sprint:** 1  
> **Estado:** 🔲 TODO  
> **Última actualización:** Sesión 1 - 19/05/2026

---

## 📋 Descripción

Rediseñar la pantalla Home para que coincida con el mockup de BBVA proporcionado. La pantalla usa un fondo **blanco/claro** (no oscuro como el actual) y sigue el diseño BBVA Perú.

## 🎨 Diseño Visual (basado en mockup)

### Header
- Fondo blanco con texto oscuro
- Saludo: **"Hola, {nombre}"** en texto azul oscuro (#004481)
- Iconos a la derecha: **Ayuda (?)** y **Menú (≡)** en azul BBVA
- Avatar circular del usuario (opcional)

### Sección Ingresos/Gastos
- Card con borde gris claro, fondo blanco
- **Ingresos:** `S/ 9,999.99` con barra de progreso amarilla/dorada
- **Gastos:** `S/ 100.00` con barra de progreso azul
- Botón: **"Ir a mi día a día"** en texto azul

### Accesos Rápidos (Quick Actions)
- 4 botones circulares en fila horizontal:
  - 🔄 **Transferir** — icono de flechas
  - 📱 **PLIN** — icono PLIN (texto "plin" con fondo azul oscuro)
  - 💱 **T-Cambio** — icono de monedas
  - ⋯ **Más** — icono de tres puntos
- Cada botón navega a su respectiva pantalla
- **PLIN navega a `PlinScreen`**

### Banner Promocional
- Card con fondo gris claro
- Icono ✅ a la izquierda
- Texto: **"¡Felicidades!"** + subtexto
- Botón X para cerrar
- Link: **"Consúltalas aquí"** en azul

### Sección Cuentas
- Título: **"Cuentas"** con saldo total a la derecha: `S/ 9,999.99`
- Card de cuenta con:
  - Nombre: **"Cuenta independencia"**
  - Número enmascarado: **•2339**
  - Saldo: `S/ 9,999.99`
  - Subtexto: **"Saldo disponible"**

### Bottom Navigation Bar
- 5 items: **Inicio** | **Opera** | **Para mí** (central con +) | **Notificaci...** | **Contacto**
- Item activo: icono con fondo circular azul
- Colores: activo = azul BBVA, inactivo = gris

---

## 🎨 Paleta de Colores (Modo Claro BBVA)

| Color | Hex | Uso |
|-------|-----|-----|
| Azul BBVA Primario | `#004481` | Texto header, iconos activos |
| Azul BBVA Medio | `#1973B8` | Botones, links |
| Azul BBVA Claro | `#D4EDFC` | Fondos de cards seleccionadas |
| Blanco | `#FFFFFF` | Fondo principal |
| Gris Fondo | `#F4F4F4` | Fondo de cards |
| Gris Texto | `#666666` | Texto secundario |
| Gris Borde | `#E0E0E0` | Bordes de cards |
| Amarillo/Dorado | `#F5C842` | Barra ingresos |
| Negro | `#1D252D` | Texto principal |

---

## 📐 Componentes Compose

### Nuevos Composables Requeridos
```kotlin
// HomeScreen.kt - Reestructurar completamente
@Composable fun HomeScreen(...)         // Contenedor principal
@Composable fun HeaderBBVA(...)         // Header con saludo
@Composable fun ResumenCard(...)        // Card ingresos/gastos
@Composable fun QuickActionsRow(...)    // Fila de accesos rápidos
@Composable fun BannerPromo(...)        // Banner promocional
@Composable fun CuentasSection(...)     // Sección de cuentas
```

### Interacciones
| Acción | Comportamiento |
|--------|----------------|
| Click en PLIN | Navegar a `Screen.Plin` |
| Click en Transferir | Navegar a `Screen.Transfer` (futuro) |
| Click en T-Cambio | Navegar a `Screen.TCambio` (futuro) |
| Click en Más | Navegar a `Screen.Opera` |
| Click en cuenta | Navegar a `Screen.Cuenta` |
| Click en Menú (≡) | Abrir Drawer lateral |
| Click en Ayuda (?) | Mostrar modal de ayuda |
| Click en "Ir a mi día a día" | Navegar a detalle de transacciones |

---

## 📊 Datos del ViewModel

```kotlin
data class HomeUiState(
    val nombre: String,
    val ingresos: Double,
    val gastos: Double,
    val cuentas: List<Cuenta>,
    val transacciones: List<Transaccion>
)
```

---

## ✅ Criterios de Aceptación

- [ ] Fondo blanco/claro (no oscuro)
- [ ] Header muestra nombre del usuario
- [ ] Resumen muestra ingresos y gastos con barras de progreso
- [ ] Quick actions muestra 4 botones (Transferir, PLIN, T-Cambio, Más)
- [ ] Click en PLIN navega a PlinScreen
- [ ] Sección Cuentas muestra cuentas del usuario con saldos
- [ ] Bottom navigation funcional con 5 tabs
- [ ] Drawer lateral se abre con el botón Menú

---

*Documento SDD — Spec Driven Development*
