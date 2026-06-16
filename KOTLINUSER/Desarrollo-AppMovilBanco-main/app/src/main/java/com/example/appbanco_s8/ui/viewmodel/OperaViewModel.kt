
package com.example.appbanco_s8.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbanco_s8.data.model.Pago
import com.example.appbanco_s8.data.repository.PagoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OperaViewModel : ViewModel() {
    private val repository = PagoRepository()

    private val _pagos = MutableStateFlow<DataUiState<List<Pago>>>(DataUiState.Loading)
    val pagos: StateFlow<DataUiState<List<Pago>>> = _pagos

    fun cargarPagos(token: String) {
        viewModelScope.launch {
            _pagos.value = DataUiState.Loading
            val result = repository.getPagos(token)
            _pagos.value = if (result.isSuccess)
                DataUiState.Success(result.getOrNull()!!)
            else DataUiState.Error(result.exceptionOrNull()?.message ?: "Error")
        }
    }
}