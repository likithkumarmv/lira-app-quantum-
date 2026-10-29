package com.lira.field.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lira.field.core.database.PendingAction
import com.lira.field.core.model.HealthResponse
import com.lira.field.core.network.LiraNetworkResult
import com.lira.field.core.session.SessionRepository
import com.lira.field.data.LiraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SettingsViewModel(
    private val repository: LiraRepository,
    private val sessionRepo: SessionRepository
) : ViewModel() {

    val baseUrl: StateFlow<String> = sessionRepo.baseUrl
    val mockMode: StateFlow<Boolean> = sessionRepo.mockModeEnabled
    val session = sessionRepo.session
    val lastSyncTime = repository.lastSyncTime
    val themeMode = sessionRepo.themeMode
    val language: StateFlow<String> = sessionRepo.language
    val useLabFixture: StateFlow<Boolean> = sessionRepo.useLabFixture

    val sseStatus = MutableStateFlow("CONNECTED (HTTP 200 SSE)")
    val lastStreamEventAt = MutableStateFlow("14:32:10")

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            sessionRepo.setLanguage(lang)
            repository.updateLanguage(lang)
        }
    }

    fun setUseLabFixture(enabled: Boolean) {
        sessionRepo.setUseLabFixture(enabled)
    }

    val supabaseUrl: StateFlow<String> = sessionRepo.supabaseUrl
    val supabaseKey: StateFlow<String> = sessionRepo.supabaseKey

    fun setThemeMode(mode: String) {
        sessionRepo.setThemeMode(mode)
    }

    fun updateSupabaseConfig(url: String, key: String) {
        sessionRepo.updateSupabaseConfig(url, key)
    }

    private val _pendingActions = MutableStateFlow<List<PendingAction>>(emptyList())
    val pendingActions: StateFlow<List<PendingAction>> = _pendingActions.asStateFlow()

    private val _healthTestResult = MutableStateFlow<String?>(null)
    val healthTestResult: StateFlow<String?> = _healthTestResult.asStateFlow()

    private val _isTestingHealth = MutableStateFlow(false)
    val isTestingHealth: StateFlow<Boolean> = _isTestingHealth.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        refreshPendingActions()
    }

    fun refreshPendingActions() {
        viewModelScope.launch {
            _pendingActions.value = repository.pendingActionDao.getAllPending()
        }
    }

    fun updateBaseUrl(newUrl: String) {
        sessionRepo.updateBaseUrl(newUrl)
    }

    fun setQuickLanIp(ip: String) {
        sessionRepo.setFastApiLanIp(ip)
    }

    fun toggleMockMode(enabled: Boolean) {
        sessionRepo.setMockMode(enabled)
    }

    fun testBackendConnection() {
        viewModelScope.launch {
            _isTestingHealth.value = true
            _healthTestResult.value = "PINGING /api/v1/driver/health..."
            val start = System.currentTimeMillis()
            when (val res = repository.checkDriverHealth()) {
                is LiraNetworkResult.Success -> {
                    val latency = System.currentTimeMillis() - start
                    val schema = res.data.schema ?: "unknown"
                    if (schema == "v13.0") {
                        _healthTestResult.value = "CONNECTED · schema v13.0 · ${latency} ms"
                    } else {
                        _healthTestResult.value = "WARNING · schema mismatch: $schema · ${latency} ms"
                    }
                }
                is LiraNetworkResult.Error -> {
                    val latency = System.currentTimeMillis() - start
                    _healthTestResult.value = "FAILED (${latency} ms): [${res.code}] ${res.message}"
                }
                is LiraNetworkResult.OfflineQueued -> {
                    _healthTestResult.value = "OFFLINE"
                }
            }
            _isTestingHealth.value = false
        }
    }

    fun forceSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            val count = repository.syncPendingActions()
            refreshPendingActions()
            _statusMessage.value = "Sync complete. Uploaded $count pending actions."
            _isSyncing.value = false
        }
    }

    fun purgeLocalData() {
        repository.purgeAllFakeAndCachedData()
        refreshPendingActions()
        _statusMessage.value = "All cached and local data purged successfully."
    }

    fun logout(onComplete: () -> Unit) {
        sessionRepo.clearSession()
        onComplete()
    }

    fun formatSyncTime(timeMs: Long?): String {
        if (timeMs == null) return "Never synced"
        val sdf = SimpleDateFormat("dd MMM, HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timeMs))
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
