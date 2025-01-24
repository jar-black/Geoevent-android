package com.geoevent.ui.geoevents

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GeoEventsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "[GEO EVENTS]"
    }
    val text: LiveData<String> = _text
}