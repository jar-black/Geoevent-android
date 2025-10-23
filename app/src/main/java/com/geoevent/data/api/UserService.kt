package com.geoevent.data.api

import com.geoevent.data.model.User
import retrofit2.Response
import retrofit2.http.*

interface UserService {
    @GET("/users/{id}")
    suspend fun getUser(@Path("id") userId: String): Response<User>

    @PUT("/users/{id}")
    suspend fun updateUser(@Path("id") userId: String, @Body user: User): Response<Unit>

    @DELETE("/users/{id}")
    suspend fun deleteUser(@Path("id") userId: String): Response<Unit>
}
