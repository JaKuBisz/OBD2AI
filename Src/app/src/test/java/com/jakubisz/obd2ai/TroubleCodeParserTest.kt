package com.jakubisz.obd2ai

import com.jakubisz.obd2ai.data.obd.TroubleCodeParser
import org.junit.Assert.assertEquals
import org.junit.Test

class TroubleCodeParserTest {

    @Test
    fun `parses comma separated codes`() {
        val result = TroubleCodeParser.parse(listOf("P0300,P0420"))
        assertEquals(listOf("P0300", "P0420"), result)
    }

    @Test
    fun `filters out ok and no-data responses`() {
        val result = TroubleCodeParser.parse(listOf("OK", "NO DATA", "P0171"))
        assertEquals(listOf("P0171"), result)
    }

    @Test
    fun `deduplicates across responses and trims whitespace`() {
        val result = TroubleCodeParser.parse(listOf("P0300, P0420", " p0300 "))
        assertEquals(listOf("P0300", "P0420"), result)
    }

    @Test
    fun `returns empty list when car has no faults`() {
        val result = TroubleCodeParser.parse(listOf("OK", "NO DATA", ""))
        assertEquals(emptyList<String>(), result)
    }
}
