package com.gymtracker.domain.voice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SessionParserTest {
    private val parser = SessionParser()

    @Test
    fun `parse series de reps con kg`() {
        val result = parser.parse("3 series de 12 con 40 kilos")
        assertEquals(3, result.sets)
        assertEquals(12, result.reps)
        assertEquals(40f, result.weightKg)
        assertTrue(result.isComplete)
    }

    @Test
    fun `parse formato 3x12 a 40`() {
        val result = parser.parse("3x12 a 40")
        assertEquals(3, result.sets)
        assertEquals(12, result.reps)
        assertEquals(40f, result.weightKg)
    }

    @Test
    fun `parse numeros en palabras`() {
        val result = parser.parse("tres series de doce con cuarenta kilos")
        assertEquals(3, result.sets)
        assertEquals(12, result.reps)
        assertEquals(40f, result.weightKg)
    }

    @Test
    fun `parse sin peso`() {
        val result = parser.parse("3 series de 10 sin peso")
        assertEquals(3, result.sets)
        assertEquals(10, result.reps)
        assertEquals(0f, result.weightKg)
    }

    @Test
    fun `parse texto sin datos devuelve nulls`() {
        val result = parser.parse("hola qué tal")
        assertNull(result.sets)
        assertNull(result.reps)
        assertNull(result.weightKg)
    }

    @Test
    fun `parse kg con decimales`() {
        val result = parser.parse("4 series 8 repeticiones 82,5 kg")
        assertEquals(4, result.sets)
        assertEquals(8, result.reps)
        assertEquals(82.5f, result.weightKg)
    }
}
