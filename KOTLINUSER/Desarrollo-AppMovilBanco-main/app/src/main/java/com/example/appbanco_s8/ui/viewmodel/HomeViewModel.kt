package com.example.appbanco_s8.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbanco_s8.data.model.Cuenta
import com.example.appbanco_s8.data.model.Transaccion
import com.example.appbanco_s8.data.repository.CuentaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repository = CuentaRepository()

    private val _cuentas = MutableStateFlow<DataUiState<List<Cuenta>>>(DataUiState.Loading)
    val cuentas: StateFlow<DataUiState<List<Cuenta>>> = _cuentas

    private val _transacciones = MutableStateFlow<DataUiState<List<Transaccion>>>(DataUiState.Loading)
    val transacciones: StateFlow<DataUiState<List<Transaccion>>> = _transacciones

    private val prestamoRepository = com.example.appbanco_s8.data.repository.PrestamoRepository()
    private val _saldoAprobado = MutableStateFlow<Double>(0.0)
    val saldoAprobado: StateFlow<Double> = _saldoAprobado

    // Calculados en tiempo real desde el StateFlow de transacciones
    val ingresosMes: Double
        get() = when (val s = _transacciones.value) {
            is DataUiState.Success -> s.data
                .filter { !it.esDebito() }
                .sumOf { it.monto }
            else -> 0.0
        }

    val gastosMes: Double
        get() = when (val s = _transacciones.value) {
            is DataUiState.Success -> s.data
                .filter { it.esDebito() }
                .sumOf { it.monto }
            else -> 0.0
        }

    fun cargarDatos(token: String, email: String) {
        viewModelScope.launch {
            try {
                // 1. Cargar cuentas
                _cuentas.value = DataUiState.Loading
                val resCuentas = repository.getCuentas(token)
                _cuentas.value = if (resCuentas.isSuccess)
                    DataUiState.Success(resCuentas.getOrNull()!!)
                else
                    DataUiState.Error(resCuentas.exceptionOrNull()?.message ?: "Error al cargar cuentas")

                // 2. Cargar transacciones de la cuenta corriente
                val corriente = resCuentas.getOrNull()
                    ?.firstOrNull { it.tipo == "corriente" }

                if (corriente != null) {
                    _transacciones.value = DataUiState.Loading
                    val resTx = repository.getTransacciones(token, corriente.id)
                    _transacciones.value = if (resTx.isSuccess)
                        DataUiState.Success(resTx.getOrNull()!!)
                    else
                        DataUiState.Error(resTx.exceptionOrNull()?.message ?: "Error al cargar movimientos")
                } else {
                    _transacciones.value = DataUiState.Success(emptyList())
                }

                // 3. Cargar solicitudes para sumar saldo aprobado
                val resSol = prestamoRepository.getSolicitudes(token, email)
                if (resSol.isSuccess) {
                    val suma = resSol.getOrNull()!!
                        .filter { it.estado == "aprobado" || it.estado == "desembolsado" }
                        .sumOf { it.monto_aprobado ?: 0.0 }
                    _saldoAprobado.value = suma
                }
            } catch (e: Exception) {
                if (_cuentas.value is DataUiState.Loading) {
                    _cuentas.value = DataUiState.Error("Error inesperado: ${e.message}")
                }
                if (_transacciones.value is DataUiState.Loading) {
                    _transacciones.value = DataUiState.Error("Error inesperado: ${e.message}")
                }
            }
        }
    }
}