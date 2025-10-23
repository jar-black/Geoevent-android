package com.geoevent.ui.dashboard

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.geoevent.data.api.RetrofitClient
import com.geoevent.data.auth.SessionManager
import com.geoevent.data.model.GeoStamp
import com.geoevent.data.repository.GeoStampRepository
import kotlinx.coroutines.launch
import java.util.UUID

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val geoStampRepository = GeoStampRepository(
        RetrofitClient.getGeoStampService(sessionManager)
    )

    private val _currentLocation = MutableLiveData<Location?>()
    val currentLocation: LiveData<Location?> = _currentLocation

    private val _geoStamps = MutableLiveData<List<GeoStamp>>()
    val geoStamps: LiveData<List<GeoStamp>> = _geoStamps

    private val _createStampState = MutableLiveData<StampState>()
    val createStampState: LiveData<StampState> = _createStampState

    private val _loadStampsState = MutableLiveData<StampState>()
    val loadStampsState: LiveData<StampState> = _loadStampsState

    fun updateLocation(location: Location) {
        _currentLocation.value = location
    }

    fun createGeoStamp() {
        val location = _currentLocation.value
        if (location == null) {
            _createStampState.value = StampState.Error("Location not available")
            return
        }

        val userId = sessionManager.getUserId()
        if (userId == null) {
            _createStampState.value = StampState.Error("User not logged in")
            return
        }

        viewModelScope.launch {
            try {
                _createStampState.value = StampState.Loading

                val geoStamp = GeoStamp(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    geoEventId = null,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    timestamp = System.currentTimeMillis()
                )

                val response = geoStampRepository.createGeoStamp(geoStamp)

                if (response.isSuccessful) {
                    _createStampState.value = StampState.Success("GeoStamp created successfully")
                    loadGeoStamps() // Reload the list
                } else {
                    _createStampState.value = StampState.Error("Failed to create GeoStamp: ${response.code()}")
                }
            } catch (e: Exception) {
                _createStampState.value = StampState.Error("Error: ${e.message}")
            }
        }
    }

    fun loadGeoStamps() {
        viewModelScope.launch {
            try {
                _loadStampsState.value = StampState.Loading

                val response = geoStampRepository.getGeoStamps()

                if (response.isSuccessful && response.body() != null) {
                    _geoStamps.value = response.body()!!
                    _loadStampsState.value = StampState.Success("Loaded ${response.body()!!.size} geostamps")
                } else {
                    _loadStampsState.value = StampState.Error("Failed to load geostamps: ${response.code()}")
                }
            } catch (e: Exception) {
                _loadStampsState.value = StampState.Error("Error: ${e.message}")
            }
        }
    }

    sealed class StampState {
        object Idle : StampState()
        object Loading : StampState()
        data class Success(val message: String) : StampState()
        data class Error(val message: String) : StampState()
    }
}