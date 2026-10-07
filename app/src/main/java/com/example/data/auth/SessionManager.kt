package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.UserRole
import com.example.data.model.UserSession

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "popular_cycle_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_PHONE = "user_phone"
        private const val KEY_NAME = "user_name"
        private const val KEY_ADDRESS = "user_address"
        private const val KEY_ROLE = "user_role"
    }

    fun saveSession(session: UserSession) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, session.isLoggedIn)
            putString(KEY_PHONE, session.phone)
            putString(KEY_NAME, session.name)
            putString(KEY_ADDRESS, session.address)
            putString(KEY_ROLE, session.role.name)
            apply()
        }
    }

    fun getSession(): UserSession {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val phone = prefs.getString(KEY_PHONE, "") ?: ""
        val name = prefs.getString(KEY_NAME, "") ?: ""
        val address = prefs.getString(KEY_ADDRESS, "") ?: ""
        val roleStr = prefs.getString(KEY_ROLE, UserRole.CUSTOMER.name) ?: UserRole.CUSTOMER.name
        val role = try {
            UserRole.valueOf(roleStr)
        } catch (_: Exception) {
            UserRole.CUSTOMER
        }

        return UserSession(
            isLoggedIn = isLoggedIn,
            phone = phone,
            name = name,
            address = address,
            role = role
        )
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
