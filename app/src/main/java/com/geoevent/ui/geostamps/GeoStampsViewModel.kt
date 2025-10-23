package com.geoevent.ui.geostamps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.geoevent.data.api.RetrofitClient
import com.geoevent.data.auth.SessionManager
import com.geoevent.data.model.GeoStamp
import com.geoevent.data.repository.GeoStampRepository
import kotlinx.coroutines.launch

class GeoStampsViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val geoStampRepository = GeoStampRepository(
        RetrofitClient.getGeoStampService(sessionManager)
    )

    private val _geoStamps = MutableLiveData<List<GeoStamp>>()
    val geoStamps: LiveData<List<GeoStamp>> = _geoStamps

    private val _operationState = MutableLiveData<OperationState>()
    val operationState: LiveData<OperationState> = _operationState

    init {
        loadGeoStamps()
    }

    fun loadGeoStamps() {
        viewModelScope.launch {
            try {
                _operationState.value = OperationState.Loading

                val response = geoStampRepository.getGeoStamps()

                if (response.isSuccessful && response.body() != null) {
                    _geoStamps.value = response.body()!!
                    _operationState.value = OperationState.Success("Loaded ${response.body()!!.size} geostamps")
                } else {
                    _operationState.value = OperationState.Error("Failed to load geostamps: ${response.code()}")
                }
            } catch (e: Exception) {
                _operationState.value = OperationState.Error("Error: ${e.message}")
            }
        }
    }

    fun deleteGeoStamp(stampId: String) {
        viewModelScope.launch {
            try {
                _operationState.value = OperationState.Loading

                val response = geoStampRepository.deleteGeoStamp(stampId)

                if (response.isSuccessful) {
                    _operationState.value = OperationState.Success("GeoStamp deleted")
                    loadGeoStamps() // Reload list
                } else {
                    _operationState.value = OperationState.Error("Failed to delete: ${response.code()}")
                }
            } catch (e: Exception) {
                _operationState.value = OperationState.Error("Error: ${e.message}")
            }
        }
    }

    fun linkToEvent(stampId: String, eventId: String) {
        viewModelScope.launch {
            try {
                _operationState.value = OperationState.Loading

                // Get the current stamp
                val currentStamp = _geoStamps.value?.find { it.id == stampId }
                if (currentStamp == null) {
                    _operationState.value = OperationState.Error("Stamp not found")
                    return@launch
                }

                // Update with event ID
                val updatedStamp = currentStamp.copy(geoEventId = eventId)
                val response = geoStampRepository.updateGeoStamp(stampId, updatedStamp)

                if (response.isSuccessful) {
                    _operationState.value = OperationState.Success("Linked to event")
                    loadGeoStamps() // Reload list
                } else {
                    _operationState.value = OperationState.Error("Failed to link: ${response.code()}")
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