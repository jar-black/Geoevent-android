package com.geoevent.data.repository

import com.geoevent.data.api.UserService
import com.geoevent.data.model.User
import retrofit2.Response

class UserRepository(private val userService: UserService) {

    suspend fun getUser(userId: String): Response<User> {
        return userService.getUser(userId)
    }

    suspend fun updateUser(userId: String, user: User): Response<Unit> {
        return userService.updateUser(userId, user)
    }

    suspend fun deleteUser(userId: String): Response<Unit> {
        return userService.deleteUser(userId)
    }
}
