package com.geoevent.data.api

import com.geoevent.data.model.ChatMessage
import retrofit2.Response
import retrofit2.http.*

interface MessageService {
    @GET("/msg")
    suspend fun getMessages(@Query("eventId") eventId: String): Response<List<ChatMessage>>

    @POST("/msg")
    suspend fun createMessage(@Body message: ChatMessage): Response<Unit>

    @PUT("/msg")
    suspend fun updateMessage(@Body message: ChatMessage): Response<Unit>

    @DELETE("/msg/{id}")
    suspend fun deleteMessage(@Path("id") messageId: String): Response<Unit>
}
