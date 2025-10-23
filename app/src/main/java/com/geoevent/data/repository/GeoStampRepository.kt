package com.geoevent.data.repository

import com.geoevent.data.api.GeoStampService
import com.geoevent.data.model.GeoStamp
import retrofit2.Response

class GeoStampRepository(private val geoStampService: GeoStampService) {

    suspend fun getGeoStamps(): Response<List<GeoStamp>> {
        return geoStampService.getGeoStamps()
    }

    suspend fun getGeoStamp(stampId: String): Response<GeoStamp> {
        return geoStampService.getGeoStamp(stampId)
    }

    suspend fun createGeoStamp(geoStamp: GeoStamp): Response<Unit> {
        return geoStampService.createGeoStamp(geoStamp)
    }

    suspend fun updateGeoStamp(stampId: String, geoStamp: GeoStamp): Response<Unit> {
        return geoStampService.updateGeoStamp(stampId, geoStamp)
    }

    suspend fun deleteGeoStamp(stampId: String): Response<Unit> {
        return geoStampService.deleteGeoStamp(stampId)
    }
}
