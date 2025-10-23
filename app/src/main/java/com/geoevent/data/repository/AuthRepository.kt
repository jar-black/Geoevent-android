package com.geoevent.data.repository

import com.geoevent.data.api.AuthService
import com.geoevent.data.model.AuthRequest
import com.geoevent.data.model.AuthResponse
import com.geoevent.data.model.User
import retrofit2.Response

class AuthRepository(private val authService: AuthService) {

    suspend fun login(phoneNumber: String, password: String): Response<AuthResponse> {
        return authService.login(AuthRequest(phoneNumber, password))
    }

    suspend fun register(user: User): Response<Unit> {
        return authService.register(user)
    }
}
