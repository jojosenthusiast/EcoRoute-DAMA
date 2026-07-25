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
    val horario: String,
    val referencia: String,
    val direccion: String,
    val estado: String,
    val creadaEn: Long
)

@Entity(tableName = "paradas")
data class ParadaEntity(
    @PrimaryKey val id: Int,
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
    val recolectada: Boolean
)

@Entity(tableName = "historial_recolecciones")
data class HistorialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: String,
    val fecha: String,
    val material: String,
    val bolsas: String,
    val estado: String,
    val creadaEn: Long
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
}

@Dao
interface ParadaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodas(paradas: List<ParadaEntity>)

    @Update
    suspend fun actualizar(parada: ParadaEntity)

    @Query("SELECT * FROM paradas ORDER BY id ASC")
    suspend fun listar(): List<ParadaEntity>

    @Query("SELECT COUNT(*) FROM paradas")
    suspend fun contar(): Int

    @Query("UPDATE paradas SET recolectada = 0")
    suspend fun reiniciarRuta()
}

@Dao
interface HistorialDao {
    @Insert
    suspend fun insertar(historial: HistorialEntity): Long

    @Query("SELECT * FROM historial_recolecciones WHERE usuarioId = :usuarioId ORDER BY creadaEn DESC")
    suspend fun listarDeUsuario(usuarioId: String): List<HistorialEntity>
}

@Database(
    entities = [
        UsuarioEntity::class,
        PreferenciaEntity::class,
        SolicitudEntity::class,
        ParadaEntity::class,
        HistorialEntity::class
    ],
    version = 1,
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
                ).build().also { instancia = it }
            }
        }
    }
}
