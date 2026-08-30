package com.jakubisz.obd2ai.data.obd

/** Pure parsing helpers for OBD responses, kept separate for testability. */
object TroubleCodeParser {

    /**
     * Merges raw responses from current/pending/permanent trouble code commands
     * into a clean, deduplicated list of codes.
     */
    fun parse(rawResponses: List<String>): List<String> =
        rawResponses.flatMap { it.split(",") }
            .map { it.trim().uppercase() }
            .filter { it.isNotEmpty() && it != "OK" && it != "NO DATA" }
            .distinct()
}
