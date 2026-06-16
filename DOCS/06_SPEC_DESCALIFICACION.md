# 06 — SPEC: DESCALIFICACIÓN

| Campo               | Valor                                                        |
|---------------------|--------------------------------------------------------------|
| **ID**              | SDD-06                                                       |
| **Sprint**          | Sprint 4 — Ficha de Campo                                    |
| **Estado**          | 📋 Especificado                                               |
| **Última revisión** | 2026-05-26                                                   |
| **Épica**           | E-05 Ficha de Campo                                          |
| **Prioridad**       | 🟡 Media                                                     |

---

## 1. Objetivo

Definir el flujo completo de **descalificación de clientes** durante la evaluación de campo. Un cliente puede ser descalificado por dos motivos: negocio no verificado (F1) o veto por carácter (F5). Este flujo requiere observaciones obligatorias, actualización de estados en múltiples tablas y sincronización con Supabase.

---

## 2. Causas de Descalificación

### 2.1. Tabla de Causas

| Causa                        | Sección | Trigger                            | Severidad |
|------------------------------|---------|-------------------------------------|-----------|
| Negocio no encontrado        | F1      | `negocio_verificado = false`        | 🔴 Alta   |
| Negocio cerrado/abandonado   | F1      | `negocio_verificado = false`        | 🔴 Alta   |
| Dirección inexistente        | F1      | `negocio_verificado = false`        | 🔴 Alta   |
| Veto por carácter            | F5      | `caracter_resultado = "veto"`       | 🔴 Alta   |
| Score NO_APLICA              | Resumen | `score_final < 350`                 | 🟡 Media  |

### 2.2. Diferencia entre Descalificación y NO_APLICA

| Concepto          | Descalificación                    | NO_APLICA                        |
|-------------------|------------------------------------|----------------------------------|
| Causa             | Veto explícito por el asesor       | Score insuficiente               |
| Sección           | F1 o F5                           | Calculado en Resumen             |
| Obligatoriedad    | Observación obligatoria            | Automático                       |
| ¿Puede revertirse?| No                                | No (nuevo ciclo de evaluación)   |
| Estado ficha      | "cancelada"                        | "completada"                     |
| Estado crédito    | "cancelado"                        | "rechazado"                      |

---

## 3. Flujo de Descalificación — F1 (Negocio No Verificado)

### 3.1. Diagrama de Flujo

```
F1: ¿Negocio verificado?
     │
     ├── SÍ → continuar evaluación normal
     │
     └── NO
          │
          ▼
     ┌──────────────────────────┐
     │ 🚨 DESCALIFICACIÓN F1    │
     │                          │
     │ Motivos posibles:        │
     │ ○ No se encontró         │
     │ ○ Cerrado permanente     │
     │ ○ Dirección incorrecta   │
     │ ○ Otro                   │
     │                          │
     │ Observación: *           │
     │ ┌──────────────────┐     │
     │ │                  │     │
     │ └──────────────────┘     │
     │                          │
     │ [Confirmar]  [Cancelar]  │
     └──────────────────────────┘
          │
          ▼
     Actualizar estados:
     ┌─────────────────────────────────┐
     │ fichas_campo.estado_ficha       │
     │   → "cancelada"                 │
     │                                 │
     │ fichas_campo.negocio_verificado │
     │   → false                       │
     │                                 │
     │ fichas_campo.segmento_resultante│
     │   → "DESCALIFICADO" (generated) │
     │                                 │
     │ creditos_preaprobados.estado    │
     │   → "cancelado"                 │
     └─────────────────────────────────┘
          │
          ▼
     Guardar en Room (offline)
          │
          ▼
     Encolar sync_queue
          │
          ▼
     Navegar a CarteraScreen
     + SnackBar "Cliente descalificado"
```

### 3.2. Motivos Predefinidos de F1

| Código | Motivo                                          |
|--------|-------------------------------------------------|
| `F1-01`| Negocio no encontrado en la dirección registrada|
| `F1-02`| Negocio cerrado permanentemente                 |
| `F1-03`| Dirección no existe o es inaccesible             |
| `F1-04`| Negocio no corresponde al registrado             |
| `F1-05`| Cliente se niega a recibir visita                |
| `F1-06`| Otro (especificar en observación)                |

---

## 4. Flujo de Descalificación — F5 (Veto por Carácter)

### 4.1. Diagrama de Flujo

```
F5: ¿Resultado del carácter?
     │
     ├── sin_penalidad → continuar a Resumen
     │
     ├── alerta → continuar a Resumen con warning
     │             (se elevará automáticamente a comité)
     │
     └── veto
          │
          ▼
     ┌──────────────────────────┐
     │ 🚫 VETO — DESCALIFICACIÓN│
     │                          │
     │ Señales de riesgo:       │
     │ ☐ Inconsistencia datos   │
     │ ☐ Comportamiento sospechoso│
     │ ☐ Negocio fachada        │
     │ ☐ Deudas ocultas graves  │
     │ ☐ Antecedentes negativos │
     │ ☐ Otro                   │
     │                          │
     │ Observación detallada: * │
     │ ┌──────────────────┐     │
     │ │ (mínimo 50 chars)│     │
     │ └──────────────────┘     │
     │                          │
     │ ⚠️ Esta acción es        │
     │ irreversible.            │
     │                          │
     │ [Confirmar Veto]         │
     │ [Cancelar]               │
     └──────────────────────────┘
          │
          ▼
     Actualizar estados (igual que F1)
     + registrar señales seleccionadas
```

### 4.2. Señales de Veto

| Código | Señal                                            |
|--------|--------------------------------------------------|
| `F5-01`| Inconsistencia grave entre datos declarados y observados |
| `F5-02`| Comportamiento evasivo o sospechoso del cliente  |
| `F5-03`| Indicios de negocio fachada                      |
| `F5-04`| Deudas informales ocultas de monto significativo |
| `F5-05`| Antecedentes negativos conocidos en la zona      |
| `F5-06`| Evidencia de actividades ilícitas                |
| `F5-07`| Otro (especificar en observación)                |

---

## 5. Observaciones Obligatorias

### 5.1. Validaciones de la Observación

| Regla                        | Valor                                      |
|------------------------------|--------------------------------------------|
| Longitud mínima              | 50 caracteres                              |
| Longitud máxima              | 500 caracteres                             |
| Caracteres permitidos        | Alfanuméricos, espacios, puntuación básica |
| Campo obligatorio            | Sí (no se puede confirmar sin observación) |
| Contador de caracteres       | Visible debajo del campo                   |

### 5.2. Ejemplos de Observaciones Válidas

```
✅ "El negocio registrado como bodega en Jr. Real 423 no fue encontrado.
    En la dirección existe un taller mecánico. Se consultó con vecinos
    y confirman que la bodega cerró hace 3 meses."

✅ "El cliente muestra documentación con datos inconsistentes. Los montos
    de venta declarados no coinciden con el stock visible. Se observaron
    deudas con al menos 3 proveedores por montos no declarados."

❌ "No encontrado" (muy corto)
❌ "Negocio cerrado" (insuficiente detalle)
```

---

## 6. Actualización de Estados

### 6.1. Tablas Afectadas

```
Descalificación confirmada
  │
  ├── fichas_campo
  │     ├── estado_ficha → "cancelada"
  │     ├── negocio_verificado → false (F1) / se mantiene (F5)
  │     ├── caracter_resultado → "veto" (F5) / se mantiene (F1)
  │     ├── motivo_no_verificado → "texto..." (F1)
  │     ├── obs_caracter → "texto..." (F5)
  │     ├── segmento_resultante → "DESCALIFICADO" (generated column)
  │     ├── hora_fin → hora actual
  │     └── updated_at → now()
  │
  ├── creditos_preaprobados
  │     ├── estado → "cancelado"
  │     └── updated_at → now()
  │
  └── sync_queue (si offline)
        ├── entity_type → "ficha_campo"
        ├── entity_id → fichaId
        ├── operation → "UPDATE"
        ├── payload → JSON con cambios
        └── status → "pending"
```

### 6.2. Orden de Operaciones

```kotlin
suspend fun descalificarCliente(
    fichaId: UUID,
    clienteId: UUID,
    motivo: MotivoDescalificacion,
    observacion: String,
    seniales: List<String> = emptyList()
) {
    // 1. Actualizar ficha en Room
    fichaDao.updateEstado(fichaId, "cancelada")
    fichaDao.updateDescalificacion(fichaId, motivo, observacion)

    // 2. Actualizar crédito preaprobado en Room
    creditoDao.updateEstado(clienteId, "cancelado")

    // 3. Encolar sync
    syncQueue.enqueue(
        SyncOperation(
            entityType = "ficha_campo",
            entityId = fichaId.toString(),
            operation = "UPDATE",
            payload = buildDescalificacionPayload(motivo, observacion, seniales),
            timestamp = System.currentTimeMillis()
        )
    )

    // 4. Si hay red, intentar sync inmediato
    if (networkMonitor.isConnected()) {
        syncManager.syncNow()
    }
}
```

---

## 7. Sincronización de Descalificaciones

### 7.1. Payload de Sync

```json
{
  "entity_type": "ficha_campo",
  "entity_id": "uuid-ficha",
  "operation": "UPDATE",
  "payload": {
    "estado_ficha": "cancelada",
    "negocio_verificado": false,
    "motivo_no_verificado": "Negocio no encontrado en la dirección...",
    "hora_fin": "14:30:00",
    "updated_at": "2026-05-26T14:30:00Z"
  },
  "related_updates": [
    {
      "entity_type": "creditos_preaprobados",
      "filter": { "user_id": "uuid-cliente" },
      "payload": {
        "estado": "cancelado",
        "updated_at": "2026-05-26T14:30:00Z"
      }
    }
  ]
}
```

### 7.2. Retry en caso de fallo

| Intento | Delay      | Acción si falla                    |
|---------|------------|------------------------------------|
| 1       | Inmediato  | Retry                              |
| 2       | 30 seg     | Retry                              |
| 3       | 2 min      | Retry                              |
| 4       | 10 min     | Retry                              |
| 5+      | 30 min     | Notificar al usuario               |

---

## 8. UI de Descalificación

### 8.1. Dialog de Confirmación

```
┌──────────────────────────────────┐
│ ⚠️ Confirmar Descalificación      │
├──────────────────────────────────┤
│                                  │
│ ¿Estás seguro de descalificar    │
│ a este cliente?                  │
│                                  │
│ Cliente: Juan Pérez Mamani       │
│ Negocio: Bodega "Don Juan"       │
│                                  │
│ Esta acción:                     │
│ • Cancelará la ficha de campo    │
│ • Cancelará el crédito preaprobado│
│ • No se puede revertir           │
│                                  │
│ [Cancelar]  [Confirmar Descalif.]│
└──────────────────────────────────┘
```

### 8.2. Snackbar Post-Descalificación

```
┌──────────────────────────────────────────────┐
│ ✅ Cliente descalificado. Ficha guardada.     │
│                                    [VER]     │
└──────────────────────────────────────────────┘
```

---

## 9. Casos Edge

| Caso                                     | Comportamiento                                   |
|------------------------------------------|--------------------------------------------------|
| Descalificar sin internet                | Guardar en Room + encolar sync                   |
| Doble click en "Confirmar"               | Debounce, solo procesa uno                       |
| Back button durante descalificación      | Cancelar dialog, volver al paso anterior         |
| App crash después de confirmar           | Room ya guardó, sync pendiente al reabrir        |
| Conflicto de sync (ya descalificado)     | Server wins, mantener descalificación            |
| Asesor quiere revertir descalificación   | No permitido — crear nueva evaluación            |
| Cliente ya descalificado previamente     | Mostrar info "Ya descalificado el DD/MM/YYYY"    |

---

## 10. Criterios de Aceptación

- [ ] La descalificación desde F1 funciona cuando `negocio_verificado = false`
- [ ] La descalificación desde F5 funciona cuando `caracter_resultado = veto`
- [ ] La observación es obligatoria (mínimo 50 caracteres)
- [ ] El dialog de confirmación muestra los datos del cliente
- [ ] La ficha cambia a estado "cancelada" después de descalificar
- [ ] El crédito preaprobado cambia a estado "cancelado"
- [ ] El segmento resultante cambia a "DESCALIFICADO"
- [ ] La descalificación funciona offline (se guarda en Room)
- [ ] La sincronización con Supabase ocurre cuando hay red
- [ ] El usuario es redirigido a CarteraScreen después de descalificar
- [ ] El Snackbar confirma la acción
- [ ] No se puede revertir una descalificación
- [ ] El historial muestra correctamente los clientes descalificados
