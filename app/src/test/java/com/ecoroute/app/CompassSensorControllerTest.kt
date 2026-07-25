package com.ecoroute.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompassSensorControllerTest {
    @Test
    fun registersOnResumeAndUnregistersOnPauseOnlyOnce() {
        var registrations = 0
        var releases = 0
        val lifecycle = SensorLifecycle(
            register = {
                registrations++
                true
            },
            unregister = { releases++ }
        )

        lifecycle.onResume()
        lifecycle.onResume()
        lifecycle.onPause()
        lifecycle.onPause()

        assertEquals(1, registrations)
        assertEquals(1, releases)
    }

    @Test
    fun controllerFailedRegistrationCleansUpStaysInactiveAndRetries() {
        var accelerometerAttempts = 0
        var magnetometerAttempts = 0
        var cleanups = 0
        val controller = CompassSensorController(
            sensorsAvailable = true,
            registerAccelerometer = { ++accelerometerAttempts > 1 },
            registerMagnetometer = { ++magnetometerAttempts > 1 },
            unregister = { cleanups++ },
            calculateHeading = { _, _ -> 0f },
            onHeading = {}
        )

        controller.onResume()
        assertFalse(controller.isActive)
        assertEquals(0, magnetometerAttempts)
        assertEquals(1, cleanups)

        controller.onResume()
        assertFalse(controller.isActive)
        assertEquals(2, cleanups)

        controller.onResume()
        assertTrue(controller.isActive)
        assertEquals(3, accelerometerAttempts)
        assertEquals(2, magnetometerAttempts)
    }

    @Test
    fun controllerIgnoresSamplesWhileInactive() {
        val headings = mutableListOf<Float>()
        val controller = CompassSensorController(
            sensorsAvailable = true,
            registerAccelerometer = { true },
            registerMagnetometer = { true },
            unregister = {},
            calculateHeading = { _, _ -> 90f },
            onHeading = headings::add
        )

        controller.onSample(SensorKind.ACCELEROMETER, floatArrayOf(1f, 2f, 3f))
        controller.onSample(SensorKind.MAGNETIC_FIELD, floatArrayOf(4f, 5f, 6f))

        assertTrue(headings.isEmpty())
    }

    @Test
    fun controllerRequiresBothFreshSensorKindsAfterResume() {
        val headings = mutableListOf<Float>()
        val controller = CompassSensorController(
            sensorsAvailable = true,
            registerAccelerometer = { true },
            registerMagnetometer = { true },
            unregister = {},
            calculateHeading = { _, _ -> -10f },
            onHeading = headings::add
        )

        controller.onResume()
        controller.onSample(SensorKind.ACCELEROMETER, floatArrayOf(0f, 0f, 9.81f))
        controller.onSample(SensorKind.MAGNETIC_FIELD, floatArrayOf(4f, 5f, 6f))
        assertEquals(listOf(350f), headings)

        controller.onPause()
        controller.onResume()
        controller.onSample(SensorKind.ACCELEROMETER, floatArrayOf(0f, 0f, 9.7f))
        assertEquals(1, headings.size)

        controller.onSample(SensorKind.MAGNETIC_FIELD, floatArrayOf(10f, 11f, 12f))
        assertEquals(listOf(350f, 350f), headings)
    }

    @Test
    fun controllerReportsRegistrationAndFreshHeadingReadiness() {
        var registrationSucceeds = true
        val controller = CompassSensorController(
            sensorsAvailable = true,
            registerAccelerometer = { registrationSucceeds },
            registerMagnetometer = { registrationSucceeds },
            unregister = {},
            calculateHeading = { _, _ -> 45f },
            onHeading = {}
        )

        assertTrue(controller.onResume())
        assertFalse(controller.headingReady)
        controller.onSample(SensorKind.ACCELEROMETER, floatArrayOf(0f, 0f, 9.81f))
        assertFalse(controller.headingReady)
        controller.onSample(SensorKind.MAGNETIC_FIELD, floatArrayOf(4f, 5f, 6f))
        assertTrue(controller.headingReady)

        controller.onPause()
        assertFalse(controller.headingReady)
        registrationSucceeds = false
        assertFalse(controller.onResume())
        assertFalse(controller.headingReady)
    }

    @Test
    fun `gravity fusion ignores samples corrupted by device movement`() {
        val fusion = SensorFusionState()

        // Una primera muestra tomada mientras el usuario ya camina tampoco debe aceptarse.
        fusion.updateGravity(floatArrayOf(0f, 8f, 9.81f))
        assertFalse(fusion.ready)
        assertEquals(0f, fusion.gravity[1], 0.001f)
        assertEquals(0f, fusion.gravity[2], 0.001f)

        fusion.updateGravity(floatArrayOf(0f, 0f, 9.81f))
        assertEquals(0f, fusion.gravity[1], 0.001f)
        assertEquals(9.81f, fusion.gravity[2], 0.001f)

        // Simulated phone movement: raw accelerometer magnitude far from ~9.8 m/s^2.
        fusion.updateGravity(floatArrayOf(0f, 8f, 9.81f))
        assertEquals(0f, fusion.gravity[1], 0.001f)
        assertEquals(9.81f, fusion.gravity[2], 0.001f)

        // Device settles back down: a reading close to gravity is accepted again.
        fusion.updateGravity(floatArrayOf(0f, 0f, 9.7f))
        assertTrue(fusion.gravity[2] < 9.81f)
    }
}
