
package com.example.appbanco_s8.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbanco_s8.data.model.Prestamo
import com.example.appbanco_s8.data.repository.PrestamoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PrestamoViewModel : ViewModel() {
    private val repository = PrestamoRepository()

    private val _prestamos = MutableStateFlow<DataUiState<List<Prestamo>>>(DataUiState.Loading)
    val prestamos: StateFlow<DataUiState<List<Prestamo>>> = _prestamos

    fun cargarPrestamos(token: String) {
        viewModelScope.launch {
            _prestamos.value = DataUiState.Loading
            val result = repository.getPrestamos(token)
            _prestamos.value = if (result.isSuccess)
                DataUiState.Success(result.getOrNull()!!)
            else DataUiState.Error(result.exceptionOrNull()?.message ?: "Error")
        }
    }

    // Estado para la creación de solicitud
    private val _solicitudState = MutableStateFlow<DataUiState<Unit>>(DataUiState.Loading)
    val solicitudState: StateFlow<DataUiState<Unit>> = _solicitudState

    // Lógica del simulador (TEA 43.92%)
    fun calcularCuota(monto: Double, plazoMeses: Int): Double {
        if (monto <= 0 || plazoMeses <= 0) return 0.0
        val tea = 43.92 / 100.0
        val tem = Math.pow(1.0 + tea, 1.0 / 12.0) - 1.0
        val temp = Math.pow(1.0 + tem, plazoMeses.toDouble())
        return monto * (tem * temp) / (temp - 1.0)
    }

    fun enviarSolicitud(token: String, clienteId: String, monto: Double, plazo: Int) {
        viewModelScope.launch {
            _solicitudState.value = DataUiState.Loading
            val result = repository.solicitarPrestamo(token, clienteId, monto, plazo)
            _solicitudState.value = if (result.isSuccess)
                DataUiState.Success(Unit)
            else DataUiState.Error(result.exceptionOrNull()?.message ?: "Error")
        }
    }

    fun resetSolicitudState() {
        _solicitudState.value = DataUiState.Loading
    }
}