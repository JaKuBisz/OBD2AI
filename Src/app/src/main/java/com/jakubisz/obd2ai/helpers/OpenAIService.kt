package com.jakubisz.obd2ai.helpers

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.jakubisz.obd2ai.BuildConfig
import com.jakubisz.obd2ai.model.DtpCodeDTO
import com.jakubisz.obd2ai.model.ErrorSeverity
import org.json.JSONObject
import kotlin.time.Duration.Companion.seconds

class OpenAIService {
    private lateinit var openAI: OpenAI

    init {
        openAI = OpenAI(
            token = BuildConfig.OPENAI_API_KEY,
            timeout = Timeout(socket = 60.seconds)
        )
    }

    suspend fun getDtpCodeAssessment(dtpCode: String): DtpCodeDTO {
        val response = getResponse(dtpCode)
        return parseErrorInfo(response)
    }

    suspend fun getResponse(query: String): String {
        //TODO: Issue #2 - implement assistant https://github.com/JaKuBisz/OBD2AI/issues/2 - https://github.com/aallam/openai-kotlin/blob/main/guides/Assistants.md
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = "\"Given a plain error code as input, provide a resolution in a structured JSON format. The JSON should include the following fields:\n" +
                            "\n" +
                            "1. errorCode: The provided error code.\n" +
                            "2. severity: The severity level of the error (0 for Low, 1 for Medium, 2 for High) - defines how big implication this has on the driving and safety of vehicle.\n" +
                            "3. title: A short, descriptive title of the error, try limit length to 32chars max is 60 chars.\n" +
                            "4. detail: A detailed explanation of what the error is. - ideal length around 300 chars\n" +
                            "5. implications: Information on the implications of the error, including any potential safety concerns, urgency for repair or usage complications/limitations. - ideal length around 300 chars\n" +
                            "6. suggestedActions: Recommended actions or steps to resolve or investigate the error further. - ideally 5 or more suggestions\n" +
                            "\n" +
                            "Example Input:\n" +
                            "'P0420'\n" +
                            "\n" +
                            "Expected JSON Output:\n" +
                            "{\n" +
                            "    \"errorCode\": \"P0420\",\n" +
                            "    \"severity\": 2,\n" +
                            "    \"title\": \"Catalyst System Efficiency Below Threshold\",\n" +
                            "    \"detail\": \"Indicates that the oxygen levels in the exhaust are not as expected, suggesting inefficiency in the catalyst system, possibly due to a malfunctioning catalytic converter or faulty sensors.\",\n" +
                            "    \"implications\": \"A compromised catalyst system can increase harmful emissions, reduce fuel efficiency, and potentially lead to more significant engine problems if not addressed promptly.\",\n" +
                            "    \"suggestedActions\": [\n" +
                            "        \"Inspect and, if necessary, replace the catalytic converter\",\n" +
                            "        \"Check and replace faulty oxygen sensors\",\n" +
                            "        \"Examine the exhaust system for leaks or damage\",\n" +
                            "        \"Verify the operation of the engine management system\",\n" +
                            "        \"Check for software updates for the engine control unit (ECU)\",\n" +
                            "        \"Ensure proper fuel quality and engine tuning\"\n" +
                            "    ]\n" +
                            "}\"\n" +
                            "Additional instructions:\n" +
                            "\"You are an expert mechanic. Your goal is to provide an assessment of the following OBD2 error codes. Explain what they mean and where the problem is. \""
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = query
                )
            )
        )

        val completion: ChatCompletion = openAI.chatCompletion(chatCompletionRequest)
        val result = completion.choices.first().message.content ?: "No result"
        return result
    }

    fun parseErrorInfo(jsonString: String): DtpCodeDTO {
        try {
            val jsonObject = JSONObject(jsonString)

            val errorCode = jsonObject.getString("errorCode")
            val severity = ErrorSeverity.fromInt(jsonObject.getInt("severity"))
            val title = jsonObject.getString("title")
            val detail = jsonObject.getString("detail")
            val implications = jsonObject.getString("implications")
            val actionsArray = jsonObject.getJSONArray("suggestedActions")
            val suggestedActions = mutableListOf<String>()
            for (i in 0 until actionsArray.length()) {
                suggestedActions.add(actionsArray.getString(i))
            }
            return DtpCodeDTO(errorCode, severity, title, detail, implications, suggestedActions)

        } catch (e: Exception) {
            return DtpCodeDTO("Error", ErrorSeverity.LOW, "Error", "Error", "Error", listOf("Error"))
        }

    }
}