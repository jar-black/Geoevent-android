package com.geoevent.data.api

import com.geoevent.data.model.GeoEvent
import retrofit2.Response
import retrofit2.http.*

interface GeoEventService {
    @GET("/geoevents")
    suspend fun getGeoEvents(): Response<List<GeoEvent>>

    @GET("/geoevents/{id}")
    suspend fun getGeoEvent(@Path("id") eventId: String): Response<GeoEvent>

    @POST("/geoevents")
    suspend fun createGeoEvent(@Body geoEvent: GeoEvent): Response<Unit>

    @DELETE("/geoevents/{id}")
    suspend fun deleteGeoEvent(@Path("id") eventId: String): Response<Unit>
}
