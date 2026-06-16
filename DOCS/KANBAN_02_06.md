# KANBAN - Sesion 02/06/2026

| Campo   | Valor                                                   |
|---------|---------------------------------------------------------|
| Fecha   | 2026-06-02                                              |
| Sprint  | Sprint 1 cierre + Sprint 2 MVP                          |
| Alcance | Login real, cartera diaria MVP, usuarios test, limpieza |
| Base    | BBVA-Ventas / srxoisgexbcifdpwetxo                      |

---

## 1. Objetivo de la sesion

Consolidar un MVP minimo revisable:

- Login funcional contra Supabase Auth.
- Home/Cartera diaria con datos reales de Supabase.
- Usuarios reales de prueba vinculados a asesores del seed.
- Limpieza de navegacion y codigo placeholder heredado.
- Kanban actualizado para continuar el desarrollo.

---

## 2. Tareas completadas

### Bloque G - Backend & Dominio (Cartera)

| ID      | Tarea                                         | Prioridad | Estado | Nota |
|---------|-----------------------------------------------|-----------|--------|------|
| S02-001 | Crear modelos de dominio base de cartera      | Alta      | `[x]`  | `ClientePreaprobado`, `Segmento`, `EstadoVisita` compilan |
| S02-003 | Implementar ClienteRepository remoto/Supabase | Alta      | `[x]`  | Lee tablas separadas; evita join PostgREST invalido |
| S02-004 | Implementar GetCarteraDiariaUseCase           | Alta      | `[x]`  | Conectado a `CarteraViewModel` |
| S02-005 | Implementar LogoutUseCase                     | Media     | `[x]`  | Agregado y conectado a navegacion |
| S02-002 | Configurar Room Database base                 | Media     | `[ ]`  | Pendiente; fuera del MVP minimo |

### Bloque H - UI & Presentacion (Cartera)

| ID      | Tarea                                         | Prioridad | Estado | Nota |
|---------|-----------------------------------------------|-----------|--------|------|
| S02-006 | Crear CarteraUiState y CarteraViewModel       | Alta      | `[x]`  | Incluye filtros, ordenamiento y resumen |
| S02-007 | Crear ClienteCard con branding BBVA           | Alta      | `[x]`  | Cards por segmento/estado |
| S02-008 | Implementar CarteraScreen y lazy column       | Alta      | `[x]`  | Pantalla home MVP conectada |
| S02-009 | Implementar filtros y busqueda por DNI/nombre | Media     | `[x]`  | Busqueda por nombre, DNI y negocio |

### Bloque I - Auth & Usuarios de prueba

| ID      | Tarea                                           | Prioridad | Estado | Nota |
|---------|-------------------------------------------------|-----------|--------|------|
| S02-010 | Verificar MCP contra proyecto BBVA-Ventas       | Alta      | `[x]`  | URL confirmada: `https://srxoisgexbcifdpwetxo.supabase.co` |
| S02-011 | Crear/normalizar usuarios reales de prueba      | Alta      | `[x]`  | 3 usuarios Auth confirmados y vinculados a asesores seed |
| S02-012 | Validar login HTTP real contra Supabase Auth    | Alta      | `[x]`  | Login OK con password de testing |
| S02-013 | Reemplazar anon JWT por publishable key moderna | Media     | `[x]`  | App usa `sb_publishable_...` |
| S02-014 | Corregir SplashScreen para sesion activa        | Media     | `[x]`  | Ya no fuerza siempre a login |

---

## 3. Usuarios test disponibles

| Email                    | Password        | Perfil seed                     |
|--------------------------|-----------------|---------------------------------|
| asesor01@asesores.pe     | BbvaVentas2026! | Luis Flores More / AG-001-01    |
| and.palian1@asesores.pe  | BbvaVentas2026! | Andres Palian Perez / AG-001-07 |
| ana.quiroz19@asesores.pe | BbvaVentas2026! | Ana Quiroz Flores / AG-019-12   |

---

## 4. Verificacion tecnica

| Check                                    | Estado |
|------------------------------------------|--------|
| `gradlew.bat assembleDebug`              | `[x]`  |
| Login HTTP con usuarios test             | `[x]`  |
| Lectura REST de `perfiles_clientes`      | `[x]`  |
| Lectura REST de `scores_transaccionales` | `[x]`  |
| Lectura REST de `creditos_preaprobados`  | `[x]`  |

---

## 5. Riesgos pendientes detectados

| Riesgo                                                | Severidad | Estado |
|-------------------------------------------------------|-----------|--------|
| Vistas `vw_pbi_*` marcadas como security definer      | Alta      | `[ ]`  |
| Funcion `rls_auto_enable()` ejecutable via API        | Media     | `[ ]`  |
| Policy `sync_queue` con `USING true / WITH CHECK true` | Media    | `[ ]`  |
| Room/offline-first todavia pendiente                  | Media     | `[ ]`  |

---

## 6. Proximos pasos recomendados

- Corregir advisors de seguridad antes de ampliar escritura offline.
- Agregar pantalla detalle/ficha minima desde `ClienteCard`.
- Implementar Room cache basico para cartera.
- Agregar pruebas unitarias de `LoginUseCase` y filtros de cartera.

---

## 7. Actualizacion de continuidad - Offline MVP

| Campo   | Valor |
|---------|-------|
| Fecha   | 2026-06-02 |
| Alcance | Cache offline minimo de cartera con SQLite nativo |
| Estado  | MVP compilado y testeado |

### Bloque J - Offline-first minimo

| ID      | Tarea                                          | Prioridad | Estado | Nota |
|---------|------------------------------------------------|-----------|--------|------|
| S02-015 | Crear datasource local SQLite de cartera       | Alta      | `[x]`  | `CarteraLocalDataSource` con tabla `cartera_clientes` |
| S02-016 | Persistir cartera remota en cache local        | Alta      | `[x]`  | Cada sync exitosa reemplaza el snapshot local |
| S02-017 | Fallback a cache si Supabase falla             | Alta      | `[x]`  | Si hay datos guardados, la app muestra cartera offline |
| S02-018 | Exponer fuente de datos al ViewModel           | Media     | `[x]`  | `CarteraLoadResult` indica `REMOTE` o `CACHE` |
| S02-019 | Mostrar estado de sincronizacion en UI         | Media     | `[x]`  | Banner compacto: Online / Modo offline |
| S02-020 | Migrar cache SQLite nativo a Room              | Media     | `[ ]`  | Pendiente para Sprint offline formal |
| S02-021 | Implementar sync queue local de escrituras     | Alta      | `[ ]`  | Pendiente para ficha de campo y visitas |

### Verificacion tecnica adicional

| Check                       | Estado |
|-----------------------------|--------|
| `gradlew.bat assembleDebug` | `[x]`  |
| `gradlew.bat testDebugUnitTest` | `[x]` |

### Nota de arquitectura

El SDD objetivo define Room como fuente de verdad final. Este corte usa SQLite nativo para evitar agregar dependencias y habilitar rapido un comportamiento offline revisable. La migracion a Room queda marcada como tarea de sprint cuando se implemente ficha, visitas y sync queue.

---

## 8. Actualizacion de continuidad - Home operativo

| Campo   | Valor |
|---------|-------|
| Fecha   | 2026-06-02 |
| Alcance | Menu Home con mapa de modulos SDD y navegacion base |
| Estado  | MVP compilado y testeado |

### Bloque K - Home y navegacion modular

| ID      | Tarea                                             | Prioridad | Estado | Nota |
|---------|---------------------------------------------------|-----------|--------|------|
| S02-022 | Relevar modulos desde SDD 00-10                   | Alta      | `[x]`  | Cartera, ruta, ficha, historial, sync, scoring, descalificacion y reportes |
| S02-023 | Crear ruta `home` como entrada post-login         | Alta      | `[x]`  | Splash y Login ya navegan a Home si hay sesion valida |
| S02-024 | Implementar `HomeScreen` operativo                | Alta      | `[x]`  | Cards por modulo, estados y roadmap de implementacion |
| S02-025 | Crear pantallas base para modulos pendientes      | Media     | `[x]`  | Placeholders con descripcion, base disponible y subtareas siguientes |
| S02-026 | Conectar Cartera hacia Ruta/Ficha                 | Media     | `[x]`  | Acciones de tarjeta ya navegan a modulos preparados |
| S02-027 | Definir detalle cliente navegable                 | Alta      | `[x]`  | Implementado en `cartera/{clienteId}` |
| S02-028 | Implementar Ficha F1-F5 con guardado local        | Alta      | `[ ]`  | Depende de detalle cliente y modelo local de ficha |
| S02-029 | Implementar Ruta diaria con mapa real             | Media     | `[ ]`  | Pendiente decision Google Maps / Mapbox |
| S02-030 | Implementar Sync queue de escrituras              | Alta      | `[ ]`  | Necesario para ficha y visitas offline |
| S02-031 | Implementar Historial de visitas                  | Media     | `[ ]`  | Depende de fichas/visitas locales |
| S02-032 | Implementar reportes/KPIs autorizados             | Baja      | `[ ]`  | Revisar advisors de vistas PBI antes de consumir |

### Orden recomendado de implementacion

1. Detalle de cliente desde cartera.
2. Ficha de campo F1-F5 con borrador local.
3. Sync queue para fichas y visitas.
4. Ruta diaria con mapa y pins por segmento.
5. Historial y reportes.

### Verificacion tecnica adicional

| Check                         | Estado |
|-------------------------------|--------|
| `gradlew.bat assembleDebug`   | `[x]`  |
| `gradlew.bat testDebugUnitTest` | `[x]` |

---

## 9. Actualizacion de continuidad - Cartera avanzada y detalle cliente

| Campo   | Valor |
|---------|-------|
| Fecha   | 2026-06-02 |
| Alcance | Cartera diaria mas operativa, regreso a Home y detalle de cliente |
| Estado  | MVP compilado y testeado |

### Bloque L - Cartera diaria avanzada

| ID      | Tarea                                             | Prioridad | Estado | Nota |
|---------|---------------------------------------------------|-----------|--------|------|
| S02-033 | Agregar regreso explicito desde Cartera a Home    | Alta      | `[x]`  | TopAppBar incluye flecha de retorno |
| S02-034 | Agregar tap en ClienteCard hacia detalle          | Alta      | `[x]`  | La tarjeta ya navega a `cartera/{clienteId}` |
| S02-035 | Crear `ClienteDetalleScreen`                      | Alta      | `[x]`  | Datos de negocio, propuesta, scoring y acciones |
| S02-036 | Crear `ClienteDetalleViewModel`                   | Media     | `[x]`  | Reusa cartera online/cache para resolver cliente |
| S02-037 | Enriquecer resumen operativo de Cartera           | Media     | `[x]`  | Contactados, agendados, comite, premier, mapa y alto score |
| S02-038 | Agregar prioridad sugerida en Cartera             | Media     | `[x]`  | Selecciona cliente pendiente/contactado con mayor score |
| S02-039 | Conectar detalle hacia ficha/ruta/descalificacion | Alta      | `[x]`  | Rutas preparadas para continuar cada flujo |
| S02-040 | Implementar Ficha F1-F5 real por cliente          | Alta      | `[ ]`  | Siguiente modulo recomendado |
| S02-041 | Implementar Ruta con mapa real centrado en cliente| Media     | `[ ]`  | Pendiente decision SDK mapas |
| S02-042 | Implementar descalificacion persistente offline   | Alta      | `[ ]`  | Pendiente sync queue |

### Verificacion tecnica adicional

| Check                         | Estado |
|-------------------------------|--------|
| MCP Supabase proyecto BBVA    | `[x]`  |
| `gradlew.bat assembleDebug`   | `[x]`  |
| `gradlew.bat testDebugUnitTest` | `[x]` |

### Nota de continuidad

El siguiente corte recomendado es Ficha de Campo F1-F5 usando `ficha/{clienteId}` como entrada. La pantalla de detalle ya deja el cliente seleccionado disponible para crear borrador local, calcular score de campo y preparar propuesta.

---

## 10. Actualizacion de continuidad - Cartera UI compacta y seeds reales

| Campo   | Valor |
|---------|-------|
| Fecha   | 2026-06-02 |
| Alcance | Redisenar Cartera para liberar lista e integrar asesor/agencia desde seeds |
| Estado  | MVP compilado y testeado |

### Bloque M - Cartera usable en pantalla movil

| ID      | Tarea                                               | Prioridad | Estado | Nota |
|---------|-----------------------------------------------------|-----------|--------|------|
| S02-043 | Convertir Cartera en una sola `LazyColumn`          | Alta      | `[x]`  | Buscador, contexto, metricas, filtros y lista scrollean juntos |
| S02-044 | Eliminar cabecera operativa fija sobredimensionada  | Alta      | `[x]`  | Solo queda fija la TopAppBar |
| S02-045 | Compactar metricas de cartera                       | Alta      | `[x]`  | Total, pendientes, visitados, score 700+, contactados, agenda, comite y mapa |
| S02-046 | Mantener prioridad sugerida sin ocupar media pantalla| Media    | `[x]`  | Se reemplaza card grande por strip compacto |
| S02-047 | Confirmar datos seed cargados en Supabase           | Alta      | `[x]`  | 30 agencias, 360 asesores, 6 perfiles/scores/creditos |
| S02-048 | Crear modelo `AsesorContext`                        | Media     | `[x]`  | Usa datos de `seed_agencias_asesores.sql` |
| S02-049 | Crear repositorio de asesor/agencia                 | Media     | `[x]`  | Lee `asesores_negocio` y `agencias` por email autenticado |
| S02-050 | Mostrar agencia, nivel y metas en Cartera           | Media     | `[x]`  | Contexto real del asesor test cuando la lectura esta permitida |
| S02-051 | Cache offline de contexto asesor                    | Media     | `[ ]`  | Pendiente para completar offline-first |
| S02-052 | Visual QA con emulador despues de instalar APK      | Media     | `[ ]`  | Pendiente de revision visual interactiva |

### Datos seed verificados

| Dataset | Conteo |
|---------|--------|
| `agencias` | 30 |
| `asesores_negocio` | 360 |
| `perfiles_clientes` | 6 |
| `scores_transaccionales` | 6 |
| `creditos_preaprobados` | 6 |

### Verificacion tecnica adicional

| Check                         | Estado |
|-------------------------------|--------|
| MCP Supabase proyecto BBVA    | `[x]`  |
| `gradlew.bat assembleDebug`   | `[x]`  |
| `gradlew.bat testDebugUnitTest` | `[x]` |
