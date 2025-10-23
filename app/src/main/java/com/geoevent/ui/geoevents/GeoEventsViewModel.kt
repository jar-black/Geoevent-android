package com.geoevent.ui.geoevents

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.geoevent.data.api.RetrofitClient
import com.geoevent.data.auth.SessionManager
import com.geoevent.data.model.GeoEvent
import com.geoevent.data.model.GeoStamp
import com.geoevent.data.repository.GeoEventRepository
import com.geoevent.data.repository.GeoStampRepository
import kotlinx.coroutines.launch
import java.util.UUID

class GeoEventsViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val geoEventRepository = GeoEventRepository(
        RetrofitClient.getGeoEventService(sessionManager)
    )
    private val geoStampRepository = GeoStampRepository(
        RetrofitClient.getGeoStampService(sessionManager)
    )

    private val _geoEvents = MutableLiveData<List<GeoEvent>>()
    val geoEvents: LiveData<List<GeoEvent>> = _geoEvents

    private val _availableStamps = MutableLiveData<List<GeoStamp>>()
    val availableStamps: LiveData<List<GeoStamp>> = _availableStamps

    private val _operationState = MutableLiveData<OperationState>()
    val operationState: LiveData<OperationState> = _operationState

    init {
        loadGeoEvents()
        loadAvailableStamps()
    }

    fun loadGeoEvents() {
        viewModelScope.launch {
            try {
                _operationState.value = OperationState.Loading

                val response = geoEventRepository.getGeoEvents()

                if (response.isSuccessful && response.body() != null) {
                    _geoEvents.value = response.body()!!
                    _operationState.value = OperationState.Success("Loaded ${response.body()!!.size} events")
                } else {
                    _operationState.value = OperationState.Error("Failed to load events: ${response.code()}")
                }
            } catch (e: Exception) {
                _operationState.value = OperationState.Error("Error: ${e.message}")
            }
        }
    }

    fun loadAvailableStamps() {
        viewModelScope.launch {
            try {
                val response = geoStampRepository.getGeoStamps()
                if (response.isSuccessful && response.body() != null) {
                    // Filter stamps that don't have an event linked
                    val unlinkedStamps = response.body()!!.filter { it.geoEventId == null }
                    _availableStamps.value = unlinkedStamps
                }
            } catch (e: Exception) {
                // Silently fail for available stamps load
            }
        }
    }

    fun createGeoEvent(stampId: String) {
        val userId = sessionManager.getUserId()
        if (userId == null) {
            _operationState.value = OperationState.Error("User not logged in")
            return
        }

        viewModelScope.launch {
            try {
                _operationState.value = OperationState.Loading

                val eventId = UUID.randomUUID().toString()
                val geoEvent = GeoEvent(id = eventId, userId = userId)

                // Create the event
                val createResponse = geoEventRepository.createGeoEvent(geoEvent)

                if (createResponse.isSuccessful) {
                    // Link the stamp to this event
                    val stamp = _availableStamps.value?.find { it.id == stampId }
                    if (stamp != null) {
                        val updatedStamp = stamp.copy(geoEventId = eventId)
                        geoStampRepository.updateGeoStamp(stampId, updatedStamp)
                    }

                    _operationState.value = OperationState.Success("GeoEvent created")
                    loadGeoEvents()
                    loadAvailableStamps()
                } else {
                    _operationState.value = OperationState.Error("Failed to create event: ${createResponse.code()}")
                }
            } catch (e: Exception) {
                _operationState.value = OperationState.Error("Error: ${e.message}")
            }
        }
    }

    fun deleteGeoEvent(eventId: String) {
        viewModelScope.launch {
            try {
                _operationState.value = OperationState.Loading

                val response = geoEventRepository.deleteGeoEvent(eventId)

                if (response.isSuccessful) {
                    _operationState.value = OperationState.Success("GeoEvent deleted")
                    loadGeoEvents()
                    loadAvailableStamps()
                } else {
                    _operationState.value = OperationState.Error("Failed to delete: ${response.code()}")
                }
            } catch (e: Exception) {
                _operationState.value = OperationState.Error("Error: ${e.message}")
            }
        }
    }

    sealed class OperationState {
        object Idle : OperationState()
        object Loading : OperationState()
        data class Success(val message: String) : OperationState()
        data class Error(val message: String) : OperationState()
    }
}