package com.geoevent.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import org.json.JSONObject

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "geoevent_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_PHONE_NUMBER = "phone_number"
    }

    /**
     * Decode JWT token and extract user ID from payload
     */
    private fun decodeJwtUserId(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            // Decode the payload (second part)
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
            val jsonObject = JSONObject(payload)

            // Extract user ID (try common field names)
            when {
                jsonObject.has("userId") -> jsonObject.getString("userId")
                jsonObject.has("user_id") -> jsonObject.getString("user_id")
                jsonObject.has("sub") -> jsonObject.getString("sub")
                jsonObject.has("id") -> jsonObject.getString("id")
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()

        // Extract and save user ID from JWT token
        decodeJwtUserId(token)?.let { userId ->
            prefs.edit().putString(KEY_USER_ID, userId).apply()
        }
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun saveUserInfo(name: String, phoneNumber: String) {
        prefs.edit().apply {
            putString(KEY_USER_NAME, name)
            putString(KEY_PHONE_NUMBER, phoneNumber)
            apply()
        }
    }

    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    fun getPhoneNumber(): String? {
        return prefs.getString(KEY_PHONE_NUMBER, null)
    }

    fun isLoggedIn(): Boolean {
        return getAuthToken() != null
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
