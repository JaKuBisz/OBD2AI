package com.jakubisz.obd2ai.data.ai

import android.content.Context
import com.jakubisz.obd2ai.model.DtpCodeDTO
import com.jakubisz.obd2ai.model.ErrorSeverity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/** Offline DTC database bundled in assets - works without network or API key. */
@Singleton
class DtcInfoProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @Serializable
    private data class DtcEntry(
        val code: String,
        val severity: Int,
        val title: String,
        val detail: String,
        val implications: String,
        val suggestedActions: List<String> = emptyList()
    )

    private val json = Json { ignoreUnknownKeys = true }

    private val entries: Map<String, DtpCodeDTO> by lazy {
        runCatching {
            val text = context.assets.open("dtc_codes.json").bufferedReader().use { it.readText() }
            json.decodeFromString<List<DtcEntry>>(text).associate { entry ->
                entry.code to DtpCodeDTO(
                    errorCode = entry.code,
                    severity = ErrorSeverity.fromInt(entry.severity),
                    title = entry.title,
                    detail = entry.detail,
                    implications = entry.implications,
                    suggestedActions = entry.suggestedActions
                )
            }
        }.getOrDefault(emptyMap())
    }

    fun lookup(code: String): DtpCodeDTO? = entries[code.trim().uppercase()]
}
