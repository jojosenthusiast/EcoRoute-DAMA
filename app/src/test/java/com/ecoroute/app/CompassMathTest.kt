package com.ecoroute.app

import org.junit.Assert.assertEquals
import org.junit.Test

class CompassMathTest {
    @Test
    fun `cardinal bearings from origin`() {
        val origin = GeoPoint(0.0, 0.0)

        assertEquals(0f, bearingDegrees(origin, GeoPoint(1.0, 0.0)), 0.001f)
        assertEquals(90f, bearingDegrees(origin, GeoPoint(0.0, 1.0)), 0.001f)
        assertEquals(180f, bearingDegrees(origin, GeoPoint(-1.0, 0.0)), 0.001f)
        assertEquals(270f, bearingDegrees(origin, GeoPoint(0.0, -1.0)), 0.001f)
    }

    @Test
    fun `needle rotation wraps around`() {
        assertEquals(20f, needleRotationDegrees(10f, 350f), 0.001f)
        assertEquals(340f, needleRotationDegrees(350f, 10f), 0.001f)
    }

    @Test
    fun `display angle rounds and wraps`() {
        assertEquals("20°", displayAngle(19.6f))
        assertEquals("0°", displayAngle(359.6f))
    }

    @Test
    fun `distance is zero at the same point and grows with real coordinates`() {
        val origin = GeoPoint(13.9946, -89.5597)

        assertEquals(0.0, distanceKm(origin, origin), 0.001)
        assertEquals(0.642, distanceKm(origin, GeoPoint(13.9992, -89.5561)), 0.01)
    }

    @Test
    fun `distance formatting switches between meters and kilometers`() {
        assertEquals("450 m", formatDistanceKm(0.45))
        assertEquals("1.1 km", formatDistanceKm(1.09))
        assertEquals("2.2 km", formatDistanceKm(2.2))
    }

    @Test
    fun guidanceUsesDestinationBearingAndHeading() {
        val guidance = destinationGuidance(
            origin = GeoPoint(0.0, 0.0),
            destination = GeoPoint(0.0, 1.0),
            heading = 350f
        )

        assertEquals(100f, guidance.rotation, 0.001f)
        assertEquals("100°", guidance.angleText)
    }

    @Test
    fun guidanceUiStateUsesTruthfulPriorityAndReadiness() {
        val point = LocationResult.Available(GeoPoint(0.0, 0.0))

        assertEquals(
            CompassUiState("Este dispositivo no tiene los sensores requeridos.", false),
            compassUiState(LocationResult.PermissionRequired, false, false, null, "Destino")
        )
        assertEquals(
            CompassUiState("Activa el permiso de ubicación para orientar la brújula hacia esta parada.", false),
            compassUiState(LocationResult.PermissionRequired, true, true, 0f, "Destino")
        )
        assertEquals(
            CompassUiState("No hay una ubicación disponible. Activa la ubicación y vuelve a intentarlo.", false),
            compassUiState(LocationResult.Unavailable, true, true, 0f, "Destino")
        )
        assertEquals(
            CompassUiState("No se pudo iniciar la brújula.", false),
            compassUiState(point, true, false, null, "Destino")
        )
        assertEquals(
            CompassUiState("Esperando orientación de los sensores.", false),
            compassUiState(point, true, true, null, "Destino")
        )
        assertEquals(
            CompassUiState("La flecha apunta hacia Destino.", true),
            compassUiState(point, true, true, 90f, "Destino")
        )
        assertEquals(
            CompassUiState("Brújula descalibrada: mové el celular en forma de 8, lejos de imanes o metal.", false),
            compassUiState(point, true, true, 90f, "Destino", necesitaCalibracion = true)
        )
    }

    @Test
    fun locationTrackingRejectsARecentButMuchLessAccurateProviderUpdate() {
        val previous = LocationResult.Available(
            point = GeoPoint(13.9946, -89.5597),
            accuracyMeters = 8f,
            timestampMillis = 10_000L
        )
        val poorNetworkUpdate = LocationResult.Available(
            point = GeoPoint(13.9950, -89.5600),
            accuracyMeters = 150f,
            timestampMillis = 11_000L
        )
        val betterGpsUpdate = LocationResult.Available(
            point = GeoPoint(13.9947, -89.5598),
            accuracyMeters = 5f,
            timestampMillis = 12_000L
        )

        assertEquals(false, shouldAcceptLocation(previous, poorNetworkUpdate))
        assertEquals(true, shouldAcceptLocation(previous, betterGpsUpdate))
        assertEquals(true, shouldAcceptLocation(null, poorNetworkUpdate))
    }
}
