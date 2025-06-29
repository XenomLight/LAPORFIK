package com.example.applaporfik.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    
    companion object {
        private const val PREF_NAME = "app_session"
        private const val PREF_USER_DATA = "user_data"
        private const val KEY_TOKEN = "token"
        private const val KEY_NIM = "nim"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LOGIN_TIME = "login_time"
        private const val KEY_SESSION_DURATION = "session_duration"
        
        // Default session duration: 30 days in milliseconds
        private const val DEFAULT_SESSION_DURATION = 30L * 24L * 60L * 60L * 1000L
    }
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val userDataPreferences: SharedPreferences = context.getSharedPreferences(PREF_USER_DATA, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()
    private val userDataEditor: SharedPreferences.Editor = userDataPreferences.edit()
    
    /**
     * Save user session data
     */
    fun saveSession(
        token: String,
        nim: String,
        userName: String,
        userRole: String,
        rememberMe: Boolean = false,
        sessionDuration: Long = DEFAULT_SESSION_DURATION
    ) {
        editor.apply {
            putString(KEY_TOKEN, token)
            putString(KEY_NIM, nim)
            putString(KEY_USER_NAME, userName)
            putString(KEY_USER_ROLE, userRole)
            putBoolean(KEY_REMEMBER_ME, rememberMe)
            putBoolean(KEY_IS_LOGGED_IN, true)
            putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
            putLong(KEY_SESSION_DURATION, sessionDuration)
            apply()
        }
        
        // Save user credentials to separate storage for persistence
        userDataEditor.apply {
            putString(KEY_NIM, nim)
            putString(KEY_USER_NAME, userName)
            putString(KEY_USER_ROLE, userRole)
            apply()
        }
    }
    
    /**
     * Check if user is currently logged in
     */
    fun isLoggedIn(): Boolean {
        val isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
        
        if (!isLoggedIn) return false
        
        // Check if session has expired (only if remember me is not enabled)
        val rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)
        if (!rememberMe) {
            val loginTime = sharedPreferences.getLong(KEY_LOGIN_TIME, 0)
            val sessionDuration = sharedPreferences.getLong(KEY_SESSION_DURATION, DEFAULT_SESSION_DURATION)
            val currentTime = System.currentTimeMillis()
            
            if (currentTime - loginTime > sessionDuration) {
                // Session expired, clear session but keep user data if remember me was enabled
                clearSession()
                return false
            }
        }
        
        return true
    }
    
    /**
     * Get user token
     */
    fun getToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }
    
    /**
     * Get NIM
     */
    fun getNim(): String? {
        return sharedPreferences.getString(KEY_NIM, null)
    }
    
    /**
     * Get user name
     */
    fun getUserName(): String? {
        return sharedPreferences.getString(KEY_USER_NAME, null)
    }
    
    /**
     * Get user role
     */
    fun getUserRole(): String? {
        return sharedPreferences.getString(KEY_USER_ROLE, null)
    }
    
    /**
     * Check if remember me is enabled
     */
    fun isRememberMeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)
    }
    
    /**
     * Get session info
     */
    fun getSessionInfo(): SessionInfo? {
        if (!isLoggedIn()) return null
        
        return SessionInfo(
            token = getToken() ?: return null,
            nim = getNim() ?: return null,
            userName = getUserName() ?: return null,
            userRole = getUserRole() ?: return null,
            rememberMe = isRememberMeEnabled(),
            loginTime = sharedPreferences.getLong(KEY_LOGIN_TIME, 0),
            sessionDuration = sharedPreferences.getLong(KEY_SESSION_DURATION, DEFAULT_SESSION_DURATION)
        )
    }
    
    /**
     * Clear user session (but keep user data if remember me is enabled)
     */
    fun clearSession() {
        val rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)
        
        // Clear session data
        editor.clear().apply()
        
        // If remember me is not enabled, also clear user data
        if (!rememberMe) {
            userDataEditor.clear().apply()
        }
    }
    
    /**
     * Clear all data including user credentials
     */
    fun clearAllData() {
        editor.clear().apply()
        userDataEditor.clear().apply()
    }
    
    /**
     * Check if user credentials exist in local storage
     */
    fun hasStoredCredentials(): Boolean {
        return userDataPreferences.getString(KEY_NIM, null) != null
    }
    
    /**
     * Get stored user credentials
     */
    fun getStoredCredentials(): StoredCredentials? {
        val nim = userDataPreferences.getString(KEY_NIM, null) ?: return null
        val userName = userDataPreferences.getString(KEY_USER_NAME, null) ?: return null
        val userRole = userDataPreferences.getString(KEY_USER_ROLE, null) ?: return null
        
        return StoredCredentials(nim, userName, userRole)
    }
    
    /**
     * Update session time (useful for extending session on app usage)
     */
    fun updateSessionTime() {
        if (isLoggedIn()) {
            editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis()).apply()
        }
    }
    
    /**
     * Get remaining session time in milliseconds
     */
    fun getRemainingSessionTime(): Long {
        if (!isLoggedIn() || isRememberMeEnabled()) return -1 // -1 means no expiration
        
        val loginTime = sharedPreferences.getLong(KEY_LOGIN_TIME, 0)
        val sessionDuration = sharedPreferences.getLong(KEY_SESSION_DURATION, DEFAULT_SESSION_DURATION)
        val currentTime = System.currentTimeMillis()
        
        return sessionDuration - (currentTime - loginTime)
    }
    
    /**
     * Check if session is about to expire (within 1 hour)
     */
    fun isSessionExpiringSoon(): Boolean {
        if (isRememberMeEnabled()) return false
        
        val remainingTime = getRemainingSessionTime()
        return remainingTime > 0 && remainingTime < 60 * 60 * 1000 // 1 hour
    }
    
    /**
     * Check if token needs to be refreshed (empty or null token)
     */
    fun needsTokenRefresh(): Boolean {
        val token = getToken()
        return token.isNullOrEmpty()
    }
    
    /**
     * Update token without changing other session data
     */
    fun updateToken(newToken: String) {
        editor.putString(KEY_TOKEN, newToken).apply()
    }
}

/**
 * Data class to hold session information
 */
data class SessionInfo(
    val token: String,
    val nim: String,
    val userName: String,
    val userRole: String,
    val rememberMe: Boolean,
    val loginTime: Long,
    val sessionDuration: Long
)

/**
 * Data class to hold stored user credentials
 */
data class StoredCredentials(
    val nim: String,
    val userName: String,
    val userRole: String
) 