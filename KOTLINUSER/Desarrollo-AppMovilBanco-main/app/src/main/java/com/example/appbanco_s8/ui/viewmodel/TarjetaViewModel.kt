
package com.example.appbanco_s8.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbanco_s8.data.model.Tarjeta
import com.example.appbanco_s8.data.repository.TarjetaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TarjetaViewModel : ViewModel() {
    private val repository = TarjetaRepository()

    private val _tarjetas = MutableStateFlow<DataUiState<List<Tarjeta>>>(DataUiState.Loading)
    val tarjetas: StateFlow<DataUiState<List<Tarjeta>>> = _tarjetas

    fun cargarTarjetas(token: String) {
        viewModelScope.launch {
            _tarjetas.value = DataUiState.Loading
            val result = repository.getTarjetas(token)
            _tarjetas.value = if (result.isSuccess)
                DataUiState.Success(result.getOrNull()!!)
            else DataUiState.Error(result.exceptionOrNull()?.message ?: "Error")
        }
    }
}