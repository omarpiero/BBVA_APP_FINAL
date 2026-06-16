# 09 — OFFLINE-FIRST STRATEGY

| Campo               | Valor                                                        |
|---------------------|--------------------------------------------------------------|
| **ID**              | SDD-09                                                       |
| **Sprint**          | Sprint 5 — Offline & Sincronización                          |
| **Estado**          | 📋 Especificado                                               |
| **Última revisión** | 2026-05-26                                                   |
| **Épica**           | E-07 Gestión Offline · E-09 Sincronización                   |
| **Prioridad**       | 🔴 Alta — Requisito crítico del proyecto                      |

---

## 1. Objetivo

Definir la estrategia **offline-first** completa para la app BBVA Fuerza de Ventas. La app debe funcionar **100% sin conexión** durante las visitas de campo, guardando todo localmente en Room y sincronizando cuando recupere conectividad.

> ⚠️ **Requisito obligatorio:** Los asesores visitan zonas rurales y periurbanas donde la conectividad es intermitente o inexistente. La app DEBE ser completamente funcional offline.

---

## 2. Principios Fundamentales

| # | Principio                      | Descripción                                           |
|---|--------------------------------|-------------------------------------------------------|
| 1 | Room como fuente de verdad     | La UI siempre lee de Room, nunca directamente de red  |
| 2 | Escrituras locales inmediatas  | Todo cambio se guarda en Room al instante             |
| 3 | Sincronización eventual        | Los datos se sincronizan cuando hay red disponible    |
| 4 | UUIDs generados localmente     | Cada entidad tiene UUID local antes de sync           |
| 5 | Server wins en conflictos      | El servidor es la fuente autoritativa final           |
| 6 | Queue de operaciones           | Las operaciones se encolan y procesan en orden FIFO   |
| 7 | Transparente al usuario        | El asesor no debe preocuparse por la sincronización   |

---

## 3. Room Database

### 3.1. Configuración

```kotlin
@Database(
    entities = [
        ClienteEntity::class,
        FichaCampoEntity::class,
        CreditoPreaprobadoEntity::class,
        ScoreTransaccionalEntity::class,
        VisitaEntity::class,
        SyncQueueEntity::class,
        AsesorEntity::class,
        AgenciaEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clienteDao(): ClienteDao
    abstract fun fichaDao(): FichaCampoDao
    abstract fun creditoDao(): CreditoPreaprobadoDao
    abstract fun scoreDao(): ScoreTransaccionalDao
    abstract fun visitaDao(): VisitaDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun asesorDao(): AsesorDao
    abstract fun agenciaDao(): AgenciaDao
}
```

### 3.2. Room Entities (Esquema Local)

#### ClienteEntity

```kotlin
@Entity(tableName = "clientes")
data class ClienteEntity(
    @PrimaryKey val id: String,            // UUID local
    val serverId: String?,                 // UUID del servidor (null si no sync)
    val userId: String,
    val dni: String?,
    val nombres: String,
    val apellidos: String,
    val telefono: String?,
    val distrito: String?,
    val provincia: String?,
    val departamento: String?,
    val nombreNegocio: String?,
    val tipoNegocio: String?,
    val direccionNegocio: String?,
    val latNegocio: Double?,
    val lngNegocio: Double?,
    val antiguedadNegocioMeses: Int = 0,
    val tenenciaLocal: String?,
    val numEntidadesSbs: Int = 0,
    val estadoCliente: String = "activo",
    // Scoring
    val scoreTransaccional: Int?,
    val segmentoPreliminar: String?,
    val montoHipotesis: Double?,
    val ingresoPromedioRef: Double?,
    val cuotaMaxRef: Double?,
    // Estado de visita
    val estadoVisita: String = "preaprobado",
    // Sync
    val syncStatus: String = "synced",     // synced, pending, conflict
    val lastModified: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)
```

#### FichaCampoEntity

```kotlin
@Entity(tableName = "fichas_campo")
data class FichaCampoEntity(
    @PrimaryKey val id: String,
    val serverId: String?,
    val userId: String,
    val scoreId: String?,
    val asesorNombre: String,
    val agencia: String,
    val fechaVisita: String,
    val horaInicio: String?,
    val horaFin: String?,
    // F1
    val negocioVerificado: Boolean?,
    val motivoNoVerificado: String?,
    val antiguedadNegocio: String?,
    val ptsAntiguedad: Int = 0,
    val tenenciaLocal: String?,
    val ptsTenencia: Int = 0,
    val direccionVerificada: String?,
    // F2
    val ventasDiariasRango: String?,
    val ptsVentas: Int = 0,
    val ventasMensualesEst: Double?,
    val gastosFijosMes: Double?,
    val ratioGastos: String?,
    val ptsGastos: Int = 0,
    val ingresoConsistente: Boolean = true,
    val obsInconsistencia: String?,
    // F3
    val tieneDeudaInformal: String?,
    val ptsDeudaInformal: Int = 0,
    val montoDeudaInformal: Double = 0.0,
    val detalleDeuda: String?,
    val participaPandero: String?,
    val ptsPandero: Int = 0,
    val aportePanderoMes: Double = 0.0,
    // F4
    val stockVisible: String?,
    val ptsStock: Int = 0,
    val activosHogar: String?,
    val ptsActivos: Int = 0,
    val descripcionActivos: String?,
    // F5
    val caracterResultado: String = "sin_penalidad",
    val obsCaracter: String?,
    // Scores
    val scoreTransaccionalRef: Int?,
    val scoreCampo: Int = 0,
    val scoreFinal: Int = 0,
    val segmentoResultante: String?,
    // Propuesta
    val montoAprobadoPropuesto: Double?,
    val plazoPropuestoMeses: Int?,
    val cuotaEstimada: Double?,
    val recomendacionAsesor: String?,
    val obsFinales: String?,
    // Estado
    val estadoFicha: String = "en_proceso",
    val currentStep: Int = 0,
    // Sync
    val syncStatus: String = "pending",
    val lastModified: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)
```

#### SyncQueueEntity

```kotlin
@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val entityType: String,            // "ficha_campo", "visita", etc.
    val entityId: String,              // UUID de la entidad
    val operation: String,             // "INSERT", "UPDATE", "DELETE"
    val payload: String,               // JSON serializado
    val status: String = "pending",    // pending, processing, completed, failed
    val retryCount: Int = 0,
    val maxRetries: Int = 5,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val processedAt: Long? = null,
    val priority: Int = 0              // 0=normal, 1=alta (descalificaciones)
)
```

---

### 3.3. DAOs

```kotlin
@Dao
interface ClienteDao {
    @Query("SELECT * FROM clientes ORDER BY scoreTransaccional DESC")
    fun getAllFlow(): Flow<List<ClienteEntity>>

    @Query("SELECT * FROM clientes WHERE id = :id")
    suspend fun getById(id: String): ClienteEntity?

    @Query("SELECT * FROM clientes WHERE segmentoPreliminar = :segmento")
    fun getBySegmento(segmento: String): Flow<List<ClienteEntity>>

    @Query("SELECT * FROM clientes WHERE nombres LIKE '%' || :query || '%' OR apellidos LIKE '%' || :query || '%' OR dni LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<ClienteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(clientes: List<ClienteEntity>)

    @Update
    suspend fun update(cliente: ClienteEntity)

    @Query("UPDATE clientes SET estadoVisita = :estado, syncStatus = 'pending', lastModified = :timestamp WHERE id = :id")
    suspend fun updateEstadoVisita(id: String, estado: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM clientes WHERE syncStatus = 'pending'")
    fun getPendingSyncCount(): Flow<Int>
}

@Dao
interface FichaCampoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ficha: FichaCampoEntity)

    @Update
    suspend fun update(ficha: FichaCampoEntity)

    @Query("SELECT * FROM fichas_campo WHERE id = :id")
    suspend fun getById(id: String): FichaCampoEntity?

    @Query("SELECT * FROM fichas_campo WHERE userId = :userId ORDER BY lastModified DESC")
    fun getByUserId(userId: String): Flow<List<FichaCampoEntity>>

    @Query("SELECT * FROM fichas_campo WHERE syncStatus = 'pending' ORDER BY lastModified ASC")
    suspend fun getPendingSync(): List<FichaCampoEntity>

    @Query("SELECT * FROM fichas_campo ORDER BY lastModified DESC")
    fun getAllFlow(): Flow<List<FichaCampoEntity>>

    @Query("UPDATE fichas_campo SET syncStatus = :status, lastSyncedAt = :syncedAt WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String, syncedAt: Long = System.currentTimeMillis())
}

@Dao
interface SyncQueueDao {
    @Insert
    suspend fun enqueue(item: SyncQueueEntity)

    @Query("SELECT * FROM sync_queue WHERE status = 'pending' ORDER BY priority DESC, createdAt ASC")
    suspend fun getPending(): List<SyncQueueEntity>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'pending'")
    fun getPendingCount(): Flow<Int>

    @Query("UPDATE sync_queue SET status = :status, processedAt = :processedAt, errorMessage = :error WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, processedAt: Long? = null, error: String? = null)

    @Query("UPDATE sync_queue SET status = 'pending', retryCount = retryCount + 1 WHERE id = :id AND retryCount < maxRetries")
    suspend fun retry(id: String): Int

    @Query("DELETE FROM sync_queue WHERE status = 'completed' AND processedAt < :before")
    suspend fun cleanCompleted(before: Long)
}
```

---

## 4. Sync Queue (Cola de Sincronización)

### 4.1. Flujo de Operaciones

```
Acción del usuario (crear/editar ficha)
     │
     ▼
[Repository]
     │
     ├── 1. Guardar en Room (inmediato)
     │
     ├── 2. Encolar en sync_queue
     │       SyncQueueEntity(
     │         entityType = "ficha_campo",
     │         entityId = fichaId,
     │         operation = "INSERT",
     │         payload = fichaJson,
     │         status = "pending"
     │       )
     │
     └── 3. Si hay red → SyncManager.syncNow()
            Si no hay red → esperar
```

### 4.2. Procesamiento de la Queue

```
SyncManager.processQueue()
     │
     ├── 1. Obtener items pendientes (FIFO + prioridad)
     │
     ├── 2. Para cada item:
     │       │
     │       ├── Marcar como "processing"
     │       │
     │       ├── Ejecutar operación en Supabase
     │       │     ├── INSERT → POST /rest/v1/{tabla}
     │       │     ├── UPDATE → PATCH /rest/v1/{tabla}?id=eq.{id}
     │       │     └── DELETE → DELETE /rest/v1/{tabla}?id=eq.{id}
     │       │
     │       ├── Si éxito:
     │       │     ├── status = "completed"
     │       │     ├── Actualizar entity.syncStatus = "synced"
     │       │     └── Actualizar entity.serverId = response.id
     │       │
     │       └── Si error:
     │             ├── retryCount++
     │             ├── Si retryCount < maxRetries → status = "pending"
     │             └── Si retryCount >= maxRetries → status = "failed"
     │
     └── 3. Limpiar items completados (>24h)
```

---

## 5. Resolución de Conflictos

### 5.1. Estrategia: Server Wins + Smart Merge

```
Al sincronizar:
     │
     ├── Obtener versión del servidor
     │
     ├── Comparar updated_at local vs servidor
     │
     ├── Si servidor es más reciente:
     │     └── Server wins: adoptar versión del servidor
     │
     ├── Si local es más reciente:
     │     └── Local wins: enviar versión local al servidor
     │
     └── Si ambos modificados (conflicto real):
           │
           ├── Para campos no-conflictivos:
           │     └── Merge: tomar el más reciente por campo
           │
           └── Para campos conflictivos:
                 └── Server wins + marcar como "conflict"
                       → Notificar al usuario
```

### 5.2. Tabla de Prioridades de Conflicto

| Campo                    | Estrategia         | Razón                          |
|--------------------------|--------------------|--------------------------------|
| estado_ficha             | Server wins        | El comité puede cambiar estado |
| score campos (F1-F4)     | Local wins         | Asesor tiene datos de campo    |
| comite_resolucion        | Server wins        | Solo el comité resuelve        |
| estado credito           | Server wins        | Múltiples actores              |
| observaciones            | Concatenar          | No perder información          |
| monto_aprobado           | Server wins        | Comité tiene última palabra    |

---

## 6. SyncManager (Orquestación)

### 6.1. Implementación

```kotlin
class SyncManager @Inject constructor(
    private val syncQueueDao: SyncQueueDao,
    private val remoteDataSource: RemoteDataSource,
    private val networkMonitor: NetworkMonitor,
    private val conflictResolver: ConflictResolver,
    private val workManager: WorkManager
) {
    // Sync inmediato (si hay red)
    suspend fun syncNow(): SyncResult {
        if (!networkMonitor.isConnected()) {
            return SyncResult.NoConnection
        }

        val pending = syncQueueDao.getPending()
        var success = 0
        var failed = 0

        for (item in pending) {
            try {
                syncQueueDao.updateStatus(item.id, "processing")
                processItem(item)
                syncQueueDao.updateStatus(item.id, "completed", System.currentTimeMillis())
                success++
            } catch (e: Exception) {
                handleError(item, e)
                failed++
            }
        }

        return SyncResult.Completed(success, failed)
    }

    // Programar sync periódico con WorkManager
    fun schedulePeriodic() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWork = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES,    // Intervalo mínimo WorkManager
            5, TimeUnit.MINUTES      // Flex interval
        )
        .setConstraints(constraints)
        .setBackoffCriteria(
            BackoffPolicy.EXPONENTIAL,
            WorkRequest.MIN_BACKOFF_MILLIS,
            TimeUnit.MILLISECONDS
        )
        .build()

        workManager.enqueueUniquePeriodicWork(
            "sync_periodic",
            ExistingPeriodicWorkPolicy.KEEP,
            syncWork
        )
    }

    // Descarga de datos del servidor
    suspend fun downloadCartera(asesorId: String): DownloadResult {
        // 1. Descargar clientes preaprobados asignados
        // 2. Descargar scores transaccionales
        // 3. Insertar/actualizar en Room
        // 4. Retornar resultado
    }
}

sealed class SyncResult {
    object NoConnection : SyncResult()
    data class Completed(val success: Int, val failed: Int) : SyncResult()
    data class Error(val message: String) : SyncResult()
}
```

---

## 7. Network Monitor

### 7.1. Implementación

```kotlin
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isOnline: StateFlow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }
            override fun onLost(network: Network) {
                trySend(false)
            }
            override fun onUnavailable() {
                trySend(false)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        // Estado inicial
        trySend(isConnected())

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, isConnected())

    fun isConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
```

---

## 8. Retry Policy

### 8.1. Backoff Exponencial

| Intento | Delay           | Acción si falla                    |
|---------|-----------------|-------------------------------------|
| 1       | Inmediato       | Retry                               |
| 2       | 30 segundos     | Retry                               |
| 3       | 2 minutos       | Retry                               |
| 4       | 10 minutos      | Retry                               |
| 5       | 30 minutos      | Marcar como "failed" + notificar    |

### 8.2. Fórmula de Delay

```kotlin
fun calculateDelay(retryCount: Int): Long {
    val baseDelay = 30_000L  // 30 segundos
    val maxDelay = 30 * 60 * 1000L  // 30 minutos
    val delay = baseDelay * 2.0.pow(retryCount).toLong()
    return minOf(delay, maxDelay)
}
```

---

## 9. UUID Local

### 9.1. Generación

```kotlin
// Cada entidad creada offline recibe un UUID local inmediatamente
val localId = UUID.randomUUID().toString()

// Al sincronizar, el servidor puede asignar un UUID diferente
// Se mantiene un mapping local_id ↔ server_id
```

### 9.2. Mapping Local-Server

| Escenario                       | Comportamiento                                |
|----------------------------------|-----------------------------------------------|
| Entidad creada offline           | id = UUID local, serverId = null              |
| Sincronizada con éxito          | serverId = UUID del servidor                  |
| Referencia entre entidades      | Usar id local en Room, serverId en Supabase   |
| Conflicto de UUID               | Mantener ambos, resolver en siguiente sync    |

---

## 10. Flujo de Datos Completo

```
┌─────────────────────────────────────────────────────────────┐
│                        OFFLINE MODE                          │
│                                                              │
│  [UI] ──► [ViewModel] ──► [UseCase] ──► [Repository]       │
│                                              │               │
│                                    ┌─────────┴────────────┐  │
│                                    │    Room Database      │  │
│                                    │    (fuente verdad)    │  │
│                                    └─────────┬────────────┘  │
│                                              │               │
│                                    ┌─────────┴────────────┐  │
│                                    │    Sync Queue         │  │
│                                    │    (operaciones)      │  │
│                                    └──────────────────────┘  │
│                                                              │
└──────────────────────┬───────────────────────────────────────┘
                       │
                       │  Cuando hay red ↓
                       │
┌──────────────────────┴───────────────────────────────────────┐
│                        ONLINE SYNC                            │
│                                                              │
│  [Sync Manager] ──► [Process Queue] ──► [Supabase API]      │
│       │                                       │               │
│       ├── Upload fichas pendientes ───────────┘               │
│       ├── Download cartera actualizada ◄──────┘               │
│       ├── Resolver conflictos                                 │
│       └── Actualizar Room con datos frescos                   │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## 11. Casos Edge

| Caso                                     | Comportamiento                                   |
|------------------------------------------|--------------------------------------------------|
| App abierta por primera vez sin internet | Mostrar mensaje "Conecta para descargar cartera" |
| Crear ficha offline, editar offline      | Todo en Room, encolar operaciones en orden        |
| App crash durante guardado               | Room es transaccional, datos seguros              |
| Batería agotada durante visita           | Auto-save cada campo modificado                  |
| Sync parcial (se pierde red a mitad)     | Items procesados = completed, resto = pending    |
| Servidor caído (500)                     | Retry con backoff, notificar después de 5 intentos|
| Datos eliminados en servidor             | Marcar como "deleted" en Room, no eliminar       |
| Espacio insuficiente en dispositivo      | Alertar al usuario, priorizar fichas pendientes  |
| Actualización de app con cambio de schema| Room migration con fallback a destructive         |

---

## 12. Criterios de Aceptación

- [ ] La app funciona completamente sin internet
- [ ] Las fichas se guardan inmediatamente en Room
- [ ] Los cambios se encolan en sync_queue
- [ ] La sincronización ocurre automáticamente al detectar red
- [ ] El NetworkMonitor detecta cambios de conectividad en tiempo real
- [ ] Los UUIDs locales se generan correctamente
- [ ] La resolución de conflictos funciona (server wins)
- [ ] El retry con backoff exponencial funciona
- [ ] Los datos se descargan correctamente desde Supabase
- [ ] Las fichas se suben correctamente a Supabase
- [ ] El WorkManager programa sync periódico cada 15 minutos
- [ ] El usuario ve indicadores de sync status (pendiente/sincronizado)
- [ ] No hay pérdida de datos en crash/batería agotada
- [ ] La paginación funciona con datasets grandes
