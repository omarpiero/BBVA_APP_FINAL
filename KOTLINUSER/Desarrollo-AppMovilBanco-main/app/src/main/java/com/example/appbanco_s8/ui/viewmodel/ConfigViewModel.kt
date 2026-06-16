package com.example.appbanco_s8.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbanco_s8.data.model.Perfil
import com.example.appbanco_s8.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ConfigUiState {
    object Initial : ConfigUiState()
    object Loading : ConfigUiState()
    data class Success(val perfil: Perfil?) : ConfigUiState()
    data class Error(val mensaje: String) : ConfigUiState()
}

class ConfigViewModel : ViewModel() {
    private val api = RetrofitClient.api

    private val _uiState = MutableStateFlow<ConfigUiState>(ConfigUiState.Initial)
    val uiState: StateFlow<ConfigUiState> = _uiState.asStateFlow()

    private fun getUserIdFromToken(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size >= 2) {
                val payloadBytes = android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT or android.util.Base64.NO_WRAP)
                val payload = String(payloadBytes, Charsets.UTF_8)
                val json = org.json.JSONObject(payload)
                json.optString("sub")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun cargarPerfil(token: String, userId: String) {
        val realUserId = getUserIdFromToken(token) ?: userId
        viewModelScope.launch {
            _uiState.value = ConfigUiState.Loading
            try {
                val authHeader = "Bearer $token"
                val response = api.getPerfil(authHeader, "eq.$realUserId")
                if (response.isSuccessful) {
                    val perfiles = response.body() ?: emptyList()
                    _uiState.value = ConfigUiState.Success(perfiles.firstOrNull())
                } else {
                    _uiState.value = ConfigUiState.Error("Error al cargar perfil")
                }
            } catch (e: Exception) {
                _uiState.value = ConfigUiState.Error("Excepción: ${e.message}")
            }
        }
    }

    fun guardarPerfil(token: String, userId: String, nombre: String, email: String) {
        val realUserId = getUserIdFromToken(token) ?: userId
        viewModelScope.launch {
            _uiState.value = ConfigUiState.Loading
            try {
                val authHeader = "Bearer $token"
                // Check if exists first
                val checkResponse = api.getPerfil(authHeader, "eq.$realUserId")
                if (checkResponse.isSuccessful) {
                    val perfiles = checkResponse.body() ?: emptyList()
                    val perfil = Perfil(userId = realUserId, nombre = nombre, email = email)
                    if (perfiles.isEmpty()) {
                        // Create
                        api.createPerfil(authHeader, perfil)
                    } else {
                        // Update
                        api.updatePerfil(authHeader, "eq.$realUserId", perfil)
                    }
                    _uiState.value = ConfigUiState.Success(perfil)
                } else {
                    _uiState.value = ConfigUiState.Error("Error verificando perfil")
                }
            } catch (e: Exception) {
                _uiState.value = ConfigUiState.Error("Error: ${e.message}")
            }
        }
    }
}
