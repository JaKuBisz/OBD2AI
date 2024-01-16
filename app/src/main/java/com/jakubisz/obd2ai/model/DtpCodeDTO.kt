package com.jakubisz.obd2ai.model

data class DtpCodeDTO(
    val errorCode: String,
    val severity: ErrorSeverity,
    val title: String,
    val detail: String,
    val implications: String,
    val suggestedActions: List<String>
)
