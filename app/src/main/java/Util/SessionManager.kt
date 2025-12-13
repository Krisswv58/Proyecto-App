package Util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREF_NAME = "MybookSession"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_USER_EMAIL = "userEmail"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_AUTH_TOKEN = "auth_token"
    }
    
    fun saveUserSession(userId: String, userName: String, userEmail: String) {
        prefs.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, userName)
            putString(KEY_USER_EMAIL, userEmail)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    
    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)
    
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
    
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    
    fun clearSession() {
        prefs.edit().clear().apply()
    }
    
    fun getCurrentUserId(): String {
        val userId = getUserId()
        if (userId.isNullOrBlank()) {
            throw Exception("No hay usuario logueado")
        }
        return userId
    }
    
    fun saveSession(userId: String, userName: String, userEmail: String) {
        saveUserSession(userId, userName, userEmail)
    }
    
    fun logout() {
        clearSession()
    }
}