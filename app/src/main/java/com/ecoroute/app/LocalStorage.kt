package com.ecoroute.app

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object LocalStorage {
    private const val ITERACIONES = 120_000
    private const val CLAVE_MODO_OSCURO = "modo_oscuro"
    private lateinit var db: EcoRouteDatabase

    fun inicializar(contexto: Context) {
        db = EcoRouteDatabase.obtener(contexto)
    }

    suspend fun cargarDatosIniciales() {
        AppState.modoOscuro = db.preferenciaDao().obtener(CLAVE_MODO_OSCURO).toBooleanSeguro()
        sembrarParadasSiHaceFalta()
        cargarParadas()
    }

    suspend fun guardarModoOscuro(activo: Boolean) {
        AppState.modoOscuro = activo
        db.preferenciaDao().guardar(PreferenciaEntity(CLAVE_MODO_OSCURO, activo.toString()))
    }

    suspend fun crearCuenta(nombre: String, usuario: String, contrasena: String, rol: String): Boolean {
        val normalizado = usuario.trim().lowercase()
        val id = idUsuario(normalizado)
        if (db.usuarioDao().obtenerPorId(id) != null) return false

        val sal = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = derivarClave(contrasena, sal)
        db.usuarioDao().insertar(
            UsuarioEntity(
                id = id,
                nombre = nombre.trim(),
                login = normalizado,
                rol = rol,
                sal = Base64.encodeToString(sal, Base64.NO_WRAP),
                hash = Base64.encodeToString(hash, Base64.NO_WRAP)
            )
        )
        AppState.usuarioId = id
        AppState.rol = rol
        return true
    }

    suspend fun iniciarSesion(usuario: String, contrasena: String): String? {
        val id = idUsuario(usuario.trim().lowercase())
        val usuarioLocal = db.usuarioDao().obtenerPorId(id) ?: return null
        val sal = Base64.decode(usuarioLocal.sal, Base64.NO_WRAP)
        val esperado = Base64.decode(usuarioLocal.hash, Base64.NO_WRAP)
        val recibido = derivarClave(contrasena, sal)
        if (!MessageDigest.isEqual(esperado, recibido)) return null
        AppState.usuarioId = usuarioLocal.id
        AppState.rol = usuarioLocal.rol
        cargarUltimaSolicitud(usuarioLocal.id)
        return usuarioLocal.nombre
    }

    suspend fun guardarRolUsuario(rol: String) {
        AppState.rol = rol
        if (AppState.usuarioId.isNotBlank()) {
            db.usuarioDao().actualizarRol(AppState.usuarioId, rol)
        }
    }

    suspend fun guardarSolicitudActual() {
        val usuarioId = AppState.usuarioId.ifBlank { idUsuario(AppState.usuario.ifBlank { "usuario" }.lowercase()) }
        db.solicitudDao().insertar(
            SolicitudEntity(
                usuarioId = usuarioId,
                materiales = AppState.materiales.joinToString(", "),
                bolsas = AppState.bolsas,
                horario = AppState.horario,
                referencia = AppState.referencia,
                direccion = "Residencial Las Flores, pasaje 4",
                estado = "En camino",
                creadaEn = System.currentTimeMillis()
            )
        )
        db.historialDao().insertar(
            HistorialEntity(
                usuarioId = usuarioId,
                fecha = "Hoy",
                material = AppState.materiales.joinToString(" y "),
                bolsas = "${AppState.bolsas} bolsas",
                estado = "En camino",
                creadaEn = System.currentTimeMillis()
            )
        )
    }

    suspend fun guardarParadaRecolectada(indice: Int) {
        val parada = AppState.paradas.getOrNull(indice) ?: return
        db.paradaDao().actualizar(parada.toEntity(indice + 1))
        db.historialDao().insertar(
            HistorialEntity(
                usuarioId = AppState.usuarioId.ifBlank { "recolector_local" },
                fecha = "Hoy",
                material = parada.material,
                bolsas = "${AppState.bolsasReales} bolsas",
                estado = "Completado",
                creadaEn = System.currentTimeMillis()
            )
        )
    }

    suspend fun reiniciarRuta() {
        db.paradaDao().reiniciarRuta()
        AppState.reiniciarRuta()
        cargarParadas()
    }

    suspend fun cargarParadas() {
        val paradas = db.paradaDao().listar().map { it.toParada() }
        if (paradas.isNotEmpty()) AppState.cargarParadas(paradas)
    }

    suspend fun cargarHistorialUsuario(): List<HistorialEntity> {
        val usuarioId = AppState.usuarioId.ifBlank { return emptyList() }
        return db.historialDao().listarDeUsuario(usuarioId)
    }

    private suspend fun cargarUltimaSolicitud(usuarioId: String) {
        val solicitud = db.solicitudDao().obtenerUltimaDeUsuario(usuarioId) ?: return
        AppState.materiales.clear()
        AppState.materiales.addAll(solicitud.materiales.split(", ").filter { it.isNotBlank() })
        AppState.bolsas = solicitud.bolsas
        AppState.horario = solicitud.horario
        AppState.referencia = solicitud.referencia
        AppState.solicitudEnviada = true
    }

    private suspend fun sembrarParadasSiHaceFalta() {
        if (db.paradaDao().contar() > 0) return
        db.paradaDao().guardarTodas(AppState.paradas.mapIndexed { indice, parada -> parada.toEntity(indice + 1) })
    }

    private fun idUsuario(usuario: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(usuario.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    private fun derivarClave(contrasena: String, sal: ByteArray): ByteArray {
        val especificacion = PBEKeySpec(contrasena.toCharArray(), sal, ITERACIONES, 256)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(especificacion).encoded
        } finally {
            especificacion.clearPassword()
        }
    }

    private fun String?.toBooleanSeguro(): Boolean = this?.toBooleanStrictOrNull() ?: false

    private fun Parada.toEntity(id: Int): ParadaEntity = ParadaEntity(
        id = id,
        nombre = nombre,
        direccion = direccion,
        material = material,
        detalle = detalle,
        distancia = distancia,
        horario = horario,
        horarioVerde = horarioVerde,
        referencia = referencia,
        latitud = latitud,
        longitud = longitud,
        recolectada = recolectada
    )

    private fun ParadaEntity.toParada(): Parada = Parada(
        nombre = nombre,
        direccion = direccion,
        material = material,
        detalle = detalle,
        distancia = distancia,
        horario = horario,
        horarioVerde = horarioVerde,
        referencia = referencia,
        latitud = latitud,
        longitud = longitud
    ).also { it.recolectada = recolectada }
}
