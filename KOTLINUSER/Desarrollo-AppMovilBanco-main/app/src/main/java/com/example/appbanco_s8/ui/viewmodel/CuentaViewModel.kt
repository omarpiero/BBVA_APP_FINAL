
package com.example.appbanco_s8.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbanco_s8.data.model.*
import com.example.appbanco_s8.data.repository.CuentaRepository
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

    fun cargarDatos(token: String) {
        viewModelScope.launch {
            _cuentas.value = DataUiState.Loading
            val resCuentas = repository.getCuentas(token)
            _cuentas.value = if (resCuentas.isSuccess)
                DataUiState.Success(resCuentas.getOrNull()!!)
            else DataUiState.Error(resCuentas.exceptionOrNull()?.message ?: "Error")

            val corriente = (resCuentas.getOrNull() ?: emptyList())
                .firstOrNull { it.tipo == "corriente" }
            if (corriente != null) {
                val resTx = repository.getTransacciones(token, corriente.id)
                _transacciones.value = if (resTx.isSuccess)
                    DataUiState.Success(resTx.getOrNull()!!)
                else DataUiState.Error(resTx.exceptionOrNull()?.message ?: "Error")
            }

            val resAhorro = repository.getCuentaAhorro(token)
            _ahorro.value = if (resAhorro.isSuccess)
                DataUiState.Success(resAhorro.getOrNull())
            else DataUiState.Error(resAhorro.exceptionOrNull()?.message ?: "Error")
        }
    }
}