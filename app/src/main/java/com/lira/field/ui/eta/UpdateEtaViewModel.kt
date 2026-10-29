package com.lira.field.ui.eta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lira.field.core.network.LiraNetworkResult
import com.lira.field.core.session.SessionRepository
import com.lira.field.data.LiraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class UpdateEtaViewModel(
    private val repository: LiraRepository,
    private val sessionRepo: SessionRepository
) : ViewModel() {

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("Asia/Kolkata")
    }

    private val _selectedMinutesOffset = MutableStateFlow(30)
    val selectedMinutesOffset: StateFlow<Int> = _selectedMinutesOffset.asStateFlow()

    private val _customMinutes = MutableStateFlow("")
    val customMinutes: StateFlow<String> = _customMinutes.asStateFlow()

    private val _comment = MutableStateFlow("")
    val comment: StateFlow<String> = _comment.asStateFlow()

    private val _selectedReason = MutableStateFlow("TRAFFIC")
    val selectedReason: StateFlow<String> = _selectedReason.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _resultMessage = MutableStateFlow<String?>(null)
    val resultMessage: StateFlow<String?> = _resultMessage.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    private val _recoveryTriggered = MutableStateFlow(false)
    val recoveryTriggered: StateFlow<Boolean> = _recoveryTriggered.asStateFlow()

    private val _isPlanValid = MutableStateFlow(true)
    val isPlanValid: StateFlow<Boolean> = _isPlanValid.asStateFlow()

    val truckId: String = sessionRepo.session.value?.truckId ?: "T07"

    fun setOffset(minutes: Int) {
        _selectedMinutesOffset.value = minutes
        _customMinutes.value = ""
    }

    fun setCustomMinutes(mins: String) {
        _customMinutes.value = mins
        val parsed = mins.toIntOrNull()
        if (parsed != null && parsed > 0) {
            _selectedMinutesOffset.value = parsed
        }
    }

    fun setComment(text: String) {
        _comment.value = text
    }

    fun setReason(reason: String) {
        _selectedReason.value = reason
    }

    fun calculateEtaIso(): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
        val mins = _customMinutes.value.toIntOrNull() ?: _selectedMinutesOffset.value
        cal.add(Calendar.MINUTE, mins)
        return isoFormat.format(cal.time)
    }

    fun submitEta() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _resultMessage.value = null
            _recoveryTriggered.value = false

            val etaIso = calculateEtaIso()
            val commentText = _comment.value.trim().ifEmpty { null }

            when (val res = repository.reportEta(etaIso, _selectedReason.value, commentText)) {
                is LiraNetworkResult.Success -> {
                    _isLoading.value = false
                    _isSuccess.value = true
                    val planValid = res.data.planValid
                    _isPlanValid.value = planValid
                    val recStatus = res.data.recoveryStatus ?: "NORMAL"

                    if (!planValid || recStatus == "QUEUED" || recStatus == "TRIGGERED") {
                        _recoveryTriggered.value = true
                        _resultMessage.value = "ETA recorded. NOTE: Central recovery wave triggered due to slot conflict."
                    } else {
                        _resultMessage.value = "ETA updated successfully. Current plan remains valid."
                    }

                    // Refresh active assignment to pull revised recovery plan if created
                    repository.getAssignments(truckId)
                }
                is LiraNetworkResult.OfflineQueued -> {
                    _isLoading.value = false
                    _isSuccess.value = true
                    _resultMessage.value = res.message
                }
                is LiraNetworkResult.Error -> {
                    _isLoading.value = false
                    _resultMessage.value = "Failed: ${res.message}"
                }
            }
        }
    }

    fun clearMessage() {
        _resultMessage.value = null
    }
}
