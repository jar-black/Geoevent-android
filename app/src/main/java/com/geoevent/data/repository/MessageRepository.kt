package com.geoevent.data.repository

import com.geoevent.data.api.MessageService
import com.geoevent.data.model.ChatMessage
import retrofit2.Response

class MessageRepository(private val messageService: MessageService) {

    suspend fun getMessages(eventId: String): Response<List<ChatMessage>> {
        return messageService.getMessages(eventId)
    }

    suspend fun createMessage(message: ChatMessage): Response<Unit> {
        return messageService.createMessage(message)
    }

    suspend fun updateMessage(message: ChatMessage): Response<Unit> {
        return messageService.updateMessage(message)
    }

    suspend fun deleteMessage(messageId: String): Response<Unit> {
        return messageService.deleteMessage(messageId)
    }
}
