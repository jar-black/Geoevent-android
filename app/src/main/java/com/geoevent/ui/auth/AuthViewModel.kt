package com.geoevent.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.geoevent.data.api.RetrofitClient
import com.geoevent.data.auth.SessionManager
import com.geoevent.data.model.User
import com.geoevent.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val authRepository = AuthRepository(
        RetrofitClient.getAuthService(sessionManager)
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
                    sessionManager.saveAuthToken(token)
                    _loginState.value = AuthState.Success
                } else {
                    _loginState.value = AuthState.Error("Login failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _loginState.value = AuthState.Error("Login error: ${e.message}")
            }
        }
    }

    fun register(userId: String, phoneNumber: String, name: String, password: String) {
        viewModelScope.launch {
            try {
                _registerState.value = AuthState.Loading

                // Create user
                val user = User(id = userId, phoneNumber = phoneNumber, name = name)
                val registerResponse = authRepository.register(user)

                if (registerResponse.isSuccessful) {
                    // Auto-login after registration
                    val loginResponse = authRepository.login(phoneNumber, password)

                    if (loginResponse.isSuccessful && loginResponse.body() != null) {
                        val token = loginResponse.body()!!.token
                        sessionManager.saveAuthToken(token)
                        sessionManager.saveUserInfo(userId, name, phoneNumber)
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
