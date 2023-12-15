package com.jakubisz.obd2ai

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.github.eltonvs.obd.connection.ObdDeviceConnection
import kotlin.time.Duration.Companion.seconds

class OpenAIService {
    private lateinit var openAI: OpenAI

    init {
        openAI = OpenAI(
            token = BuildConfig.OPENAI_API_KEY,
            timeout = Timeout(socket = 60.seconds)
        )
    }

    suspend fun getResponse(query: String): String {
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = "You are a expert mechanic. Your goal is to provide assessment of the following OBD2 error codes" +
                            "Explain what they mean and where is problem. For each code list the code short name what it means and what is the problem. " +
                            "Explain in -list what could cause this, how serious is it if you can continue driving etc. and possible solutions. \n"
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
   /* suspend fun askOpenAI(question: String): String {
        // Implement network call to OpenAI API here
        // Return the response
    }*/
}