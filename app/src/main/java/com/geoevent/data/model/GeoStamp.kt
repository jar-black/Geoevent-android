package com.geoevent.data.model

data class GeoStamp(
    val id: String,
    val userId: String,
    val geoEventId: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timestamp: Long = System.currentTimeMillis()
)
