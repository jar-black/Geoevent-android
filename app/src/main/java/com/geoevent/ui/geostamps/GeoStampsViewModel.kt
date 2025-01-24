package com.geoevent.ui.geostamps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GeoStampsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "[GEO STAMPS]"
    }
    val text: LiveData<String> = _text
}