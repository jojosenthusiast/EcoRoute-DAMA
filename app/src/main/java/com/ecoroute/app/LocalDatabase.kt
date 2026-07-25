package com.ecoroute.app

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val login: String,
    val rol: String,
    val sal: String,
    val hash: String
)

@Entity(tableName = "preferencias")
data class PreferenciaEntity(
    @PrimaryKey val clave: String,
    val valor: String
)

@Entity(tableName = "solicitudes")
data class SolicitudEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: String,
    val materiales: String,
    val bolsas: Int,
    val tamanoBolsa: String = "Mediana",
    val horario: String,
    val referencia: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double,
    val estado: String,
    val creadaEn: Long,
    val recolectorAsignado: String? = null
)

@Entity(tableName = "paradas", primaryKeys = ["usuarioId", "id"])
data class ParadaEntity(
    val usuarioId: String,
    val id: Int,
    val nombre: String,
    val direccion: String,
    val material: String,
    val detalle: String,
    val distancia: String,
    val horario: String,
    val horarioVerde: Boolean,
    val referencia: String,
    val latitud: Double,
    val longitud: Double,
    val recolectada: Boolean,
    val solicitudId: Long? = null
)

@Entity(tableName = "historial_recolecciones")
data class HistorialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: String,
    val fecha: String,
    val material: String,
    val bolsas: String,
    val estado: String,
    val creadaEn: Long,
    val solicitudId: Long? = null
)

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: String): UsuarioEntity?

    @Query("UPDATE usuarios SET rol = :rol WHERE id = :id")
    suspend fun actualizarRol(id: String, rol: String)
}

@Dao
interface PreferenciaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(preferencia: PreferenciaEntity)

    @Query("SELECT valor FROM preferencias WHERE clave = :clave LIMIT 1")
    suspend fun obtener(clave: String): String?
}

@Dao
interface SolicitudDao {
    @Insert
    suspend fun insertar(solicitud: SolicitudEntity): Long

    @Query("SELECT * FROM solicitudes WHERE usuarioId = :usuarioId ORDER BY creadaEn DESC LIMIT 1")
    suspend fun obtenerUltimaDeUsuario(usuarioId: String): SolicitudEntity?

    @Query("SELECT * FROM solicitudes WHERE usuarioId = :usuarioId ORDER BY creadaEn DESC")
    suspend fun listarDeUsuario(usuarioId: String): List<SolicitudEntity>

    @Query("SELECT * FROM solicitudes WHERE estado = 'En camino' AND (recolectorAsignado IS NULL OR recolectorAsignado = :usuarioId) ORDER BY creadaEn ASC")
    suspend fun listarActivas(usuarioId: String): List<SolicitudEntity>

    @Query("SELECT * FROM solicitudes WHERE usuarioId = :usuarioId AND estado != 'Completado' ORDER BY creadaEn ASC")
    suspend fun listarActivasDeUsuario(usuarioId: String): List<SolicitudEntity>

    @Query("SELECT * FROM solicitudes WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): SolicitudEntity?

    @Query("UPDATE solicitudes SET estado = :estado, bolsas = :bolsas WHERE id = :id")
    suspend fun actualizarEstadoYBolsas(id: Long, estado: String, bolsas: Int)

    @Query("UPDATE solicitudes SET recolectorAsignado = :recolectorId WHERE id = :id")
    suspend fun asignarRecolector(id: Long, recolectorId: String)
}

@Dao
interface ParadaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodas(paradas: List<ParadaEntity>)

    @Update
    suspend fun actualizar(parada: ParadaEntity)

    @Query("SELECT * FROM paradas WHERE usuarioId = :usuarioId ORDER BY (solicitudId IS NULL) ASC, id ASC")
    suspend fun listarDeUsuario(usuarioId: String): List<ParadaEntity>

    @Query("SELECT COUNT(*) FROM paradas WHERE usuarioId = :usuarioId")
    suspend fun contarDeUsuario(usuarioId: String): Int

    @Query("UPDATE paradas SET recolectada = 0 WHERE usuarioId = :usuarioId")
    suspend fun reiniciarRutaDeUsuario(usuarioId: String)

    @Query("SELECT COALESCE(MAX(id), 0) FROM paradas WHERE usuarioId = :usuarioId")
    suspend fun maxIdDeUsuario(usuarioId: String): Int

    @Query("SELECT solicitudId FROM paradas WHERE usuarioId = :usuarioId AND solicitudId IS NOT NULL")
    suspend fun listarSolicitudIdsDeUsuario(usuarioId: String): List<Long>

    @Query("DELETE FROM paradas WHERE usuarioId = :usuarioId AND solicitudId IS NULL AND recolectada = 0")
    suspend fun eliminarDemoDeUsuario(usuarioId: String)
}

@Dao
interface HistorialDao {
    @Insert
    suspend fun insertar(historial: HistorialEntity): Long

    @Insert
    suspend fun insertarTodas(historial: List<HistorialEntity>)

    @Query("SELECT * FROM historial_recolecciones WHERE usuarioId = :usuarioId ORDER BY creadaEn DESC")
    suspend fun listarDeUsuario(usuarioId: String): List<HistorialEntity>

    @Query("UPDATE historial_recolecciones SET estado = :estado, bolsas = :bolsas WHERE solicitudId = :solicitudId AND usuarioId = :usuarioId")
    suspend fun actualizarPorSolicitudYUsuario(solicitudId: Long, usuarioId: String, estado: String, bolsas: String)
}

@Database(
    entities = [
        UsuarioEntity::class,
        PreferenciaEntity::class,
        SolicitudEntity::class,
        ParadaEntity::class,
        HistorialEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class EcoRouteDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun preferenciaDao(): PreferenciaDao
    abstract fun solicitudDao(): SolicitudDao
    abstract fun paradaDao(): ParadaDao
    abstract fun historialDao(): HistorialDao

    companion object {
        @Volatile private var instancia: EcoRouteDatabase? = null

        fun obtener(contexto: Context): EcoRouteDatabase {
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    contexto.applicationContext,
                    EcoRouteDatabase::class.java,
                    "ecoroute.db"
                ).fallbackToDestructiveMigration().build().also { instancia = it }
            }
        }
    }
}
