package com.example.appbanco_s8.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbanco_s8.data.model.CreditoCore
import com.example.appbanco_s8.data.model.SolicitudCreditoEstado
import com.example.appbanco_s8.data.repository.PrestamoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.pow

class PrestamoViewModel : ViewModel() {
    private val repository = PrestamoRepository()

    private val _prestamos = MutableStateFlow<DataUiState<List<CreditoCore>>>(DataUiState.Loading)
    val prestamos: StateFlow<DataUiState<List<CreditoCore>>> = _prestamos

    private val _solicitudes =
        MutableStateFlow<DataUiState<List<SolicitudCreditoEstado>>>(DataUiState.Loading)
    val solicitudes: StateFlow<DataUiState<List<SolicitudCreditoEstado>>> = _solicitudes

    private val _solicitudState = MutableStateFlow<DataUiState<Unit>>(DataUiState.Loading)
    val solicitudState: StateFlow<DataUiState<Unit>> = _solicitudState

    fun cargarPrestamos(token: String, email: String) {
        viewModelScope.launch {
            _prestamos.value = DataUiState.Loading
            val result = repository.getPrestamos(token, email)
            _prestamos.value = if (result.isSuccess)
                DataUiState.Success(result.getOrNull()!!)
            else DataUiState.Error(result.exceptionOrNull()?.message ?: "Error")
        }
    }

    fun cargarSolicitudes(token: String, email: String) {
        viewModelScope.launch {
            _solicitudes.value = DataUiState.Loading
            val result = repository.getSolicitudes(token, email)
            _solicitudes.value = if (result.isSuccess)
                DataUiState.Success(result.getOrNull()!!)
            else DataUiState.Error(result.exceptionOrNull()?.message ?: "Error")
        }
    }

    fun calcularCuota(monto: Double, plazoMeses: Int, teaAnual: Double): Double {
        if (monto <= 0 || plazoMeses <= 0) return 0.0
        val tea = teaAnual / 100.0
        val tem = (1.0 + tea).pow(1.0 / 12.0) - 1.0
        val factor = (1.0 + tem).pow(plazoMeses.toDouble())
        return monto * (tem * factor) / (factor - 1.0)
    }

    fun enviarSolicitud(
        token: String,
        email: String,
        monto: Double,
        plazo: Int,
        destino: String,
        garantia: String,
        tea: Double,
        gastosMensuales: Double
    ) {
        viewModelScope.launch {
            _solicitudState.value = DataUiState.Loading
            val result = repository.solicitarPrestamo(
                token = token,
                email = email,
                monto = monto,
                plazo = plazo,
                destino = destino,
                garantia = garantia,
                tea = tea,
                gastosMensuales = gastosMensuales
            )
            _solicitudState.value = if (result.isSuccess) {
                cargarSolicitudes(token, email)
                cargarPrestamos(token, email)
                DataUiState.Success(Unit)
            } else DataUiState.Error(result.exceptionOrNull()?.message ?: "Error")
        }
    }

    fun resetSolicitudState() {
        _solicitudState.value = DataUiState.Loading
    }
}
