package com.ecoroute.app

import org.junit.Assert.assertEquals
import org.junit.Test

class RouteSummaryTest {
    @Test
    fun `four loaded stops with one completed reports route summary`() {
        val stops = AppState.paradas.map {
            Parada(
                it.nombre,
                it.direccion,
                it.material,
                it.detalle,
                it.distancia,
                it.horario,
                it.horarioVerde,
                it.referencia,
                it.latitud,
                it.longitud
            )
        }
        stops.first().recolectada = true

        assertEquals(RouteSummary(total = 4, pending = 3, completed = 1), routeSummary(stops))
    }
}
