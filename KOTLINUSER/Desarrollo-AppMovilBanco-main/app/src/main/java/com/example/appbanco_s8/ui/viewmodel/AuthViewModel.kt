package com.example.appbanco_s8.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbanco_s8.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle    : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val token: String, val email: String) : AuthUiState()
    data class Error(val mensaje: String) : AuthUiState()
}

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    // In-memory lockout tracking
    private var failedAttempts = 0
    private var lockoutEndTime: Long = 0

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Completa todos los campos")
            return
        }

        if (System.currentTimeMillis() < lockoutEndTime) {
            val remainingMinutes = ((lockoutEndTime - System.currentTimeMillis()) / 60000) + 1
            _uiState.value = AuthUiState.Error("Bloqueo por seguridad. Intenta en $remainingMinutes minuto(s).")
            return
        } else {
            // Reset if time passed
            if (lockoutEndTime > 0 && System.currentTimeMillis() >= lockoutEndTime) {
                failedAttempts = 0
                lockoutEndTime = 0
            }
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            // 1. Check database lockout status (if user exists in usuarios_cliente)
            val checkBlock = repository.obtenerEstadoBloqueo(email.trim())
            if (checkBlock.isSuccess) {
                val status = checkBlock.getOrNull()!!
                if (status.bloqueado) {
                    _uiState.value = AuthUiState.Error("Usuario bloqueado por seguridad debido a múltiples intentos fallidos en el sistema central.")
                    return@launch
                }
            }

            // 2. Perform authenticating call
            val result = repository.login(email.trim(), password)
            if (result.isSuccess) {
                failedAttempts = 0
                repository.resetearIntentosFallidos(email.trim())
                val data = result.getOrNull()!!
                _uiState.value = AuthUiState.Success(token = data.accessToken, email = data.user?.email ?: email)
            } else {
                failedAttempts++
                repository.registrarIntentoFallido(email.trim())
                
                if (failedAttempts >= 5) {
                    lockoutEndTime = System.currentTimeMillis() + (5 * 60 * 1000) // 5 minutes
                    _uiState.value = AuthUiState.Error("Has superado los 5 intentos. Aplicación bloqueada por 5 minutos.")
                } else {
                    _uiState.value = AuthUiState.Error("Credenciales incorrectas. Intento $failedAttempts de 5.")
                }
            }
        }
    }

    fun resetState() { _uiState.value = AuthUiState.Idle }
}