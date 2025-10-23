package com.geoevent.data.model

data class ChatMessage(
    val id: String,
    val eventId: String,
    val content: String,
    val userId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
