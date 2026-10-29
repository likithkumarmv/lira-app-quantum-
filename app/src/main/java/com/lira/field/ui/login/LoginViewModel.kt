package com.lira.field.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lira.field.core.network.LiraNetworkResult
import com.lira.field.core.session.SessionRepository
import com.lira.field.data.LiraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AuthMode {
    SUPABASE_AUTH,
    DEV_BYPASS
}

class LoginViewModel(
    private val repository: LiraRepository,
    private val sessionRepo: SessionRepository
) : ViewModel() {

    private val _authMode = MutableStateFlow(AuthMode.DEV_BYPASS)
    val authMode: StateFlow<AuthMode> = _authMode.asStateFlow()

    private val _email = MutableStateFlow("driver_t07@lira.field")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _selectedDriver = MutableStateFlow("T09")
    val selectedDriver: StateFlow<String> = _selectedDriver.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess.asStateFlow()

    val baseUrl: StateFlow<String> = sessionRepo.baseUrl
    val supabaseUrl: StateFlow<String> = sessionRepo.supabaseUrl
    val themeMode: StateFlow<String> = sessionRepo.themeMode

    fun setAuthMode(mode: AuthMode) {
        _authMode.value = mode
        _errorMessage.value = null
    }

    fun setEmail(value: String) {
        _email.value = value
    }

    fun setPassword(value: String) {
        _password.value = value
    }

    fun selectDriver(driver: String) {
        _selectedDriver.value = driver
    }

    fun toggleTheme() {
        val current = sessionRepo.themeMode.value
        val next = if (current == "LIGHT") "DARK" else "LIGHT"
        sessionRepo.setThemeMode(next)
    }

    fun performLogin() {
        _errorMessage.value = null
        if (_authMode.value == AuthMode.SUPABASE_AUTH) {
            val em = _email.value.trim()
            val pw = _password.value
            if (em.isBlank() || pw.isBlank()) {
                _errorMessage.value = "Please enter both driver email and password"
                return
            }
            viewModelScope.launch {
                _isLoading.value = true
                when (val result = repository.loginWithSupabase(em, pw)) {
                    is LiraNetworkResult.Success -> {
                        _isLoading.value = false
                        _loginSuccess.value = true
                    }
                    is LiraNetworkResult.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = "[${result.code}] ${result.message}"
                    }
                    is LiraNetworkResult.OfflineQueued -> {
                        _isLoading.value = false
                        _loginSuccess.value = true
                    }
                }
            }
        } else {
            val codeToLogin = _selectedDriver.value.trim().ifBlank { "T07" }
            viewModelScope.launch {
                _isLoading.value = true
                when (val result = repository.loginDevelopmentMode(codeToLogin)) {
                    is LiraNetworkResult.Success -> {
                        _isLoading.value = false
                        _loginSuccess.value = true
                    }
                    is LiraNetworkResult.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = "[${result.code}] ${result.message}"
                    }
                    is LiraNetworkResult.OfflineQueued -> {
                        _isLoading.value = false
                        _loginSuccess.value = true
                    }
                }
            }
        }
    }

    fun resetSuccess() {
        _loginSuccess.value = false
    }
}
