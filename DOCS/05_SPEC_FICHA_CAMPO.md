# 05 — SPEC: FICHA DE CAMPO

| Campo               | Valor                                                        |
|---------------------|--------------------------------------------------------------|
| **ID**              | SDD-05                                                       |
| **Sprint**          | Sprint 4 — Ficha de Campo                                    |
| **Estado**          | 📋 Especificado                                               |
| **Última revisión** | 2026-05-26                                                   |
| **Épica**           | E-05 Ficha de Campo · E-06 Motor de Scoring                  |
| **Prioridad**       | 🔴 Crítica — Documento más importante del proyecto            |

---

## 1. Objetivo

Implementar el formulario de **evaluación crediticia de campo** utilizado por los asesores durante las visitas a clientes preaprobados. La ficha está dividida en **5 secciones (F1-F5)** y permite calcular el **score de campo** (máx 200 pts), el **score final** (transaccional + campo = máx 1000 pts), determinar el segmento resultante y generar una propuesta de crédito.

> ⚠️ **IMPORTANTE:** Toda la lógica financiera y de scoring debe vivir en `domain/usecases`. Los composables solo muestran datos y capturan inputs. NO colocar cálculos en la capa de presentación.

---

## 2. Estructura General F1-F5

### 2.1. Secciones de la Ficha

| Sección | Nombre                  | Puntaje Máximo | Propósito                       |
|---------|-------------------------|----------------|---------------------------------|
| **F1**  | Verificación del Negocio| 60 pts         | ¿El negocio existe y es real?   |
| **F2**  | Capacidad de Pago Real  | 60 pts         | ¿Puede pagar la cuota?          |
| **F3**  | Deuda Informal          | 40 pts*        | ¿Tiene deudas ocultas?          |
| **F4**  | Activos y Respaldo      | 40 pts         | ¿Tiene respaldo patrimonial?    |
| **F5**  | Carácter del Cliente    | Sin puntaje    | ¿Es de fiar? (veto / alerta)    |

> *F3 puede ser negativo (-50 a +40 pts) — penaliza deudas informales significativas.

### 2.2. Flujo del Wizard

```
┌────────────────────────────────────────────────┐
│                                                │
│  F1 ──► F2 ──► F3 ──► F4 ──► F5 ──► Resumen   │
│  │       │       │       │       │       │     │
│  ▼       ▼       ▼       ▼       ▼       ▼     │
│ 60pts  60pts   40pts*  40pts   veto   TOTAL    │
│                                      1000pts   │
│                                                │
│  En cualquier punto:                           │
│  · Si F1.negocio_verificado = false → DESCAL.  │
│  · Si F5.caracter = veto → DESCAL.             │
│                                                │
│  Puede guardar progreso parcial (offline)      │
└────────────────────────────────────────────────┘
```

---

## 3. Diseño UX / UI General

### 3.1. Layout del Wizard

```
┌─────────────────────────────────┐
│ ← Ficha de Campo         💾 ⋮  │ ← TopAppBar
├─────────────────────────────────┤
│ ● ○ ○ ○ ○ ○                    │ ← StepIndicator
│ F1: Verificación del Negocio    │
├─────────────────────────────────┤
│                                 │
│  [Contenido de la sección]      │
│  [Campos del formulario]        │
│  [Puntaje parcial: XX pts]      │
│                                 │
├─────────────────────────────────┤
│                                 │
│  [← Anterior]    [Siguiente →]  │ ← Navigation buttons
│                                 │
└─────────────────────────────────┘
```

### 3.2. Componentes Comunes

| Componente         | Uso                                           |
|--------------------|-----------------------------------------------|
| StepIndicator      | 6 pasos (F1-F5 + Resumen), con progreso       |
| ScoreChip          | Muestra puntaje parcial por sección            |
| RadioGroup         | Selección única (opciones de rangos)           |
| DropdownField      | Selección de opciones predefinidas             |
| CurrencyField      | Input numérico con formato S/ X,XXX.XX         |
| ObservationField   | TextArea para observaciones (obligatorio/opc)  |
| SectionCard        | Card contenedora por grupo de campos           |
| AlertBanner        | Banner de descalificación (rojo)               |
| OfflineBadge       | Indicador "Guardado offline"                   |

---

## 4. F1: Verificación del Negocio (máx 60 pts)

### 4.1. Wireframe F1

```
┌─────────────────────────────────┐
│ F1: Verificación del Negocio    │
├─────────────────────────────────┤
│                                 │
│ ¿El negocio fue verificado?     │
│ ○ Sí                            │
│ ○ No → [⚠️ DESCALIFICACIÓN]     │
│                                 │
│ Si No: Motivo *                 │
│ ┌─────────────────────────┐     │
│ │ Describe el motivo...   │     │
│ └─────────────────────────┘     │
│                                 │
│ ─────────────────────────       │
│                                 │
│ Antigüedad del negocio          │
│ ○ Menos de 1 año       → 0 pts │
│ ○ 1 a 3 años           → 20 pts│
│ ○ Más de 3 años        → 40 pts│
│                                 │
│ Tenencia del local              │
│ ○ Alquilado sin contrato → 0 pts│
│ ○ Alquilado con contrato→ 10 pts│
│ ○ Propio                → 20 pts│
│                                 │
│ Dirección verificada            │
│ ┌─────────────────────────┐     │
│ │ Dirección observada...  │     │
│ └─────────────────────────┘     │
│                                 │
│ Puntaje F1: 40/60 pts           │
├─────────────────────────────────┤
│ [← Anterior]    [Siguiente →]   │
└─────────────────────────────────┘
```

### 4.2. Campos F1

| Campo                    | Tipo         | Opciones / Reglas                                          | Pts   |
|--------------------------|--------------|-------------------------------------------------------------|-------|
| `negocio_verificado`     | Boolean      | **Sí** / **No** (obligatorio)                              | —     |
| `motivo_no_verificado`   | TextArea     | Obligatorio si `negocio_verificado = false`                | —     |
| `antiguedad_negocio`     | Radio        | `menos_1_anio`=0 · `1_a_3_anios`=20 · `mas_3_anios`=40     | 0-40  |
| `tenencia_local`         | Radio        | `alquilado_sin_contrato`=0 · `alquilado_con_contrato`=10 · `propio`=20 | 0-20  |
| `direccion_verificada`   | TextField    | Texto libre, dirección observada in situ                    | —     |

### 4.3. Reglas F1

| Regla                                        | Acción                                        |
|----------------------------------------------|-----------------------------------------------|
| `negocio_verificado = false`                 | **DESCALIFICACIÓN INMEDIATA** — flujo de veto |
| `antiguedad_negocio` no seleccionado         | No permite avanzar a F2                       |
| `tenencia_local` no seleccionado             | No permite avanzar a F2                       |
| `pts_f1 = pts_antiguedad + pts_tenencia`     | Calculado automáticamente                     |

### 4.4. Descalificación desde F1

```
negocio_verificado = false
     │
     ▼
┌──────────────────────────────────┐
│ ⚠️ DESCALIFICACIÓN               │
│                                  │
│ El negocio no fue verificado.    │
│ Se requiere motivo obligatorio.  │
│                                  │
│ Motivo: *                        │
│ ┌──────────────────────────┐     │
│ │                          │     │
│ └──────────────────────────┘     │
│                                  │
│ [Confirmar Descalificación]      │
│ [Cancelar]                       │
└──────────────────────────────────┘
     │
     ▼
Ficha → estado = "cancelada"
Crédito → estado = "cancelado"
Segmento → "DESCALIFICADO"
```

---

## 5. F2: Capacidad de Pago Real (máx 60 pts)

### 5.1. Campos F2

| Campo                    | Tipo         | Opciones / Reglas                                    | Pts   |
|--------------------------|--------------|-------------------------------------------------------|-------|
| `ventas_diarias_rango`   | Radio        | `menos_50`=0 · `50_a_150`=15 · `151_a_300`=30 · `mas_300`=45 | 0-45  |
| `ventas_mensuales_est`   | Currency     | Calculado: rango × 26 días hábiles (editable)        | —     |
| `gastos_fijos_mes`       | Currency     | Monto de gastos fijos mensuales (alquiler, luz, etc.) | —     |
| `ratio_gastos`           | Radio        | `mas_80pct`=0 · `50_a_80pct`=5 · `menos_50pct`=15     | 0-15  |
| `ingreso_consistente`    | Boolean      | ¿Los ingresos observados son consistentes?            | —     |
| `obs_inconsistencia`     | TextArea     | Obligatorio si `ingreso_consistente = false`          | —     |

### 5.2. Cálculos F2

```
pts_f2 = pts_ventas + pts_gastos

Ventas mensuales estimadas (sugerencia):
  - menos_50:   S/ 50 × 26 = S/ 1,300
  - 50_a_150:   S/ 100 × 26 = S/ 2,600
  - 151_a_300:  S/ 225 × 26 = S/ 5,850
  - mas_300:    S/ 400 × 26 = S/ 10,400

Ratio de gastos:
  ratio = gastos_fijos_mes / ventas_mensuales_est × 100
  El asesor confirma el rango observado
```

### 5.3. Reglas F2

| Regla                                          | Acción                                  |
|------------------------------------------------|-----------------------------------------|
| `ingreso_consistente = false`                  | Alerta amarilla, no bloquea             |
| Ventas = 0 y ratio_gastos = "mas_80pct"        | Warning: cliente de alto riesgo         |
| Todos los campos obligatorios para avanzar     | Validación al click "Siguiente"         |

---

## 6. F3: Deuda Informal (máx 40 pts, puede ser negativo)

### 6.1. Campos F3

| Campo                    | Tipo         | Opciones / Reglas                                    | Pts     |
|--------------------------|--------------|-------------------------------------------------------|---------|
| `tiene_deuda_informal`   | Radio        | `si_significativa`=-50 · `si_menor`=-20 · `no`=+20    | -50/+20 |
| `monto_deuda_informal`   | Currency     | Obligatorio si tiene deuda                             | —       |
| `detalle_deuda`          | TextArea     | Detalle de la deuda informal                           | —       |
| `participa_pandero`      | Radio        | `si_mayor_cuota`=-20 · `si_menor_cuota`=0 · `no`=+20  | -20/+20 |
| `aporte_pandero_mes`     | Currency     | Obligatorio si participa en pandero                    | —       |

### 6.2. Cálculos F3

```
pts_f3 = pts_deuda_informal + pts_pandero

Rango posible: -70 a +40 pts

Escenarios:
  MEJOR CASO:  no deuda(+20) + no pandero(+20) = +40 pts
  PEOR CASO:   deuda significativa(-50) + pandero mayor(-20) = -70 pts
```

### 6.3. Reglas F3

| Regla                                        | Acción                                      |
|----------------------------------------------|---------------------------------------------|
| `tiene_deuda_informal = si_significativa`    | Alerta roja: "Riesgo alto por deuda informal"|
| Deuda informal > 50% de ventas mensuales     | Warning informativo al asesor               |
| Pandero > 20% de ingresos                   | Warning informativo al asesor               |

---

## 7. F4: Activos y Respaldo (máx 40 pts)

### 7.1. Campos F4

| Campo                    | Tipo         | Opciones / Reglas                                    | Pts   |
|--------------------------|--------------|-------------------------------------------------------|-------|
| `stock_visible`          | Radio        | `escaso`=0 · `moderado`=10 · `abundante`=20            | 0-20  |
| `activos_hogar`          | Radio        | `ninguno`=0 · `al_menos_uno`=20                        | 0-20  |
| `descripcion_activos`    | TextArea     | Descripción de activos observados (opcional)           | —     |

### 7.2. Cálculos F4

```
pts_f4 = pts_stock + pts_activos
Rango: 0 a 40 pts
```

---

## 8. F5: Carácter del Cliente (veto / alerta)

### 8.1. Campos F5

| Campo                    | Tipo         | Opciones / Reglas                                    |
|--------------------------|--------------|-------------------------------------------------------|
| `caracter_resultado`     | Radio        | `sin_penalidad` · `alerta` · `veto`                   |
| `obs_caracter`           | TextArea     | Obligatorio si `alerta` o `veto`                      |

### 8.2. Reglas F5

| Regla                                 | Acción                                        |
|---------------------------------------|-----------------------------------------------|
| `caracter_resultado = veto`           | **DESCALIFICACIÓN INMEDIATA**                 |
| `caracter_resultado = alerta`         | Warning: "Elevará automáticamente a comité"   |
| `obs_caracter` obligatorio si ≠ sin_penalidad | Validación al click "Siguiente"       |

### 8.3. Descalificación desde F5

```
caracter_resultado = "veto"
     │
     ▼
┌──────────────────────────────────┐
│ 🚫 VETO — DESCALIFICACIÓN        │
│                                  │
│ El asesor ha detectado señales   │
│ de riesgo graves en el cliente.  │
│                                  │
│ Observación obligatoria: *       │
│ ┌──────────────────────────┐     │
│ │                          │     │
│ └──────────────────────────┘     │
│                                  │
│ [Confirmar Veto]                 │
│ [Cancelar]                       │
└──────────────────────────────────┘
```

---

## 9. Cálculo de Score

### 9.1. Score de Campo

```
score_campo = pts_f1 + pts_f2 + pts_f3 + pts_f4

Donde:
  pts_f1 = pts_antiguedad + pts_tenencia            (máx 60)
  pts_f2 = pts_ventas + pts_gastos                   (máx 60)
  pts_f3 = pts_deuda_informal + pts_pandero          (máx 40, puede ser negativo)
  pts_f4 = pts_stock + pts_activos                   (máx 40)

Rango teórico: -70 a +200 pts
Rango práctico: 0 a 200 pts
```

### 9.2. Score Final

```
score_final = score_transaccional + score_campo

Donde:
  score_transaccional = pts_saldo + pts_regularidad + pts_disciplina
                      + pts_vinculo + pts_riesgo       (máx 800)
  score_campo = resultado de F1-F4                      (máx 200)

Score final máximo: 1000 pts
```

### 9.3. Segmento Resultante (Post Campo)

```kotlin
fun determinarSegmento(
    negocioVerificado: Boolean,
    caracterResultado: String,
    scoreFinal: Int
): String = when {
    !negocioVerificado         -> "DESCALIFICADO"
    caracterResultado == "veto" -> "DESCALIFICADO"
    scoreFinal >= 750          -> "PREMIER"
    scoreFinal >= 550          -> "ESTANDAR"
    scoreFinal >= 350          -> "BASICO"
    else                       -> "NO_APLICA"
}
```

---

## 10. Cálculo de Cuota

### 10.1. Fórmula de Cuota Mensual

```kotlin
fun calcularCuotaMensual(
    montoAprobado: Double,
    plazoMeses: Int,
    teaAnual: Double = 0.60  // TEA 60%
): Double {
    val temMensual = (1 + teaAnual).pow(1.0 / 12) - 1
    return montoAprobado * temMensual /
           (1 - (1 + temMensual).pow(-plazoMeses))
}
```

### 10.2. Montos por Segmento

| Segmento   | Monto Mínimo | Monto Máximo | Plazo Mín | Plazo Máx |
|------------|--------------|--------------|-----------|-----------|
| PREMIER    | S/ 1,000     | S/ 5,000     | 6 meses   | 24 meses  |
| ESTÁNDAR   | S/ 500       | S/ 2,500     | 6 meses   | 18 meses  |
| BÁSICO     | S/ 300       | S/ 1,000     | 6 meses   | 12 meses  |

### 10.3. Regla de Cuota Máxima

```
cuota_estimada ≤ ingreso_promedio × 0.30  (30% del ingreso)

Si cuota_estimada > 30% del ingreso:
  → Reducir monto automáticamente
  → Warning al asesor
  → Sugerir monto ajustado
```

---

## 11. Pantalla Resumen

### 11.1. Wireframe Resumen

```
┌─────────────────────────────────┐
│ ← Resumen de Evaluación   💾   │
├─────────────────────────────────┤
│ ● ● ● ● ● ●                    │
│ Resumen Final                   │
├─────────────────────────────────┤
│                                 │
│ ┌───────────────────────────┐   │
│ │ 📊 SCORE FINAL             │   │
│ │                           │   │
│ │ Transaccional:  680 pts   │   │
│ │ Campo:          120 pts   │   │
│ │ ──────────────────────    │   │
│ │ TOTAL:          800 pts   │   │
│ │                           │   │
│ │ Segmento: 🟢 PREMIER      │   │
│ └───────────────────────────┘   │
│                                 │
│ ┌───────────────────────────┐   │
│ │ 💰 PROPUESTA DE CRÉDITO    │   │
│ │                           │   │
│ │ Monto propuesto:          │   │
│ │ ┌───────────────────┐     │   │
│ │ │ S/ 4,200           │     │   │
│ │ └───────────────────┘     │   │
│ │                           │   │
│ │ Plazo:                    │   │
│ │ [6m] [12m] [18m] [24m]    │   │
│ │                           │   │
│ │ Cuota estimada:           │   │
│ │ S/ 275.40 / mes           │   │
│ │                           │   │
│ │ Ratio cuota/ingreso: 22%  │   │
│ │ ✅ Dentro del 30%          │   │
│ └───────────────────────────┘   │
│                                 │
│ ┌───────────────────────────┐   │
│ │ 📋 RECOMENDACIÓN           │   │
│ │                           │   │
│ │ ○ Aprobar                 │   │
│ │ ○ Aprobar monto reducido  │   │
│ │ ○ Elevar a comité         │   │
│ │ ○ Rechazar                │   │
│ │                           │   │
│ │ Observaciones finales:    │   │
│ │ ┌───────────────────┐     │   │
│ │ │                   │     │   │
│ │ └───────────────────┘     │   │
│ └───────────────────────────┘   │
│                                 │
│  [Guardar Borrador]             │
│  [Enviar al Comité]             │
│                                 │
└─────────────────────────────────┘
```

---

## 12. UI State

### 12.1. FichaUiState

```kotlin
data class FichaUiState(
    // Identificación
    val clienteId: UUID? = null,
    val fichaId: UUID? = null,
    val clienteNombre: String = "",
    val clienteNegocio: String = "",
    val scoreTransaccional: Int = 0,
    val segmentoPreliminar: String = "",

    // Wizard
    val currentStep: Int = 0,  // 0=F1, 1=F2, 2=F3, 3=F4, 4=F5, 5=Resumen
    val isStepValid: Boolean = false,

    // F1: Verificación del Negocio
    val negocioVerificado: Boolean? = null,
    val motivoNoVerificado: String = "",
    val antiguedadNegocio: String? = null,
    val ptsAntiguedad: Int = 0,
    val tenenciaLocal: String? = null,
    val ptsTenencia: Int = 0,
    val direccionVerificada: String = "",
    val ptsF1: Int = 0,

    // F2: Capacidad de Pago
    val ventasDiariasRango: String? = null,
    val ptsVentas: Int = 0,
    val ventasMensualesEst: Double = 0.0,
    val gastosFijosMes: Double = 0.0,
    val ratioGastos: String? = null,
    val ptsGastos: Int = 0,
    val ingresoConsistente: Boolean = true,
    val obsInconsistencia: String = "",
    val ptsF2: Int = 0,

    // F3: Deuda Informal
    val tieneDeudaInformal: String? = null,
    val ptsDeudaInformal: Int = 0,
    val montoDeudaInformal: Double = 0.0,
    val detalleDeuda: String = "",
    val participaPandero: String? = null,
    val ptsPandero: Int = 0,
    val aportePanderoMes: Double = 0.0,
    val ptsF3: Int = 0,

    // F4: Activos y Respaldo
    val stockVisible: String? = null,
    val ptsStock: Int = 0,
    val activosHogar: String? = null,
    val ptsActivos: Int = 0,
    val descripcionActivos: String = "",
    val ptsF4: Int = 0,

    // F5: Carácter
    val caracterResultado: String = "sin_penalidad",
    val obsCaracter: String = "",

    // Scores calculados
    val scoreCampo: Int = 0,
    val scoreFinal: Int = 0,
    val segmentoResultante: String = "",

    // Propuesta
    val montoAprobadoPropuesto: Double = 0.0,
    val plazoPropuestoMeses: Int = 12,
    val cuotaEstimada: Double = 0.0,
    val ratioCuotaIngreso: Double = 0.0,
    val recomendacionAsesor: String? = null,
    val obsFinales: String = "",

    // Estado
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isDescalificado: Boolean = false,
    val motivoDescalificacion: String? = null,
    val savedOffline: Boolean = false,
    val fichaEstado: String = "en_proceso",

    // Metadata
    val fechaVisita: String = "",
    val horaInicio: String = "",
    val horaFin: String? = null,
    val asesorNombre: String = "",
    val agencia: String = ""
)
```

---

## 13. Use Cases (Lógica de Dominio)

### 13.1. CalcularScoreCampoUseCase

```kotlin
class CalcularScoreCampoUseCase @Inject constructor() {

    operator fun invoke(
        ptsAntiguedad: Int,
        ptsTenencia: Int,
        ptsVentas: Int,
        ptsGastos: Int,
        ptsDeudaInformal: Int,
        ptsPandero: Int,
        ptsStock: Int,
        ptsActivos: Int
    ): Int {
        return (ptsAntiguedad + ptsTenencia) +    // F1: máx 60
               (ptsVentas + ptsGastos) +           // F2: máx 60
               (ptsDeudaInformal + ptsPandero) +   // F3: -70 a +40
               (ptsStock + ptsActivos)             // F4: máx 40
    }
}
```

### 13.2. CalcularScoreFinalUseCase

```kotlin
class CalcularScoreFinalUseCase @Inject constructor() {

    operator fun invoke(
        scoreTransaccional: Int,
        scoreCampo: Int
    ): Int {
        return scoreTransaccional + scoreCampo
    }
}
```

### 13.3. DeterminarSegmentoUseCase

```kotlin
class DeterminarSegmentoUseCase @Inject constructor() {

    operator fun invoke(
        negocioVerificado: Boolean,
        caracterResultado: String,
        scoreFinal: Int
    ): Segmento = when {
        !negocioVerificado          -> Segmento.DESCALIFICADO
        caracterResultado == "veto" -> Segmento.DESCALIFICADO
        scoreFinal >= 750           -> Segmento.PREMIER
        scoreFinal >= 550           -> Segmento.ESTANDAR
        scoreFinal >= 350           -> Segmento.BASICO
        else                        -> Segmento.NO_APLICA
    }
}
```

### 13.4. CalcularCuotaUseCase

```kotlin
class CalcularCuotaUseCase @Inject constructor() {

    operator fun invoke(
        montoAprobado: Double,
        plazoMeses: Int,
        teaAnual: Double = 0.60
    ): CuotaResult {
        val temMensual = (1 + teaAnual).pow(1.0 / 12) - 1
        val cuota = montoAprobado * temMensual /
                    (1 - (1 + temMensual).pow(-plazoMeses.toDouble()))

        return CuotaResult(
            cuotaMensual = cuota.roundTo2(),
            temMensual = temMensual,
            totalIntereses = (cuota * plazoMeses) - montoAprobado,
            totalPagar = cuota * plazoMeses
        )
    }
}

data class CuotaResult(
    val cuotaMensual: Double,
    val temMensual: Double,
    val totalIntereses: Double,
    val totalPagar: Double
)
```

### 13.5. ValidarFichaUseCase

```kotlin
class ValidarFichaUseCase @Inject constructor() {

    operator fun invoke(step: Int, state: FichaUiState): ValidationResult {
        return when (step) {
            0 -> validateF1(state)
            1 -> validateF2(state)
            2 -> validateF3(state)
            3 -> validateF4(state)
            4 -> validateF5(state)
            5 -> validateResumen(state)
            else -> ValidationResult.Valid
        }
    }

    private fun validateF1(state: FichaUiState): ValidationResult {
        val errors = mutableListOf<String>()
        if (state.negocioVerificado == null)
            errors.add("Indica si el negocio fue verificado")
        if (state.negocioVerificado == false && state.motivoNoVerificado.isBlank())
            errors.add("El motivo de no verificación es obligatorio")
        if (state.negocioVerificado == true) {
            if (state.antiguedadNegocio == null)
                errors.add("Selecciona la antigüedad del negocio")
            if (state.tenenciaLocal == null)
                errors.add("Selecciona el tipo de tenencia del local")
        }
        return if (errors.isEmpty()) ValidationResult.Valid
               else ValidationResult.Invalid(errors)
    }
    // ... similar para F2-F5 y Resumen
}

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errors: List<String>) : ValidationResult()
}
```

---

## 14. Manejo Offline

### 14.1. Guardado Automático

```
Cada cambio en un campo:
  1. ViewModel actualiza el UiState
  2. ViewModel encola guardado con debounce(500ms)
  3. Room guarda la ficha parcial
  4. Badge "💾 Guardado offline" aparece

Al cerrar la app o perder batería:
  → La ficha se recupera desde Room al reabrir
  → El wizard continúa desde el último step completado
```

### 14.2. Room Entity para Ficha

```kotlin
@Entity(tableName = "fichas_campo")
data class FichaCampoEntity(
    @PrimaryKey
    val id: String,  // UUID generado localmente
    val userId: String,
    val scoreId: String?,
    val asesorNombre: String,
    val agencia: String,
    val fechaVisita: String,
    val horaInicio: String?,
    // ... todos los campos F1-F5
    val estadoFicha: String,
    val syncStatus: String,  // "pending" | "synced" | "conflict"
    val lastModified: Long,
    val createdAt: Long
)
```

### 14.3. Sync de Fichas

```
Al detectar conectividad:
  1. SyncManager consulta fichas con syncStatus = "pending"
  2. Para cada ficha:
     a. POST /rest/v1/fichas_campo → Supabase
     b. Si éxito: syncStatus = "synced"
     c. Si error: retry con backoff exponencial
     d. Si conflicto: syncStatus = "conflict" → notificar al asesor
```

---

## 15. Adjuntos / Fotos

### 15.1. Fotos de Visita (Futuro)

| Tipo de Foto          | Obligatoria | Almacenamiento    |
|-----------------------|-------------|-------------------|
| Fachada del negocio   | Sí          | Supabase Storage  |
| Interior / Stock      | No          | Supabase Storage  |
| Documento identidad   | Sí          | Supabase Storage  |
| Contrato de alquiler  | Condicional | Supabase Storage  |

### 15.2. Flujo de Fotos

```
Capturar foto (Camera Intent)
  │
  ├── Guardar localmente (cache interno)
  │
  ├── Comprimir (max 1MB, 80% quality)
  │
  └── Cuando hay red:
       └── Upload a Supabase Storage
           bucket: "fotos-visitas/{fichaId}/"
```

---

## 16. Navegación

### 16.1. Flujo

```
CarteraScreen / RutaScreen
  │ "Iniciar Ficha" (clienteId)
  ▼
FichaScreen (wizard)
  │
  ├── F1 → F2 → F3 → F4 → F5 → Resumen
  │         │                      │
  │         └── Back navigation    │
  │                                │
  ├── Descalificación (F1 o F5) ──► DescalificacionScreen
  │                                │
  └── "Enviar al Comité" ─────────► CarteraScreen (refresh)
      "Guardar Borrador" ─────────► CarteraScreen
```

### 16.2. Routes

| Route                         | Screen              | Params          |
|-------------------------------|----------------------|-----------------|
| `ficha/{clienteId}`           | FichaScreen          | clienteId: UUID |
| `ficha/{clienteId}/f{n}`      | Interno (wizard step)| n: 1-5          |
| `ficha/{fichaId}/resumen`     | FichaResumenScreen   | fichaId: UUID   |

---

## 17. Casos Edge

| Caso                                     | Comportamiento                                    |
|------------------------------------------|---------------------------------------------------|
| Cierre de app en medio de ficha          | Guardado automático en Room, recuperable           |
| Sin batería durante visita               | Auto-save cada campo modificado                    |
| Doble tap en "Enviar al Comité"          | Debounce, solo procesa uno                         |
| Rotación de pantalla                     | Mantener todo el estado del wizard                 |
| Back button en F3                        | Navegar a F2 conservando datos                     |
| Back button en F1                        | Dialog "¿Salir? Se guardará como borrador"         |
| Score negativo en F3                     | Permitir, mostrar warning                          |
| Monto propuesto > máximo del segmento   | Ajustar automáticamente al techo                   |
| Cuota > 30% del ingreso                 | Warning + sugerir monto ajustado                   |
| Cliente ya tiene ficha completada        | Mostrar ficha anterior (read-only) o nueva visita  |
| Ficha en estado "cancelada"              | No permitir edición, solo vista                    |
| Error al sincronizar ficha              | Mantener en sync queue, retry automático           |

---

## 18. Criterios de Aceptación

- [ ] El wizard navega correctamente entre F1-F5 y Resumen
- [ ] Cada sección calcula su puntaje parcial en tiempo real
- [ ] El score de campo se calcula como suma de F1+F2+F3+F4
- [ ] El score final = score transaccional + score campo
- [ ] El segmento resultante se determina correctamente según tabla
- [ ] La descalificación en F1 (negocio no verificado) funciona
- [ ] La descalificación en F5 (veto) funciona
- [ ] La cuota se calcula con la fórmula TEA correcta
- [ ] El ratio cuota/ingreso se valida contra el 30%
- [ ] La ficha se guarda automáticamente en Room (offline)
- [ ] La ficha se recupera correctamente al reabrir la app
- [ ] La validación impide avanzar sin completar campos obligatorios
- [ ] El resumen muestra todos los datos consolidados
- [ ] "Guardar Borrador" guarda en Room con estado en_proceso
- [ ] "Enviar al Comité" cambia estado a en_comité y sincroniza
- [ ] La lógica de scoring está 100% en domain/usecases
- [ ] Los composables no contienen cálculos financieros
- [ ] Los puntajes de F3 pueden ser negativos y se manejan correctamente
