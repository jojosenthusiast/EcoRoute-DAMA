package com.ecoroute.app

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object LocalStorage {
    private const val PREFS = "ecoroute_local"
    private const val ITERACIONES = 120_000
    private lateinit var prefs: SharedPreferences

    fun inicializar(contexto: Context) {
        prefs = contexto.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        AppState.modoOscuro = prefs.getBoolean("modo_oscuro", false)
    }

    fun guardarModoOscuro(activo: Boolean) {
        AppState.modoOscuro = activo
        prefs.edit().putBoolean("modo_oscuro", activo).apply()
    }

    fun crearCuenta(nombre: String, usuario: String, contrasena: String): Boolean {
        val normalizado = usuario.trim().lowercase()
        val id = idUsuario(normalizado)
        if (prefs.contains("usuario_${id}_hash")) return false

        val sal = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = derivarClave(contrasena, sal)
        prefs.edit()
            .putString("usuario_${id}_nombre", nombre.trim())
            .putString("usuario_${id}_login", normalizado)
            .putString("usuario_${id}_sal", Base64.encodeToString(sal, Base64.NO_WRAP))
            .putString("usuario_${id}_hash", Base64.encodeToString(hash, Base64.NO_WRAP))
            .apply()
        return true
    }

    fun iniciarSesion(usuario: String, contrasena: String): String? {
        val id = idUsuario(usuario.trim().lowercase())
        val salTexto = prefs.getString("usuario_${id}_sal", null) ?: return null
        val hashTexto = prefs.getString("usuario_${id}_hash", null) ?: return null
        val sal = Base64.decode(salTexto, Base64.NO_WRAP)
        val esperado = Base64.decode(hashTexto, Base64.NO_WRAP)
        val recibido = derivarClave(contrasena, sal)
        if (!MessageDigest.isEqual(esperado, recibido)) return null
        return prefs.getString("usuario_${id}_nombre", usuario.trim())
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
}
