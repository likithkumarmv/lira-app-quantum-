package com.lira.field.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lira.field.core.model.DriverEvent
import com.lira.field.data.LiraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ActivityTimelineViewModel(
    private val repository: LiraRepository
) : ViewModel() {

    private val _events = MutableStateFlow<List<DriverEvent>>(emptyList())
    val events: StateFlow<List<DriverEvent>> = _events.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            _events.value = repository.getEvents()
            _isLoading.value = false
        }
    }
}

