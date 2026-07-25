package com.ecoroute.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CollectionValidationTest {
    @Test
    fun emptyMaterialsAreRejected() {
        assertEquals("Selecciona al menos un material.", validateCollection(emptyList(), 1))
    }

    @Test
    fun selectedMaterialAndPositiveBagCountAreAccepted() {
        assertNull(validateCollection(listOf("Papel"), 1))
    }

    @Test
    fun fewerThanOneBagIsRejected() {
        assertEquals("Agrega al menos una bolsa.", validateCollection(listOf("Papel"), 0))
    }
}
