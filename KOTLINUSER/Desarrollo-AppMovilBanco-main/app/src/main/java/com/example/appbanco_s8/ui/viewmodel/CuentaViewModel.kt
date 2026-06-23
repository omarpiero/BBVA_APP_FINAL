
package com.example.appbanco_s8.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbanco_s8.data.model.*
import com.example.appbanco_s8.data.repository.CuentaRepository
import com.example.appbanco_s8.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CuentaViewModel : ViewModel() {
    private val repository = CuentaRepository()

    private val _cuentas = MutableStateFlow<DataUiState<List<Cuenta>>>(DataUiState.Loading)
    val cuentas: StateFlow<DataUiState<List<Cuenta>>> = _cuentas

    private val _transacciones = MutableStateFlow<DataUiState<List<Transaccion>>>(DataUiState.Loading)
    val transacciones: StateFlow<DataUiState<List<Transaccion>>> = _transacciones

    private val _ahorro = MutableStateFlow<DataUiState<CuentaAhorro?>>(DataUiState.Loading)
    val ahorro: StateFlow<DataUiState<CuentaAhorro?>> = _ahorro

    private val _movimientosCore = MutableStateFlow<DataUiState<List<MovimientoCore>>>(DataUiState.Loading)
    val movimientosCore: StateFlow<DataUiState<List<MovimientoCore>>> = _movimientosCore

    fun cargarDatos(token: String, email: String) {
        viewModelScope.launch {
            _cuentas.value = DataUiState.Loading
            val resCuentas = repository.getCuentas(token)
            _cuentas.value = if (resCuentas.isSuccess)
                DataUiState.Success(resCuentas.getOrNull()!!)
            else DataUiState.Error(resCuentas.exceptionOrNull()?.message ?: "Error")

            val corriente = (resCuentas.getOrNull() ?: emptyList())
                .firstOrNull { it.tipo == "corriente" }
            if (corriente != null) {
                _transacciones.value = DataUiState.Loading
                val resTx = repository.getTransacciones(token, corriente.id)
                _transacciones.value = if (resTx.isSuccess)
                    DataUiState.Success(resTx.getOrNull()!!)
                else DataUiState.Error(resTx.exceptionOrNull()?.message ?: "Error")
            }

            // Resolve client ID by email
            try {
                val clienteRes = RetrofitClient.api.getClientePorEmail("Bearer $token", "eq.$email")
                val cliente = clienteRes.body()?.firstOrNull()
                if (cliente != null) {
                    val resAhorro = repository.getCuentaAhorro(token, cliente.id)
                    _ahorro.value = if (resAhorro.isSuccess)
                        DataUiState.Success(resAhorro.getOrNull())
                    else DataUiState.Error(resAhorro.exceptionOrNull()?.message ?: "Error")

                    _movimientosCore.value = DataUiState.Loading
                    val resMov = repository.getMovimientosCore(token, cliente.id)
                    _movimientosCore.value = if (resMov.isSuccess)
                        DataUiState.Success(resMov.getOrNull()!!)
                    else DataUiState.Error(resMov.exceptionOrNull()?.message ?: "Error")
                } else {
                    _ahorro.value = DataUiState.Error("Cliente no encontrado")
                    _movimientosCore.value = DataUiState.Error("Cliente no encontrado")
                }
            } catch (e: Exception) {
                _ahorro.value = DataUiState.Error("Error al resolver cliente: ${e.message}")
                _movimientosCore.value = DataUiState.Error("Error al resolver cliente: ${e.message}")
            }
        }
    }
}