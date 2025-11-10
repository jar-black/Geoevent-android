package com.geoevent.data.api

import com.geoevent.data.auth.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBuilder = request.newBuilder()

        // Skip auth for /auth and /users POST endpoints (as per OpenAPI spec security: [])
        val path = request.url.encodedPath
        val method = request.method
        val skipAuth = (path == "/auth" && method == "POST") ||
                       (path == "/users" && method == "POST")

        // Add Authorization header if token exists and endpoint requires auth
        if (!skipAuth) {
            sessionManager.getAuthToken()?.let { token ->
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
        }

        return chain.proceed(requestBuilder.build())
    }
}
