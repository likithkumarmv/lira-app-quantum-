package com.lira.field.ui.checkpoint

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lira.field.core.model.CheckpointStatus
import com.lira.field.core.network.LiraNetworkResult
import com.lira.field.data.LiraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CheckpointViewModel(
    private val repository: LiraRepository,
    val assignmentId: String
) : ViewModel() {

    private val _status = MutableStateFlow<CheckpointStatus?>(null)
    val status: StateFlow<CheckpointStatus?> = _status.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _custodyVerificationResult = MutableStateFlow<String?>(null)
    val custodyVerificationResult: StateFlow<String?> = _custodyVerificationResult.asStateFlow()

    private val _isVerifyingCustody = MutableStateFlow(false)
    val isVerifyingCustody: StateFlow<Boolean> = _isVerifyingCustody.asStateFlow()

    init {
        refreshStatus()
    }

    fun refreshStatus() {
        viewModelScope.launch {
            _isLoading.value = true
            _status.value = repository.getCheckpointStatus(assignmentId)
            _isLoading.value = false
        }
    }

    fun verifyQrCustody(qrPayload: String) {
        viewModelScope.launch {
            _isVerifyingCustody.value = true
            when (val res = repository.verifyQrCustody(assignmentId, qrPayload)) {
                is LiraNetworkResult.Success -> {
                    _custodyVerificationResult.value = "VERIFIED: ${res.data.chainOfCustodyStatus} • HMAC-8: ${res.data.hmac8} • Ratchet #${res.data.ratchet} (${res.data.verifiedAt})"
                }
                is LiraNetworkResult.Error -> {
                    _custodyVerificationResult.value = "FAILED: ${res.message}"
                }
                is LiraNetworkResult.OfflineQueued -> {
                    _custodyVerificationResult.value = "OFFLINE QUEUED"
                }
            }
            _isVerifyingCustody.value = false
        }
    }

    fun clearCustodyResult() {
        _custodyVerificationResult.value = null
    }
}

