package com.geoevent.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.geoevent.data.api.RetrofitClient
import com.geoevent.data.auth.SessionManager
import com.geoevent.data.repository.AuthRepository
import com.geoevent.data.repository.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val authRepository = AuthRepository(
        RetrofitClient.getAuthService(sessionManager)
    )
    private val userRepository = UserRepository(
        RetrofitClient.getUserService(sessionManager)
    )

    private val _loginState = MutableLiveData<AuthState>()
    val loginState: LiveData<AuthState> = _loginState

    private val _registerState = MutableLiveData<AuthState>()
    val registerState: LiveData<AuthState> = _registerState

    fun login(phoneNumber: String, password: String) {
        viewModelScope.launch {
            try {
                _loginState.value = AuthState.Loading

                val response = authRepository.login(phoneNumber, password)

                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.token
                    // Save token (this automatically extracts and saves user ID from JWT)
                    sessionManager.saveAuthToken(token)

                    // Fetch user details to save name and phone number
                    val userId = sessionManager.getUserId()
                    if (userId != null) {
                        val userResponse = userRepository.getUser(userId)
                        if (userResponse.isSuccessful && userResponse.body() != null) {
                            val user = userResponse.body()!!
                            sessionManager.saveUserInfo(user.name, user.phoneNumber)
                        }
                    }

                    _loginState.value = AuthState.Success
                } else {
                    _loginState.value = AuthState.Error("Login failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _loginState.value = AuthState.Error("Login error: ${e.message}")
            }
        }
    }

    fun register(name: String, phoneNumber: String, password: String) {
        viewModelScope.launch {
            try {
                _registerState.value = AuthState.Loading

                // Register user with new structure (name, phoneNumber, password)
                val registerResponse = authRepository.register(name, phoneNumber, password)

                if (registerResponse.isSuccessful) {
                    // Auto-login after registration
                    val loginResponse = authRepository.login(phoneNumber, password)

                    if (loginResponse.isSuccessful && loginResponse.body() != null) {
                        val token = loginResponse.body()!!.token
                        // Save token (this automatically extracts and saves user ID from JWT)
                        sessionManager.saveAuthToken(token)
                        // Save additional user info
                        sessionManager.saveUserInfo(name, phoneNumber)
                        _registerState.value = AuthState.Success
                    } else {
                        _registerState.value = AuthState.Error("Registration successful but login failed")
                    }
                } else {
                    _registerState.value = AuthState.Error("Registration failed: ${registerResponse.code()}")
                }
            } catch (e: Exception) {
                _registerState.value = AuthState.Error("Registration error: ${e.message}")
            }
        }
    }

    fun isLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object Success : AuthState()
        data class Error(val message: String) : AuthState()
    }
}
