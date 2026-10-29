package com.lira.field.ui.disruptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lira.field.core.model.Disruption
import com.lira.field.data.LiraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RiskAlertsViewModel(
    private val repository: LiraRepository
) : ViewModel() {

    private val _disruptions = MutableStateFlow<List<Disruption>>(emptyList())
    val disruptions: StateFlow<List<Disruption>> = _disruptions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _disruptions.value = repository.getDisruptions()
            _isLoading.value = false
        }
    }
}
