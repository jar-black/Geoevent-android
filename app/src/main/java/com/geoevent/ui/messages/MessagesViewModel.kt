package com.geoevent.ui.messages

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.geoevent.data.api.RetrofitClient
import com.geoevent.data.auth.SessionManager
import com.geoevent.data.model.ChatMessage
import com.geoevent.data.model.GeoEvent
import com.geoevent.data.repository.GeoEventRepository
import com.geoevent.data.repository.MessageRepository
import kotlinx.coroutines.launch
import java.util.UUID

class MessagesViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val messageRepository = MessageRepository(
        RetrofitClient.getMessageService(sessionManager)
    )
    private val geoEventRepository = GeoEventRepository(
        RetrofitClient.getGeoEventService(sessionManager)
    )

    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _availableEvents = MutableLiveData<List<GeoEvent>>()
    val availableEvents: LiveData<List<GeoEvent>> = _availableEvents

    private val _selectedEvent = MutableLiveData<GeoEvent?>()
    val selectedEvent: LiveData<GeoEvent?> = _selectedEvent

    private val _operationState = MutableLiveData<OperationState>()
    val operationState: LiveData<OperationState> = _operationState

    init {
        loadAvailableEvents()
    }

    fun loadAvailableEvents() {
        viewModelScope.launch {
            try {
                val response = geoEventRepository.getGeoEvents()
                if (response.isSuccessful && response.body() != null) {
                    _availableEvents.value = response.body()!!
                }
            } catch (e: Exception) {
                // Silently fail
            }
        }
    }

    fun selectEvent(event: GeoEvent) {
        _selectedEvent.value = event
        loadMessages(event.id)
    }

    fun loadMessages(eventId: String) {
        viewModelScope.launch {
            try {
                val response = messageRepository.getMessages(eventId)

                if (response.isSuccessful && response.body() != null) {
                    _messages.value = response.body()!!.sortedBy { it.timestamp }
                } else {
                    _messages.value = emptyList()
                }
            } catch (e: Exception) {
                _messages.value = emptyList()
            }
        }
    }

    fun sendMessage(content: String) {
        val event = _selectedEvent.value
        if (event == null) {
            _operationState.value = OperationState.Error("No event selected")
            return
        }

        val userId = sessionManager.getUserId()
        if (userId == null) {
            _operationState.value = OperationState.Error("User not logged in")
            return
        }

        viewModelScope.launch {
            try {
                _operationState.value = OperationState.Loading

                val message = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    eventId = event.id,
                    content = content,
                    userId = userId,
                    timestamp = System.currentTimeMillis()
                )

                val response = messageRepository.createMessage(message)

                if (response.isSuccessful) {
                    _operationState.value = OperationState.Success("Message sent")
                    loadMessages(event.id) // Reload messages
                } else {
                    _operationState.value = OperationState.Error("Failed to send: ${response.code()}")
                }
            } catch (e: Exception) {
                _operationState.value = OperationState.Error("Error: ${e.message}")
            }
        }
    }

    fun refreshMessages() {
        val event = _selectedEvent.value
        if (event != null) {
            loadMessages(event.id)
        }
    }

    sealed class OperationState {
        object Idle : OperationState()
        object Loading : OperationState()
        data class Success(val message: String) : OperationState()
        data class Error(val message: String) : OperationState()
    }
}