package com.ecoroute.app

import android.content.Context
import android.util.Base64
import androidx.room.withTransaction
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object LocalStorage {
    private const val ITERACIONES = 120_000
    private const val CLAVE_MODO_OSCURO = "modo_oscuro"
    private const val CLAVE_AHORRO_BATERIA = "ahorro_bateria"
    private const val USUARIO_LOCAL_RESPALDO = "recolector_local"
    private lateinit var db: EcoRouteDatabase

    private fun usuarioIdActual(): String = AppState.usuarioId.ifBlank { USUARIO_LOCAL_RESPALDO }

    fun inicializar(contexto: Context) {
        db = EcoRouteDatabase.obtener(contexto)
    }

    internal fun inicializar(database: EcoRouteDatabase) {
        db = database
    }

    suspend fun cargarDatosIniciales() {
        AppState.modoOscuro = db.preferenciaDao().obtener(CLAVE_MODO_OSCURO).toBooleanSeguro()
        AppState.ahorroBateria = db.preferenciaDao().obtener(CLAVE_AHORRO_BATERIA).toBooleanSeguro()
    }

    suspend fun guardarModoOscuro(activo: Boolean) {
        AppState.modoOscuro = activo
        db.preferenciaDao().guardar(PreferenciaEntity(CLAVE_MODO_OSCURO, activo.toString()))
    }

    suspend fun guardarAhorroBateria(activo: Boolean) {
        AppState.ahorroBateria = activo
        db.preferenciaDao().guardar(PreferenciaEntity(CLAVE_AHORRO_BATERIA, activo.toString()))
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
        limpiarSolicitudActual()
        cargarParadas()
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
        cargarParadas()
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
        val creadaEn = System.currentTimeMillis()
        db.withTransaction {
            val solicitudId = db.solicitudDao().insertar(
                SolicitudEntity(
                    usuarioId = usuarioId,
                    materiales = AppState.materiales.joinToString(", "),
                    bolsas = AppState.bolsas,
                    tamanoBolsa = AppState.tamanoBolsa,
                    horario = AppState.horario,
                    referencia = AppState.referencia,
                    direccion = AppState.direccion,
                    latitud = AppState.latitudSolicitud,
                    longitud = AppState.longitudSolicitud,
                    estado = "En camino",
                    creadaEn = creadaEn
                )
            )
            db.historialDao().insertar(
                HistorialEntity(
                    usuarioId = usuarioId,
                    fecha = "Hoy",
                    material = AppState.materiales.joinToString(" y "),
                    bolsas = "${AppState.bolsas} bolsas",
                    estado = "En camino",
                    creadaEn = creadaEn,
                    solicitudId = solicitudId
                )
            )
        }
        AppState.solicitudEnviada = true
    }

    suspend fun guardarParadaRecolectada(indice: Int, bolsas: Int) {
        val usuarioId = usuarioIdActual()
        val parada = AppState.paradas.getOrNull(indice) ?: return
        if (parada.recolectada) return
        val actualizada = parada.toEntity(usuarioId, indice + 1).copy(recolectada = true)
        db.withTransaction {
            db.paradaDao().actualizar(actualizada)
            val solicitudId = parada.solicitudId
            if (solicitudId != null) {
                // Actualiza la fila "En camino" ya existente en vez de duplicarla, para que
                // el historial no muestre la misma solicitud dos veces con estados distintos.
                // Se actualiza por usuario+solicitud (no solo por solicitud) porque el vecino
                // y el recolector tienen cada uno su propia fila para el mismo solicitudId.
                db.historialDao().actualizarPorSolicitudYUsuario(solicitudId, usuarioId, "Completado", "$bolsas bolsas")
                db.solicitudDao().obtenerPorId(solicitudId)?.let { solicitud ->
                    db.historialDao().actualizarPorSolicitudYUsuario(solicitudId, solicitud.usuarioId, "Completado", "$bolsas bolsas")
                }
                db.solicitudDao().actualizarEstadoYBolsas(solicitudId, "Completado", bolsas)
            }
            // El historial del recolector es independiente del historial del vecino. Si la
            // parada viene de una solicitud, ya tiene una fila "En camino" propia (insertada
            // al sincronizar) y actualizarPorSolicitud la acaba de pasar a "Completado"; solo
            // hace falta insertar una fila nueva para las paradas demo (sin solicitud detrás).
            if (solicitudId == null) {
                db.historialDao().insertar(
                    HistorialEntity(
                        usuarioId = usuarioId,
                        fecha = "Hoy",
                        material = parada.material,
                        bolsas = "$bolsas bolsas",
                        estado = "Completado",
                        creadaEn = System.currentTimeMillis(),
                        solicitudId = solicitudId
                    )
                )
            }
        }
        parada.recolectada = true
    }

    suspend fun reiniciarRuta() {
        val usuarioId = usuarioIdActual()
        db.paradaDao().reiniciarRutaDeUsuario(usuarioId)
        AppState.reiniciarRuta()
        cargarParadas()
    }

    suspend fun cargarParadas() {
        val usuarioId = usuarioIdActual()
        if (AppState.rol == "recolector") {
            sembrarParadasSiHaceFalta(usuarioId)
            sincronizarParadasDesdeSolicitudes(usuarioId)
            // Las 4 paradas demo son solo un respaldo para poder probar la ruta y la
            // brújula mientras el recolector no tiene ninguna solicitud real. En cuanto
            // llega la primera solicitud real, se descartan para no mezclar datos falsos
            // con la ruta real.
            if (db.paradaDao().listarSolicitudIdsDeUsuario(usuarioId).isNotEmpty()) {
                db.paradaDao().eliminarDemoDeUsuario(usuarioId)
            }
        }
        val paradas = db.paradaDao().listarDeUsuario(usuarioId).map { it.toParada() }
        if (paradas.isNotEmpty()) AppState.cargarParadas(paradas)
    }

    /**
     * Las paradas de un recolector no son solo la ruta estática de demostración: cada
     * solicitud de un vecino con estado "En camino" (sin importar de qué usuario venga,
     * porque en esta app vecino y recolector son solo cuentas locales en el mismo
     * dispositivo/BD, no hay backend) debe convertirse en una parada nueva para que el
     * recolector la vea y la complete.
     */
    private suspend fun sincronizarParadasDesdeSolicitudes(usuarioId: String) {
        val activas = db.solicitudDao().listarActivas(usuarioId)
        if (activas.isEmpty()) return
        val yaVinculadas = db.paradaDao().listarSolicitudIdsDeUsuario(usuarioId).toSet()
        val nuevas = activas.filter { it.id !in yaVinculadas }
        if (nuevas.isEmpty()) return
        nuevas.forEach { solicitud ->
            if (solicitud.recolectorAsignado == null) {
                db.solicitudDao().asignarRecolector(solicitud.id, usuarioId)
            }
        }
        var siguienteId = db.paradaDao().maxIdDeUsuario(usuarioId)
        val entidades = nuevas.map { solicitud ->
            siguienteId++
            val nombreVecino = db.usuarioDao().obtenerPorId(solicitud.usuarioId)?.nombre ?: "Vecino"
            ParadaEntity(
                usuarioId = usuarioId,
                id = siguienteId,
                nombre = nombreVecino,
                direccion = solicitud.direccion,
                material = solicitud.materiales,
                detalle = "${solicitud.bolsas} bolsas ${solicitud.tamanoBolsa.lowercase()}s",
                distancia = "Nueva solicitud",
                horario = solicitud.horario,
                horarioVerde = false,
                referencia = solicitud.referencia,
                latitud = solicitud.latitud,
                longitud = solicitud.longitud,
                recolectada = false,
                solicitudId = solicitud.id
            )
        }
        db.paradaDao().guardarTodas(entidades)
        db.historialDao().insertarTodas(
            nuevas.map { solicitud ->
                HistorialEntity(
                    usuarioId = usuarioId,
                    fecha = "Hoy",
                    material = solicitud.materiales,
                    bolsas = "${solicitud.bolsas} bolsas",
                    estado = "En camino",
                    creadaEn = System.currentTimeMillis(),
                    solicitudId = solicitud.id
                )
            }
        )
    }

    suspend fun cargarHistorialUsuario(): List<HistorialEntity> {
        val usuarioId = AppState.usuarioId.ifBlank { return emptyList() }
        return db.historialDao().listarDeUsuario(usuarioId)
    }

    suspend fun cargarUltimaSolicitudUsuario(): SolicitudEntity? {
        val usuarioId = AppState.usuarioId.ifBlank { return null }
        return db.solicitudDao().obtenerUltimaDeUsuario(usuarioId)
    }

    suspend fun cargarSolicitudesActivasUsuario(): List<SolicitudEntity> {
        val usuarioId = AppState.usuarioId.ifBlank { return emptyList() }
        return db.solicitudDao().listarActivasDeUsuario(usuarioId)
    }

    private suspend fun cargarUltimaSolicitud(usuarioId: String) {
        limpiarSolicitudActual()
        val solicitud = db.solicitudDao().obtenerUltimaDeUsuario(usuarioId)
        if (solicitud == null) return
        AppState.materiales.clear()
        AppState.materiales.addAll(solicitud.materiales.split(", ").filter { it.isNotBlank() })
        AppState.bolsas = solicitud.bolsas
        AppState.horario = solicitud.horario
        AppState.referencia = solicitud.referencia
        AppState.solicitudEnviada = true
    }

    private fun limpiarSolicitudActual() {
        AppState.materiales.clear()
        AppState.bolsas = 0
        AppState.horario = ""
        AppState.referencia = ""
        AppState.solicitudEnviada = false
    }

    private suspend fun sembrarParadasSiHaceFalta(usuarioId: String) {
        if (db.paradaDao().contarDeUsuario(usuarioId) > 0) return
        db.paradaDao().guardarTodas(
            paradasDemo().mapIndexed { indice, parada -> parada.toEntity(usuarioId, indice + 1) }
        )
    }

    // Plantilla fija para la ruta demo. No usar AppState.paradas acá: es un singleton que
    // queda con los datos de la última sesión activa, y sembrar desde ahí clona la parada
    // real de un recolector hacia el siguiente que se registre.
    private fun paradasDemo(): List<Parada> = listOf(
        Parada("Andrea M.", "Residencial Las Flores, pasaje 4", "Plástico y papel", "2 bolsas medianas 8-12 kg", "0.6 km", "2:00-5:00-p.m", true, "Portón verde, bolsas junto al árbol de mango.", 13.9946, -89.5597),
        Parada("Familia Lopez", "Colonia El Palmar, casa 12", "Vidrio", "1 bolsa grande 10-15 kg", "1.1 km", "2:00-5:00-p.m", false, "Casa esquinera, bolsas en la acera.", 13.9992, -89.5561),
        Parada("Carlos P", "Barrio Santa Lucía, av. 3", "Metal y papel", "3 bolsas medianas 12-18 kg", "1.8 km", "Sin restriccion", false, "Portón negro, tocar el timbre.", 14.0031, -89.5518),
        Parada("Marta P", "Residencial Altavista, block C", "Plástico", "2 bolsas pequeñas 4-6 kg", "2.2 km", "Sin restriccion", false, "Dejar aviso al vigilante.", 14.0074, -89.5476)
    )

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

    internal fun Parada.toEntity(usuarioId: String, id: Int): ParadaEntity = ParadaEntity(
        usuarioId = usuarioId,
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
        recolectada = recolectada,
        solicitudId = solicitudId
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
    ).also {
        it.recolectada = recolectada
        it.solicitudId = solicitudId
    }
}
