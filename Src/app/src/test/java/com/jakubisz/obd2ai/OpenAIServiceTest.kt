package com.jakubisz.obd2ai

import com.jakubisz.obd2ai.helpers.OpenAIService
import com.jakubisz.obd2ai.model.ErrorSeverity
import org.junit.Assert.assertEquals
import org.junit.Test

class OpenAIServiceTest {

    @Test
    fun `parseErrorInfo parses valid response`() {
        val json = """
            {
              "errorCode": "P0420",
              "severity": 2,
              "title": "Catalyst System Efficiency Below Threshold",
              "detail": "Catalyst system is not efficient.",
              "implications": "Increased emissions, reduced fuel efficiency.",
              "suggestedActions": ["Inspect catalytic converter", "Check oxygen sensors"]
            }
        """.trimIndent()

        val result = OpenAIService.parseErrorInfo(json)

        assertEquals("P0420", result.errorCode)
        assertEquals(ErrorSeverity.HIGH, result.severity)
        assertEquals("Catalyst System Efficiency Below Threshold", result.title)
        assertEquals(2, result.suggestedActions.size)
    }

    @Test
    fun `parseErrorInfo returns fallback on malformed json`() {
        val result = OpenAIService.parseErrorInfo("not a json at all")

        assertEquals("Error", result.errorCode)
        assertEquals(ErrorSeverity.LOW, result.severity)
    }

    @Test
    fun `severity maps correctly from int`() {
        assertEquals(ErrorSeverity.LOW, ErrorSeverity.fromInt(0))
        assertEquals(ErrorSeverity.MEDIUM, ErrorSeverity.fromInt(1))
        assertEquals(ErrorSeverity.HIGH, ErrorSeverity.fromInt(2))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `severity rejects unknown int`() {
        ErrorSeverity.fromInt(5)
    }
}
