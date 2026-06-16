# 📱 SPEC: ConfirmacionPagoScreen — Pago Realizado

> **ID:** S1-08  
> **Sprint:** 1  
> **Estado:** 🔲 TODO  
> **Última actualización:** Sesión 1 - 19/05/2026

---

## 📋 Descripción

Pantalla de confirmación que se muestra después de un pago exitoso. Muestra un check verde grande, los datos del pago realizado y un mensaje de que se enviará la constancia al correo del usuario.

---

## 🎨 Diseño Visual (basado en mockup)

### Top Bar
- Fondo blanco
- Título a la izquierda: **"Pagar compra en línea"** en negro/bold
- Icono X (cerrar) a la derecha → navega al Home

### Ícono de Éxito
- Círculo verde grande (#00A650) con check ✓ blanco centrado
- Tamaño ~80dp

### Título de Éxito
- **"Pago realizado"** en negro, bold, ~22sp
- Subtexto: **"Ya puedes validar tu pago en la página del comercio donde realizaste la compra."** en gris

### Importe Pagado
- Título: **"Importe pagado"** en gris
- Monto: **"S/ 2,000"** en azul BBVA (#004481), bold, ~28sp

### Detalles del Pago
Filas con separadores:

| Campo | Valor (ejemplo) |
|-------|-----------------|
| Fecha y hora | **11 mar 2025 11:22** |
| Número de operación | **0102030405** |
| Comercio | **Adidas** |
| Medio de pago | **Plin** |
| Cuenta de origen | **Cuenta Digital •1234** |
| Comisión | **Gratis** |

### Banner de Constancia
- Card con fondo azul claro (#D4EDFC)
- Icono ℹ️ a la izquierda
- Texto: **"Enviaremos la constancia a este correo: ••••adeo@gmail.com"**
- Email parcialmente enmascarado

---

## 🎨 Paleta de Colores

| Color | Hex | Uso |
|-------|-----|-----|
| Blanco | `#FFFFFF` | Fondo principal |
| Negro | `#1D252D` | Texto títulos |
| Gris Texto | `#666666` | Texto secundario, labels |
| Gris Separador | `#E0E0E0` | Líneas divisorias |
| Verde Éxito | `#00A650` | Círculo check |
| Azul BBVA | `#004481` | Importe pagado |
| Azul Claro | `#D4EDFC` | Fondo banner constancia |
| Azul Info | `#1973B8` | Icono info |

---

## 📐 Componentes Compose

```kotlin
// ConfirmacionPagoScreen.kt
@Composable fun ConfirmacionPagoScreen(...)    // Contenedor principal
@Composable fun ConfirmacionTopBar(...)        // Top bar con X
@Composable fun CheckExitoIcon(...)            // Círculo verde con check
@Composable fun ImportePagado(...)             // Monto grande azul
@Composable fun DetallesPagoList(...)          // Lista de detalles
@Composable fun ConstanciaBanner(...)          // Banner azul de constancia
```

---

## 🔗 Navegación

| Acción | Destino |
|--------|---------|
| Click X (cerrar) | Navegar al Home (popUpTo Home) |
| Botón "Volver al inicio" (opcional) | Navegar al Home |

---

## 📊 Datos Recibidos

La pantalla recibe los datos del pago realizado, ya sea por argumentos de navegación o estado compartido:

```kotlin
data class ConfirmacionPagoData(
    val importePagado: Double,
    val fechaHora: String,
    val numeroOperacion: String,
    val comercio: String,
    val medioPago: String,
    val cuentaOrigen: String,      // "Cuenta Digital •1234"
    val comision: String,          // "Gratis" o monto
    val emailEnmascarado: String   // "••••adeo@gmail.com"
)
```

---

## 🔒 Lógica de Enmascaramiento de Email

```kotlin
fun enmascararEmail(email: String): String {
    val parts = email.split("@")
    if (parts.size != 2) return email
    val nombre = parts[0]
    val dominio = parts[1]
    val visible = nombre.takeLast(4.coerceAtMost(nombre.length))
    val masked = "•".repeat((nombre.length - visible.length).coerceAtLeast(0))
    return "$masked$visible@$dominio"
}
// "alexmoisesadeo@gmail.com" → "••••••••••adeo@gmail.com"
```

---

## 🔄 Generación de Número de Operación

```kotlin
fun generarNumeroOperacion(): String {
    val timestamp = System.currentTimeMillis()
    return timestamp.toString().takeLast(10).padStart(10, '0')
}
// Ejemplo: "0102030405"
```

---

## ✅ Criterios de Aceptación

- [ ] Diseño fiel al mockup BBVA (fondo blanco)
- [ ] Check verde grande visible
- [ ] Texto "Pago realizado" con descripción
- [ ] Importe pagado en azul grande
- [ ] Tabla de detalles con: fecha, nro operación, comercio, medio pago, cuenta, comisión
- [ ] Banner de constancia con email enmascarado
- [ ] Botón X cierra y vuelve al Home
- [ ] Datos reales del pago procesado

---

*Documento SDD — Spec Driven Development*
