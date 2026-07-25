package com.ecoroute.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class Parada(
    val nombre: String,
    val direccion: String,
    val material: String,
    val detalle: String,
    val distancia: String,
    val horario: String,
    val horarioVerde: Boolean,
    val referencia: String,
    val latitud: Double,
    val longitud: Double
) {
    var recolectada by mutableStateOf(false)
}

object AppState {
    var rol by mutableStateOf("vecino")
    var usuario by mutableStateOf("")
    var modoOscuro by mutableStateOf(false)

    const val LATITUD_CASA = 13.9946
    const val LONGITUD_CASA = -89.5597

    val materiales = mutableStateListOf("Plástico", "Papel")
    var bolsas by mutableIntStateOf(2)
    var horario by mutableStateOf("Hoy 3-5 p.m.")
    var referencia by mutableStateOf("Portón verde, dejar las bolsas junto al árbol.")
    var autorizoUbicacion by mutableStateOf(true)
    var solicitudEnviada by mutableStateOf(true)

    var paradaActual by mutableIntStateOf(0)
    var bolsasReales by mutableIntStateOf(2)
    var notaRecolector by mutableStateOf("")

    val paradas = mutableStateListOf(
        Parada("Andrea M.", "Residencial Las Flores, pasaje 4", "Plástico y papel", "2 bolsas medianas 8-12 kg", "0.6 km", "2:00-5:00-p.m", true, "Portón verde, bolsas junto al árbol de mango.", 13.9946, -89.5597),
        Parada("Familia Lopez", "Colonia El Palmar, casa 12", "Vidrio", "1 bolsa grande 10-15 kg", "1.1 km", "2:00-5:00-p.m", false, "Casa esquinera, bolsas en la acera.", 13.9992, -89.5561),
        Parada("Carlos P", "Barrio Santa Lucía, av. 3", "Metal y papel", "3 bolsas medianas 12-18 kg", "1.8 km", "Sin restriccion", false, "Portón negro, tocar el timbre.", 14.0031, -89.5518),
        Parada("Marta P", "Residencial Altavista, block C", "Plástico", "2 bolsas pequeñas 4-6 kg", "2.2 km", "Sin restriccion", false, "Dejar aviso al vigilante.", 14.0074, -89.5476)
    )

    fun reiniciarRuta() {
        paradas.forEach { it.recolectada = false }
        paradaActual = 0
    }
}
