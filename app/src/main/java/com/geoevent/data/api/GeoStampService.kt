package com.geoevent.data.api

import com.geoevent.data.model.GeoStamp
import retrofit2.Response
import retrofit2.http.*

interface GeoStampService {
    @GET("/geostamps")
    suspend fun getGeoStamps(): Response<List<GeoStamp>>

    @GET("/geostamps/{id}")
    suspend fun getGeoStamp(@Path("id") stampId: String): Response<GeoStamp>

    @POST("/geostamps")
    suspend fun createGeoStamp(@Body geoStamp: GeoStamp): Response<Unit>

    @PUT("/geostamps/{id}")
    suspend fun updateGeoStamp(@Path("id") stampId: String, @Body geoStamp: GeoStamp): Response<Unit>

    @DELETE("/geostamps/{id}")
    suspend fun deleteGeoStamp(@Path("id") stampId: String): Response<Unit>
}
