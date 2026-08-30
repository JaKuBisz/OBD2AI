package com.jakubisz.obd2ai.data.ai

import com.jakubisz.obd2ai.BuildConfig
import com.jakubisz.obd2ai.model.DtpCodeDTO
import com.jakubisz.obd2ai.model.ErrorSeverity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepository @Inject constructor(
    private val openAI: OpenAIService,
    private val offlineInfo: DtcInfoProvider
) {
    val isAiAvailable: Boolean
        get() = BuildConfig.OPENAI_API_KEY.isNotBlank()

    /** Instant offline lookup, or null when the code is not in the bundled database. */
    fun offlineLookup(code: String): DtpCodeDTO? = offlineInfo.lookup(code)

    /**
     * Full assessment: tries the LLM first (when configured) and falls back to the
     * offline database, so the app stays useful without network or an API key.
     */
    suspend fun assess(code: String): DtpCodeDTO {
        if (isAiAvailable) {
            val aiResult = runCatching { openAI.getDtpCodeAssessment(code) }.getOrNull()
            if (aiResult != null && aiResult.errorCode != "Error") return aiResult
        }
        return offlineLookup(code) ?: DtpCodeDTO(
            errorCode = code,
            severity = ErrorSeverity.MEDIUM,
            title = code,
            detail = "No information available for this code.",
            implications = "Consult a mechanic or service documentation for details.",
            suggestedActions = listOf("Look up the code in your vehicle's service manual")
        )
    }
}
