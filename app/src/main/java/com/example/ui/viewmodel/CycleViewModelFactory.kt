package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.auth.AuthService
import com.example.data.auth.SessionManager
import com.example.data.repository.CycleRepository

class CycleViewModelFactory(
    private val repository: CycleRepository,
    private val authService: AuthService,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CycleViewModel::class.java)) {
            return CycleViewModel(repository, authService, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
