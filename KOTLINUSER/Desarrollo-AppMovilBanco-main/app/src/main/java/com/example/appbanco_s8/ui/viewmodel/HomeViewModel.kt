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

    fun cargarDatos(token: String) {
        viewModelScope.launch {

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
            }
        }
    }
}