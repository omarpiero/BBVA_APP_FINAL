# 📱 SPEC: PagarEnLineaScreen — Pagar Compra en Línea

> **ID:** S1-07  
> **Sprint:** 1  
> **Estado:** 🔲 TODO  
> **Última actualización:** Sesión 1 - 19/05/2026

---

## 📋 Descripción

Pantalla modal/fullscreen que muestra la información de un pago en línea antes de confirmarlo. El usuario ve los datos del comercio, importe, medio de pago, cuenta de origen y confirma el pago deslizando un botón.

---

## 🎨 Diseño Visual (basado en mockup)

### Top Bar
- Fondo blanco
- Título a la izquierda: **"Pagar compra en línea"** en negro/bold
- Icono X (cerrar) a la derecha

### Sección "Información del pago"
- Título: **"Información del pago"** en negro/bold
- Filas de detalle con separadores:

| Campo | Valor (ejemplo) |
|-------|-----------------|
| Comercio | **Adidas** |
| Importe | **S/ 2,000** |
| Medio de pago | **Plin** |

### Sección "Cuenta de origen"
- Título: **"Cuenta de origen"** en negro/bold
- Card con:
  - Icono circular azul/teal a la izquierda (logo de cuenta)
  - **"Cuenta Digital"** — nombre de la cuenta
  - **"•1234"** — número enmascarado
  - **"S/ 10,000"** — saldo de la cuenta
  - **"Saldo disponible"** — subtexto gris

### Sección "Importe a pagar"
- **"S/ 2,000"** en azul grande (#004481), bold, ~28sp
- **"Comisión: Gratis"** en texto gris

### Botón Confirmar
- Botón verde (#00A650) con bordes redondeados
- Texto: **"Confirmar"** en blanco
- Icono circular rojo/oscuro de fingerprint/slide a la derecha
- Efecto de deslizamiento (slide to confirm) — o click normal
- Al confirmar → procesa pago → navega a `ConfirmacionPagoScreen`

---

## 🎨 Paleta de Colores

| Color | Hex | Uso |
|-------|-----|-----|
| Blanco | `#FFFFFF` | Fondo principal |
| Negro | `#1D252D` | Texto títulos |
| Gris Texto | `#666666` | Texto secundario |
| Gris Separador | `#E0E0E0` | Líneas divisorias |
| Azul BBVA | `#004481` | Importe a pagar |
| Verde Confirmar | `#00A650` | Botón confirmar |
| Teal/Azul Cuenta | `#008B8B` | Icono de cuenta |
| Rojo Slide | `#8B0000` | Círculo de slide |

---

## 📐 Componentes Compose

```kotlin
// PagarEnLineaScreen.kt
@Composable fun PagarEnLineaScreen(...)      // Contenedor principal
@Composable fun PagarTopBar(...)             // Top bar con X
@Composable fun InfoPagoSection(...)         // Información del pago
@Composable fun CuentaOrigenCard(...)        // Card de cuenta de origen
@Composable fun ImporteAPagar(...)           // Importe grande + comisión
@Composable fun BotonConfirmar(...)          // Botón verde de confirmar
```

---

## 🔗 Navegación

| Acción | Destino |
|--------|---------|
| Click X (cerrar) | Volver a `PlinScreen` |
| Click "Confirmar" | Procesar pago → `Screen.ConfirmacionPago` |

---

## 📊 Datos del ViewModel

```kotlin
// PagarEnLineaViewModel.kt
data class PagarEnLineaUiState(
    val comercio: Comercio? = null,
    val cuentaOrigen: Cuenta? = null,
    val importe: Double = 0.0,
    val medioPago: String = "Plin",
    val comision: Double = 0.0,
    val isProcessing: Boolean = false,
    val pagoExitoso: Boolean = false,
    val error: String? = null,
    val resultadoPago: PagoEnLinea? = null
)
```

---

## 🗄️ Modelo de Datos

```kotlin
// PagarEnLineaModels.kt
data class Comercio(
    val id: String,
    val nombre: String,
    val categoria: String,    // "ropa", "tecnologia", "comida", etc.
    val logoUrl: String?,
    val activo: Boolean = true
)

data class PagoEnLinea(
    val id: String,
    val userId: String,
    val comercioId: String,
    val comercioNombre: String,
    val cuentaOrigenId: String,
    val monto: Double,
    val comision: Double = 0.0,
    val medioPago: String = "Plin",
    val estado: String,           // "completado", "pendiente", "fallido"
    val numeroOperacion: String,
    val fecha: String
)
```

---

## 🔄 Flujo de Procesamiento

```
1. Usuario llega a la pantalla con datos del comercio y monto
2. Se carga la cuenta de origen del usuario (cuenta principal/corriente)
3. Se muestra la información del pago
4. Usuario presiona "Confirmar"
5. Se valida que el saldo sea suficiente
6. Se descuenta el monto de la cuenta
7. Se registra el pago en `pagos_en_linea`
8. Se registra la transacción en `transacciones`
9. Se genera número de operación
10. Se navega a ConfirmacionPagoScreen con el resultado
```

---

## ⚠️ Validaciones

| Validación | Acción |
|-----------|--------|
| Saldo insuficiente | Mostrar error, no procesar |
| Error de red | Mostrar error, permitir reintentar |
| Monto = 0 | No permitir confirmar |
| Cuenta no encontrada | Mostrar error |

---

## ✅ Criterios de Aceptación

- [ ] Diseño fiel al mockup BBVA (fondo blanco)
- [ ] Muestra información del comercio, importe y medio de pago
- [ ] Muestra cuenta de origen con saldo disponible
- [ ] Muestra importe a pagar en grande + comisión
- [ ] Botón confirmar verde funcional
- [ ] Validación de saldo suficiente
- [ ] Al confirmar exitosamente → navega a ConfirmacionPago
- [ ] Descuenta saldo de la cuenta
- [ ] Registra pago en BD

---

*Documento SDD — Spec Driven Development*
