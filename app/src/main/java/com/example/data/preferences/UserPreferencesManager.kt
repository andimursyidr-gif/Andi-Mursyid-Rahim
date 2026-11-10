package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class UserRole {
    GURU,
    SISWA
}

data class UserSession(
    val role: UserRole = UserRole.GURU,
    val currentStudentId: Long = 1L,
    val currentStudentNis: String = "002101",
    val currentStudentName: String = "Ahmad Fauzi",
    val currentClassId: Long = 1L,
    val currentClassName: String = "X RPL 1",
    val isDarkMode: Boolean = false,
    val isOfflineMode: Boolean = false
)

class UserPreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("smart_learning_prefs", Context.MODE_PRIVATE)

    private val _session = MutableStateFlow(
        UserSession(
            role = if (prefs.getString("role", "GURU") == "SISWA") UserRole.SISWA else UserRole.GURU,
            currentStudentId = prefs.getLong("student_id", 1L),
            currentStudentNis = prefs.getString("student_nis", "002101") ?: "002101",
            currentStudentName = prefs.getString("student_name", "Ahmad Fauzi") ?: "Ahmad Fauzi",
            currentClassId = prefs.getLong("class_id", 1L),
            currentClassName = prefs.getString("class_name", "X RPL 1") ?: "X RPL 1",
            isDarkMode = prefs.getBoolean("dark_mode", false),
            isOfflineMode = prefs.getBoolean("offline_mode", false)
        )
    )
    val session: StateFlow<UserSession> = _session.asStateFlow()

    fun switchRole(role: UserRole) {
        prefs.edit().putString("role", role.name).apply()
        _session.value = _session.value.copy(role = role)
    }

    fun selectStudent(id: Long, nis: String, name: String, classId: Long, className: String) {
        prefs.edit()
            .putLong("student_id", id)
            .putString("student_nis", nis)
            .putString("student_name", name)
            .putLong("class_id", classId)
            .putString("class_name", className)
            .apply()

        _session.value = _session.value.copy(
            currentStudentId = id,
            currentStudentNis = nis,
            currentStudentName = name,
            currentClassId = classId,
            currentClassName = className
        )
    }

    fun toggleDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("dark_mode", enabled).apply()
        _session.value = _session.value.copy(isDarkMode = enabled)
    }

    fun toggleOfflineMode(enabled: Boolean) {
        prefs.edit().putBoolean("offline_mode", enabled).apply()
        _session.value = _session.value.copy(isOfflineMode = enabled)
    }
}
