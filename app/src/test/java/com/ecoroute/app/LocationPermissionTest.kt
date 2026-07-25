package com.ecoroute.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationPermissionTest {
    @Test
    fun finePermissionGrantsLocation() {
        assertTrue(hasLocationPermission(fineGranted = true, coarseGranted = false))
    }

    @Test
    fun coarsePermissionGrantsLocation() {
        assertTrue(hasLocationPermission(fineGranted = false, coarseGranted = true))
    }

    @Test
    fun noLocationPermissionDeniesLocation() {
        assertFalse(hasLocationPermission(fineGranted = false, coarseGranted = false))
    }
}
