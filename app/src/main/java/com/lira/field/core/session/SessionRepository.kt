package com.lira.field.core.session

import android.content.Context
import android.content.SharedPreferences
import com.lira.field.BuildConfig
import com.lira.field.core.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("lira_session_prefs", Context.MODE_PRIVATE)

    private val _session = MutableStateFlow<UserSession?>(loadSession())
    val session: StateFlow<UserSession?> = _session.asStateFlow()

    private val _baseUrl = MutableStateFlow(loadBaseUrl())
    val baseUrl: StateFlow<String> = _baseUrl.asStateFlow()

    private val _mockModeEnabled = MutableStateFlow(loadMockMode())
    val mockModeEnabled: StateFlow<Boolean> = _mockModeEnabled.asStateFlow()

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    // Supabase Cloud Backend State
    private val _supabaseUrl = MutableStateFlow(loadSupabaseUrl())
    val supabaseUrl: StateFlow<String> = _supabaseUrl.asStateFlow()

    private val _supabaseKey = MutableStateFlow(loadSupabaseKey())
    val supabaseKey: StateFlow<String> = _supabaseKey.asStateFlow()

    // Language and Lab Fixture State
    private val _language = MutableStateFlow(loadLanguage())
    val language: StateFlow<String> = _language.asStateFlow()

    private val _useLabFixture = MutableStateFlow(loadUseLabFixture())
    val useLabFixture: StateFlow<Boolean> = _useLabFixture.asStateFlow()

    private fun loadLanguage(): String {
        return prefs.getString(KEY_LANGUAGE, "en") ?: "en"
    }

    private fun loadUseLabFixture(): Boolean {
        return prefs.getBoolean(KEY_USE_LAB_FIXTURE, false)
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
        _language.value = lang
    }

    fun setUseLabFixture(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_USE_LAB_FIXTURE, enabled).apply()
        _useLabFixture.value = enabled
    }

    private fun loadThemeMode(): String {
        return prefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM"
    }

    private fun loadSession(): UserSession? {
        val token = prefs.getString(KEY_TOKEN, null) ?: return null
        val userCode = prefs.getString(KEY_USER_CODE, "") ?: ""
        val displayName = prefs.getString(KEY_DISPLAY_NAME, "") ?: ""
        val role = prefs.getString(KEY_ROLE, "DRIVER") ?: "DRIVER"
        val truckId = prefs.getString(KEY_TRUCK_ID, "") ?: ""
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        val isDemoSession = prefs.getBoolean(KEY_IS_DEMO, false)
        val email = prefs.getString(KEY_EMAIL, null)

        // If session token has expired, return null to enforce re-authentication
        if (expiresAt > 0 && expiresAt < System.currentTimeMillis()) {
            return null
        }

        return UserSession(
            token = token,
            userCode = userCode,
            displayName = displayName,
            role = role,
            truckId = truckId,
            expiresAt = if (expiresAt == 0L) System.currentTimeMillis() + 86400000L else expiresAt,
            isDemoSession = isDemoSession,
            email = email
        )
    }

    private fun loadBaseUrl(): String {
        val saved = prefs.getString(KEY_BASE_URL, null)
        if (!saved.isNullOrBlank() && !saved.contains("192.168.43.100")) {
            return normalizeBaseUrl(saved)
        }
        return DEFAULT_FASTAPI_LAN_URL
    }

    fun normalizeBaseUrl(url: String): String {
        var trimmed = url.trim()
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            trimmed = "http://$trimmed"
        }
        if (!trimmed.endsWith("/")) {
            trimmed = "$trimmed/"
        }
        if (!trimmed.contains("/api/v1/") && !trimmed.contains("/rest/v1/")) {
            trimmed = "${trimmed}api/v1/"
        }
        return trimmed
    }

    fun setFastApiLanIp(ipOrHost: String) {
        val clean = ipOrHost.trim().removePrefix("http://").removePrefix("https://").removeSuffix("/")
        val ipOnly = if (clean.contains(":")) clean.substringBefore(":") else clean
        val newUrl = "http://$ipOnly:8000/api/v1/"
        updateBaseUrl(newUrl)
    }

    private fun loadMockMode(): Boolean {
        return prefs.getBoolean(KEY_MOCK_MODE, false)
    }

    private fun loadSupabaseUrl(): String {
        return prefs.getString(KEY_SUPABASE_URL, BuildConfig.SUPABASE_URL) ?: BuildConfig.SUPABASE_URL
    }

    private fun loadSupabaseKey(): String {
        return prefs.getString(KEY_SUPABASE_KEY, BuildConfig.SUPABASE_KEY) ?: BuildConfig.SUPABASE_KEY
    }

    fun saveSession(session: UserSession) {
        prefs.edit()
            .putString(KEY_TOKEN, session.token)
            .putString(KEY_USER_CODE, session.userCode)
            .putString(KEY_DISPLAY_NAME, session.displayName)
            .putString(KEY_ROLE, session.role)
            .putString(KEY_TRUCK_ID, session.truckId)
            .putLong(KEY_EXPIRES_AT, session.expiresAt)
            .putBoolean(KEY_IS_DEMO, session.isDemoSession)
            .putString(KEY_EMAIL, session.email)
            .apply()
        _session.value = session
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER_CODE)
            .remove(KEY_DISPLAY_NAME)
            .remove(KEY_ROLE)
            .remove(KEY_TRUCK_ID)
            .remove(KEY_EXPIRES_AT)
            .remove(KEY_IS_DEMO)
            .remove(KEY_EMAIL)
            .apply()
        _session.value = null
    }

    fun isSessionExpired(): Boolean {
        val s = _session.value ?: return true
        return s.expiresAt > 0 && s.expiresAt < System.currentTimeMillis()
    }

    fun updateBaseUrl(newUrl: String) {
        val sanitized = normalizeBaseUrl(newUrl)
        prefs.edit().putString(KEY_BASE_URL, sanitized).apply()
        _baseUrl.value = sanitized
    }

    fun setMockMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MOCK_MODE, enabled).apply()
        _mockModeEnabled.value = enabled
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeMode.value = mode
    }

    fun updateSupabaseConfig(url: String, key: String) {
        val sanitizedUrl = if (url.endsWith("/")) url else "$url/"
        prefs.edit()
            .putString(KEY_SUPABASE_URL, sanitizedUrl)
            .putString(KEY_SUPABASE_KEY, key)
            .apply()
        _supabaseUrl.value = sanitizedUrl
        _supabaseKey.value = key
    }

    fun getToken(): String? = _session.value?.token

    companion object {
        const val DEFAULT_FASTAPI_LAN_URL = "http://192.168.137.1:8000/api/v1/"
        private const val KEY_TOKEN = "session_token"
        private const val KEY_USER_CODE = "session_user_code"
        private const val KEY_DISPLAY_NAME = "session_display_name"
        private const val KEY_ROLE = "session_role"
        private const val KEY_TRUCK_ID = "session_truck_id"
        private const val KEY_EXPIRES_AT = "session_expires_at"
        private const val KEY_IS_DEMO = "session_is_demo"
        private const val KEY_EMAIL = "session_email"
        private const val KEY_BASE_URL = "lira_api_base_url"
        private const val KEY_MOCK_MODE = "lira_mock_mode"
        private const val KEY_THEME_MODE = "lira_theme_mode"
        private const val KEY_LANGUAGE = "lira_user_language"
        private const val KEY_USE_LAB_FIXTURE = "lira_use_lab_fixture"
        private const val KEY_SUPABASE_URL = "lira_supabase_url"
        private const val KEY_SUPABASE_KEY = "lira_supabase_key"
    }
}
