# 10 — SCORING ENGINE

| Campo               | Valor                                                        |
|---------------------|--------------------------------------------------------------|
| **ID**              | SDD-10                                                       |
| **Sprint**          | Sprint 4 — Ficha de Campo                                    |
| **Estado**          | 📋 Especificado                                               |
| **Última revisión** | 2026-05-26                                                   |
| **Épica**           | E-06 Motor de Scoring                                        |
| **Prioridad**       | 🔴 Alta                                                      |

---

## 1. Objetivo

Definir la **arquitectura completa del motor de scoring** del proyecto. El scoring es **híbrido** con dos componentes: score transaccional (calculado desde datos del sistema bancario) y score de campo (calculado durante la visita del asesor). Toda la lógica debe residir en `domain/usecases`.

> ⚠️ **ARQUITECTURA:** Toda la lógica financiera y de scoring DEBE vivir en `domain/usecases`. Los composables solo muestran datos. Los ViewModels solo orquestan. Los repositorios solo leen/escriben datos.

---

## 2. Estructura del Score

### 2.1. Composición

```
┌────────────────────────────────────────────────────┐
│              SCORE FINAL (máx 1000 pts)             │
│                                                    │
│  ┌──────────────────────┐  ┌──────────────────────┐│
│  │  SCORE TRANSACCIONAL │  │   SCORE DE CAMPO     ││
│  │     (máx 800 pts)    │  │    (máx 200 pts)     ││
│  │                      │  │                      ││
│  │  A: Saldo      200   │  │  F1: Negocio    60   ││
│  │  B: Regularidad 160  │  │  F2: Capacidad  60   ││
│  │  C: Disciplina 160   │  │  F3: Deuda      40*  ││
│  │  D: Vínculo    160   │  │  F4: Activos    40   ││
│  │  E: Riesgo     120   │  │  F5: Carácter   0†   ││
│  └──────────────────────┘  └──────────────────────┘│
│                                                    │
│  * F3 puede ser negativo (-70 a +40)               │
│  † F5 es cualitativo (veto/alerta/ok)              │
└────────────────────────────────────────────────────┘
```

### 2.2. Tabla Resumen

| Componente              | Puntaje Máx | Puntaje Mín | Fuente            |
|-------------------------|-------------|-------------|-------------------|
| Score Transaccional     | 800         | 0           | Sistema bancario  |
| Score de Campo          | 200         | -70         | Visita del asesor |
| **Score Final**         | **1000**    | **-70**     | Suma de ambos     |

---

## 3. Score Transaccional (800 pts)

### 3.1. Grupo A: Capacidad de Ahorro (máx 200 pts)

**Indicador:** Saldo promedio de la cuenta en últimos 12 meses

| Rango Saldo Promedio | Puntaje |
|----------------------|---------|
| ≥ S/ 5,000           | 200     |
| ≥ S/ 2,000           | 160     |
| ≥ S/ 1,000           | 120     |
| ≥ S/ 500             | 80      |
| ≥ S/ 200             | 40      |
| < S/ 200             | 0       |

### 3.2. Grupo B: Regularidad de Ingresos (máx 160 pts)

**Indicador:** Meses con al menos un abono en últimos 12 meses

| Meses con Abono | Puntaje |
|-----------------|---------|
| ≥ 11 meses      | 160     |
| ≥ 9 meses       | 128     |
| ≥ 7 meses       | 96      |
| ≥ 5 meses       | 64      |
| < 5 meses       | 24      |

### 3.3. Grupo C: Disciplina Financiera (máx 160 pts)

**Indicador:** Ratio de ahorro neto = (abonos - cargos) / abonos

| Ratio Ahorro Neto | Puntaje |
|--------------------|---------|
| ≥ 30%              | 160     |
| ≥ 20%              | 120     |
| ≥ 10%              | 80      |
| ≥ 1%               | 40      |
| < 1%               | 0       |

### 3.4. Grupo D: Vínculo con la Institución (máx 160 pts)

**Indicador:** Antigüedad de la cuenta más antigua (en meses)

| Antigüedad Cuenta | Puntaje |
|--------------------|---------|
| ≥ 36 meses         | 160     |
| ≥ 24 meses         | 120     |
| ≥ 12 meses         | 80      |
| ≥ 6 meses          | 40      |
| < 6 meses          | 0       |

### 3.5. Grupo E: Perfil de Riesgo (máx 120 pts)

**Indicador:** Número de entidades financieras en la SBS

| Entidades SBS | Puntaje |
|---------------|---------|
| 0             | 120     |
| 1             | 90      |
| 2-3           | 48      |
| ≥ 4           | 12      |

---

## 4. Segmento Preliminar (Pre-Campo)

### 4.1. Determinación

```kotlin
fun determinarSegmentoPreliminar(scoreTransaccional: Int): String = when {
    scoreTransaccional >= 600 -> "PREMIER"
    scoreTransaccional >= 440 -> "ESTANDAR"
    scoreTransaccional >= 280 -> "BASICO"
    else                      -> "NO_APLICA"
}
```

### 4.2. Umbrales

| Segmento Preliminar | Score Transaccional | % del Score Máx (800) |
|---------------------|---------------------|-----------------------|
| PREMIER             | ≥ 600               | 75%                   |
| ESTÁNDAR            | 440 – 599           | 55% – 74%             |
| BÁSICO              | 280 – 439           | 35% – 54%             |
| NO_APLICA           | < 280               | < 35%                 |

---

## 5. Score de Campo (200 pts)

### 5.1. F1: Verificación del Negocio (máx 60 pts)

| Sub-indicador         | Opciones                    | Puntaje |
|-----------------------|-----------------------------|---------|
| **Antigüedad negocio**| Menos de 1 año             | 0       |
|                       | 1 a 3 años                 | 20      |
|                       | Más de 3 años              | 40      |
| **Tenencia local**    | Alquilado sin contrato     | 0       |
|                       | Alquilado con contrato     | 10      |
|                       | Propio                     | 20      |

```
pts_f1 = pts_antiguedad + pts_tenencia
```

---

### 5.2. F2: Capacidad de Pago Real (máx 60 pts)

| Sub-indicador         | Opciones                    | Puntaje |
|-----------------------|-----------------------------|---------|
| **Ventas diarias**    | Menos de S/ 50              | 0       |
|                       | S/ 50 - S/ 150             | 15      |
|                       | S/ 151 - S/ 300            | 30      |
|                       | Más de S/ 300              | 45      |
| **Ratio gastos**      | Más del 80%                | 0       |
|                       | 50% a 80%                  | 5       |
|                       | Menos del 50%              | 15      |

```
pts_f2 = pts_ventas + pts_gastos
```

---

### 5.3. F3: Deuda Informal (rango -70 a +40 pts)

| Sub-indicador         | Opciones                    | Puntaje |
|-----------------------|-----------------------------|---------|
| **Deuda informal**    | Sí, significativa          | -50     |
|                       | Sí, menor                  | -20     |
|                       | No tiene                   | +20     |
| **Pandero/junta**     | Sí, mayor a cuota estimada | -20     |
|                       | Sí, menor a cuota estimada | 0       |
|                       | No participa               | +20     |

```
pts_f3 = pts_deuda_informal + pts_pandero
```

> ⚠️ **Nota:** F3 puede ser negativo. Esto penaliza el score de campo y por tanto el score final. El rango teórico de F3 es de -70 (peor caso) a +40 (mejor caso).

---

### 5.4. F4: Activos y Respaldo (máx 40 pts)

| Sub-indicador         | Opciones                    | Puntaje |
|-----------------------|-----------------------------|---------|
| **Stock visible**     | Escaso                     | 0       |
|                       | Moderado                   | 10      |
|                       | Abundante                  | 20      |
| **Activos hogar**     | Ninguno                    | 0       |
|                       | Al menos uno               | 20      |

```
pts_f4 = pts_stock + pts_activos
```

---

### 5.5. F5: Carácter del Cliente (sin puntaje, cualitativo)

| Resultado          | Efecto                                          |
|--------------------|-------------------------------------------------|
| Sin penalidad      | Continúa normalmente                            |
| Alerta             | Se eleva automáticamente a comité               |
| Veto               | **DESCALIFICACIÓN INMEDIATA** → segmento = DESCALIFICADO |

---

## 6. Score Final y Segmento Resultante

### 6.1. Fórmula

```
score_final = score_transaccional + score_campo

Donde:
  score_campo = pts_f1 + pts_f2 + pts_f3 + pts_f4
              = (pts_antiguedad + pts_tenencia)
              + (pts_ventas + pts_gastos)
              + (pts_deuda_informal + pts_pandero)
              + (pts_stock + pts_activos)
```

### 6.2. Segmento Resultante (Post-Campo)

```kotlin
fun determinarSegmentoFinal(
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
```

### 6.3. Comparación de Umbrales Pre vs Post Campo

| Segmento   | Pre-Campo (Solo Trans.) | Post-Campo (Final)  | Diferencia  |
|------------|-------------------------|---------------------|-------------|
| PREMIER    | ≥ 600 / 800 (75%)      | ≥ 750 / 1000 (75%) | +150 pts    |
| ESTÁNDAR   | ≥ 440 / 800 (55%)      | ≥ 550 / 1000 (55%) | +110 pts    |
| BÁSICO     | ≥ 280 / 800 (35%)      | ≥ 350 / 1000 (35%) | +70 pts     |
| NO_APLICA  | < 280                   | < 350               | —           |

---

## 7. Fórmula de Cuota y Montos

### 7.1. Hipótesis de Monto (Pre-Campo)

```kotlin
fun calcularMontoHipotesis(
    segmentoPreliminar: String,
    montoMaxPorIngreso: Double  // ingreso_promedio × 2
): Double = when (segmentoPreliminar) {
    "PREMIER"  -> minOf(montoMaxPorIngreso, 5000.0)
    "ESTANDAR" -> minOf(montoMaxPorIngreso, 2500.0)
    "BASICO"   -> minOf(montoMaxPorIngreso, 1000.0)
    else       -> 0.0
}
```

### 7.2. Montos Aprobados por Segmento

| Segmento   | Monto Mínimo | Monto Máximo | TEA Referencial |
|------------|--------------|--------------|-----------------|
| PREMIER    | S/ 1,000     | S/ 5,000     | 55%             |
| ESTÁNDAR   | S/ 500       | S/ 2,500     | 60%             |
| BÁSICO     | S/ 300       | S/ 1,000     | 65%             |

### 7.3. Plazos Disponibles

| Segmento   | Plazos (meses)        |
|------------|------------------------|
| PREMIER    | 6, 9, 12, 18, 24      |
| ESTÁNDAR   | 6, 9, 12, 18          |
| BÁSICO     | 6, 9, 12              |

### 7.4. Cálculo de Cuota Mensual

```kotlin
fun calcularCuotaMensual(
    montoAprobado: Double,
    plazoMeses: Int,
    teaAnual: Double
): Double {
    // TEM = (1 + TEA)^(1/12) - 1
    val temMensual = (1 + teaAnual).pow(1.0 / 12.0) - 1

    // Cuota = M × TEM / (1 - (1 + TEM)^(-n))
    val cuota = montoAprobado * temMensual /
                (1 - (1 + temMensual).pow(-plazoMeses.toDouble()))

    return (cuota * 100).roundToInt() / 100.0  // Redondear a 2 decimales
}
```

### 7.5. Ejemplo de Cálculo

```
Datos:
  Monto: S/ 3,000
  Plazo: 12 meses
  TEA: 60%

Cálculo:
  TEM = (1 + 0.60)^(1/12) - 1 = 0.03987 ≈ 3.99%
  Cuota = 3000 × 0.03987 / (1 - (1.03987)^(-12))
        = 119.61 / (1 - 0.6270)
        = 119.61 / 0.3730
        = S/ 320.67 / mes

Verificación:
  Total a pagar: 320.67 × 12 = S/ 3,848.04
  Total intereses: S/ 848.04
  Ratio cuota/ingreso: si ingreso = S/ 1,200 → 26.7% ✅ (< 30%)
```

### 7.6. Regla del 30% (Ratio Cuota/Ingreso)

```kotlin
fun validarRatioCuotaIngreso(
    cuotaMensual: Double,
    ingresoPromedio: Double,
    maxRatio: Double = 0.30
): RatioResult {
    val ratio = cuotaMensual / ingresoPromedio

    return when {
        ratio <= maxRatio -> RatioResult.Ok(ratio)
        ratio <= maxRatio + 0.05 -> RatioResult.Warning(
            ratio,
            "La cuota representa el ${(ratio * 100).roundToInt()}% del ingreso"
        )
        else -> RatioResult.Exceeded(
            ratio,
            calcularMontoMaximoPorRatio(ingresoPromedio, maxRatio)
        )
    }
}

fun calcularMontoMaximoPorRatio(
    ingresoPromedio: Double,
    maxRatio: Double,
    plazoMeses: Int,
    teaAnual: Double
): Double {
    val cuotaMax = ingresoPromedio * maxRatio
    val temMensual = (1 + teaAnual).pow(1.0 / 12.0) - 1
    // Monto = Cuota × (1 - (1+TEM)^(-n)) / TEM
    return cuotaMax * (1 - (1 + temMensual).pow(-plazoMeses.toDouble())) / temMensual
}
```

---

## 8. Descalificaciones Automáticas

### 8.1. Reglas de Descalificación

| # | Regla                                  | Momento      | Acción                    |
|---|----------------------------------------|--------------|---------------------------|
| 1 | Negocio no verificado                  | F1           | Descalificación inmediata |
| 2 | Carácter = veto                        | F5           | Descalificación inmediata |
| 3 | Score final < 350                      | Resumen      | Segmento = NO_APLICA      |
| 4 | Calificación SBS ≠ Normal              | Pre-scoring  | No elegible (filtro)      |
| 5 | Deuda total SBS > límite               | Pre-scoring  | No elegible (filtro)      |

### 8.2. Validaciones Pre-Scoring

```kotlin
fun esElegibleParaPreaprobacion(perfil: PerfilCliente): ElegibilidadResult {
    val razones = mutableListOf<String>()

    if (perfil.calificacionSbs != "Normal") {
        razones.add("Calificación SBS: ${perfil.calificacionSbs}")
    }
    if (perfil.numEntidadesSbs > 5) {
        razones.add("Demasiadas entidades SBS: ${perfil.numEntidadesSbs}")
    }
    if (perfil.estadoCliente != "activo") {
        razones.add("Estado del cliente: ${perfil.estadoCliente}")
    }

    return if (razones.isEmpty()) {
        ElegibilidadResult.Elegible
    } else {
        ElegibilidadResult.NoElegible(razones)
    }
}
```

---

## 9. Use Cases (Arquitectura de Dominio)

### 9.1. Diagrama de Use Cases

```
domain/usecase/scoring/
  ├── CalcularScoreTransaccionalUseCase.kt
  ├── CalcularScoreCampoUseCase.kt
  ├── CalcularScoreFinalUseCase.kt
  ├── DeterminarSegmentoUseCase.kt
  ├── CalcularCuotaUseCase.kt
  ├── CalcularMontoHipotesisUseCase.kt
  ├── ValidarRatioCuotaIngresoUseCase.kt
  ├── ValidarElegibilidadUseCase.kt
  └── GenerarPropuestaCreditoUseCase.kt
```

### 9.2. GenerarPropuestaCreditoUseCase

```kotlin
class GenerarPropuestaCreditoUseCase @Inject constructor(
    private val calcularScoreFinalUseCase: CalcularScoreFinalUseCase,
    private val determinarSegmentoUseCase: DeterminarSegmentoUseCase,
    private val calcularCuotaUseCase: CalcularCuotaUseCase,
    private val validarRatioCuotaIngresoUseCase: ValidarRatioCuotaIngresoUseCase
) {
    operator fun invoke(
        scoreTransaccional: Int,
        scoreCampo: Int,
        negocioVerificado: Boolean,
        caracterResultado: String,
        montoSolicitado: Double,
        plazoMeses: Int,
        ingresoPromedio: Double
    ): PropuestaCreditoResult {

        // 1. Score final
        val scoreFinal = calcularScoreFinalUseCase(scoreTransaccional, scoreCampo)

        // 2. Segmento
        val segmento = determinarSegmentoUseCase(
            negocioVerificado, caracterResultado, scoreFinal
        )

        // 3. Verificar descalificación
        if (segmento == Segmento.DESCALIFICADO || segmento == Segmento.NO_APLICA) {
            return PropuestaCreditoResult.NoAprobado(
                scoreFinal = scoreFinal,
                segmento = segmento,
                motivo = when (segmento) {
                    Segmento.DESCALIFICADO -> "Cliente descalificado"
                    else -> "Score insuficiente ($scoreFinal pts)"
                }
            )
        }

        // 4. Ajustar monto al rango del segmento
        val (montoMin, montoMax) = segmento.rangoMonto()
        val montoAjustado = montoSolicitado.coerceIn(montoMin, montoMax)

        // 5. TEA según segmento
        val tea = segmento.tasaTea()

        // 6. Calcular cuota
        val cuotaResult = calcularCuotaUseCase(montoAjustado, plazoMeses, tea)

        // 7. Validar ratio cuota/ingreso
        val ratioResult = validarRatioCuotaIngresoUseCase(
            cuotaResult.cuotaMensual, ingresoPromedio
        )

        // 8. Si excede ratio, calcular monto máximo
        val montoFinal = if (ratioResult is RatioResult.Exceeded) {
            val montoMaxRatio = calcularMontoMaximoPorRatio(
                ingresoPromedio, 0.30, plazoMeses, tea
            )
            minOf(montoAjustado, montoMaxRatio)
        } else {
            montoAjustado
        }

        // 9. Recalcular cuota con monto final
        val cuotaFinal = calcularCuotaUseCase(montoFinal, plazoMeses, tea)

        return PropuestaCreditoResult.Aprobado(
            scoreFinal = scoreFinal,
            segmento = segmento,
            montoAprobado = montoFinal,
            plazoMeses = plazoMeses,
            tea = tea,
            cuotaMensual = cuotaFinal.cuotaMensual,
            totalIntereses = cuotaFinal.totalIntereses,
            totalPagar = cuotaFinal.totalPagar,
            ratioCuotaIngreso = cuotaFinal.cuotaMensual / ingresoPromedio,
            montoAjustado = montoFinal != montoSolicitado,
            motivoAjuste = when {
                montoFinal < montoSolicitado && ratioResult is RatioResult.Exceeded ->
                    "Ajustado por ratio cuota/ingreso"
                montoFinal < montoSolicitado ->
                    "Ajustado al techo del segmento $segmento"
                else -> null
            }
        )
    }
}

sealed class PropuestaCreditoResult {
    data class Aprobado(
        val scoreFinal: Int,
        val segmento: Segmento,
        val montoAprobado: Double,
        val plazoMeses: Int,
        val tea: Double,
        val cuotaMensual: Double,
        val totalIntereses: Double,
        val totalPagar: Double,
        val ratioCuotaIngreso: Double,
        val montoAjustado: Boolean,
        val motivoAjuste: String?
    ) : PropuestaCreditoResult()

    data class NoAprobado(
        val scoreFinal: Int,
        val segmento: Segmento,
        val motivo: String
    ) : PropuestaCreditoResult()
}
```

---

## 10. Modelo de Dominio

### 10.1. Enums

```kotlin
enum class Segmento(val label: String) {
    PREMIER("Premier"),
    ESTANDAR("Estándar"),
    BASICO("Básico"),
    NO_APLICA("No Aplica"),
    DESCALIFICADO("Descalificado");

    fun rangoMonto(): Pair<Double, Double> = when (this) {
        PREMIER    -> 1000.0 to 5000.0
        ESTANDAR   -> 500.0 to 2500.0
        BASICO     -> 300.0 to 1000.0
        else       -> 0.0 to 0.0
    }

    fun tasaTea(): Double = when (this) {
        PREMIER    -> 0.55
        ESTANDAR   -> 0.60
        BASICO     -> 0.65
        else       -> 0.60
    }

    fun plazosDisponibles(): List<Int> = when (this) {
        PREMIER    -> listOf(6, 9, 12, 18, 24)
        ESTANDAR   -> listOf(6, 9, 12, 18)
        BASICO     -> listOf(6, 9, 12)
        else       -> emptyList()
    }
}
```

---

## 11. Tablas de Score: Referencia Rápida

### 11.1. Score Transaccional Completo

| Grupo | Indicador              | Peso | Rangos                                        |
|-------|------------------------|------|-----------------------------------------------|
| A     | Saldo promedio         | 200  | ≥5000→200, ≥2000→160, ≥1000→120, ≥500→80, ≥200→40, <200→0 |
| B     | Meses con abono        | 160  | ≥11→160, ≥9→128, ≥7→96, ≥5→64, <5→24         |
| C     | Ratio ahorro neto      | 160  | ≥30%→160, ≥20%→120, ≥10%→80, ≥1%→40, <1%→0   |
| D     | Antigüedad cuenta      | 160  | ≥36m→160, ≥24m→120, ≥12m→80, ≥6m→40, <6m→0   |
| E     | Entidades SBS          | 120  | 0→120, 1→90, 2-3→48, ≥4→12                   |

### 11.2. Score de Campo Completo

| Sección | Indicador              | Peso     | Rangos                                     |
|---------|------------------------|----------|--------------------------------------------|
| F1      | Antigüedad negocio     | 40       | <1a→0, 1-3a→20, >3a→40                    |
| F1      | Tenencia local         | 20       | sin_contrato→0, con_contrato→10, propio→20 |
| F2      | Ventas diarias         | 45       | <50→0, 50-150→15, 151-300→30, >300→45     |
| F2      | Ratio gastos           | 15       | >80%→0, 50-80%→5, <50%→15                 |
| F3      | Deuda informal         | -50/+20  | significativa→-50, menor→-20, no→+20      |
| F3      | Pandero/junta          | -20/+20  | mayor_cuota→-20, menor_cuota→0, no→+20    |
| F4      | Stock visible          | 20       | escaso→0, moderado→10, abundante→20        |
| F4      | Activos hogar          | 20       | ninguno→0, al_menos_uno→20                 |
| F5      | Carácter               | cualit.  | ok / alerta / veto (descalifica)           |

---

## 12. Criterios de Aceptación

- [ ] El score transaccional calcula correctamente los 5 grupos (A-E)
- [ ] El score de campo calcula correctamente las 4 secciones (F1-F4)
- [ ] F3 puede ser negativo y se maneja correctamente
- [ ] El score final = transaccional + campo
- [ ] El segmento se determina correctamente según umbrales
- [ ] La descalificación por veto funciona (F5)
- [ ] La descalificación por negocio no verificado funciona (F1)
- [ ] La cuota se calcula con la fórmula TEA correcta
- [ ] El ratio cuota/ingreso se valida contra el 30%
- [ ] Los montos se ajustan al rango del segmento
- [ ] La propuesta de crédito se genera con todos los datos
- [ ] Toda la lógica reside en domain/usecases (NO en composables)
- [ ] Los use cases son testeables unitariamente
- [ ] Los cálculos de cuota coinciden con tablas financieras estándar
