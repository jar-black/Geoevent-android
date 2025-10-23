package com.geoevent.data.repository

import com.geoevent.data.api.GeoEventService
import com.geoevent.data.model.GeoEvent
import retrofit2.Response

class GeoEventRepository(private val geoEventService: GeoEventService) {

    suspend fun getGeoEvents(): Response<List<GeoEvent>> {
        return geoEventService.getGeoEvents()
    }

    suspend fun getGeoEvent(eventId: String): Response<GeoEvent> {
        return geoEventService.getGeoEvent(eventId)
    }

    suspend fun createGeoEvent(geoEvent: GeoEvent): Response<Unit> {
        return geoEventService.createGeoEvent(geoEvent)
    }

    suspend fun deleteGeoEvent(eventId: String): Response<Unit> {
        return geoEventService.deleteGeoEvent(eventId)
    }
}
