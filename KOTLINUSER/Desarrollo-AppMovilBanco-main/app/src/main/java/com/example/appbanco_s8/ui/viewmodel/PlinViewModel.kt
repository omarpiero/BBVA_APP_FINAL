package com.example.appbanco_s8.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbanco_s8.data.model.Comercio
import com.example.appbanco_s8.data.model.PlinTransaccion
import com.example.appbanco_s8.data.repository.PlinRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlinUiState(
    val comercios: List<Comercio> = emptyList(),
    val movimientosFinalizados: List<PlinTransaccion> = emptyList(),
    val movimientosPendientes: List<PlinTransaccion> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class PlinViewModel : ViewModel() {
    private val repository = PlinRepository()

    private val _uiState = MutableStateFlow(PlinUiState())
    val uiState: StateFlow<PlinUiState> = _uiState.asStateFlow()

    fun cargarDatos(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Cargar comercios
            val resComercios = repository.getComercios(token)
            val comercios = resComercios.getOrNull() ?: emptyList()

            // Cargar transacciones PLIN
            val resTx = repository.getPlinTransacciones(token)
            val transacciones = resTx.getOrNull() ?: emptyList()

            _uiState.value = PlinUiState(
                comercios = comercios,
                movimientosFinalizados = transacciones.filter { it.esFinalizado() },
                movimientosPendientes = transacciones.filter { it.esPendiente() },
                isLoading = false,
                error = if (resComercios.isFailure && resTx.isFailure)
                    "Error al cargar datos" else null
            )
        }
    }
}
