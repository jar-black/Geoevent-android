package com.geoevent.data.api

import com.geoevent.data.auth.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://192.168.1.175:8081"

    private var retrofit: Retrofit? = null

    fun getInstance(sessionManager: SessionManager): Retrofit {
        if (retrofit == null) {
            // Logging interceptor for debugging
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            // Auth interceptor for adding JWT token
            val authInterceptor = AuthInterceptor(sessionManager)

            // OkHttp client
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            // Retrofit instance
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    fun getAuthService(sessionManager: SessionManager): AuthService {
        return getInstance(sessionManager).create(AuthService::class.java)
    }

    fun getUserService(sessionManager: SessionManager): UserService {
        return getInstance(sessionManager).create(UserService::class.java)
    }

    fun getGeoEventService(sessionManager: SessionManager): GeoEventService {
        return getInstance(sessionManager).create(GeoEventService::class.java)
    }

    fun getGeoStampService(sessionManager: SessionManager): GeoStampService {
        return getInstance(sessionManager).create(GeoStampService::class.java)
    }

    fun getMessageService(sessionManager: SessionManager): MessageService {
        return getInstance(sessionManager).create(MessageService::class.java)
    }
}
