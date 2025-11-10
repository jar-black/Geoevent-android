package com.geoevent.data.api

import com.geoevent.data.model.AuthRequest
import com.geoevent.data.model.AuthResponse
import com.geoevent.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("/auth")
    suspend fun login(@Body authRequest: AuthRequest): Response<AuthResponse>

    @POST("/users")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<Unit>
}
