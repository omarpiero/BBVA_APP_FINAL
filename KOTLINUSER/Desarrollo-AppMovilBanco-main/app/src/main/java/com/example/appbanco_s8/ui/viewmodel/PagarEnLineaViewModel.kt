package com.example.appbanco_s8.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbanco_s8.data.model.Comercio
import com.example.appbanco_s8.data.model.Cuenta
import com.example.appbanco_s8.data.repository.CuentaRepository
import com.example.appbanco_s8.data.repository.PagoEnLineaRepository
import com.example.appbanco_s8.data.repository.PlinRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PagarEnLineaUiState(
    val comercio: Comercio? = null,
    val cuentaOrigen: Cuenta? = null,
    val monto: Double = 0.0,
    val medioPago: String = "Plin",
    val comision: Double = 0.0,
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val pagoExitoso: Boolean = false,
    val numeroOperacion: String = "",
    val error: String? = null
)

class PagarEnLineaViewModel : ViewModel() {
    private val cuentaRepo = CuentaRepository()
    private val pagoRepo = PagoEnLineaRepository()
    private val plinRepo = PlinRepository()

    private val _uiState = MutableStateFlow(PagarEnLineaUiState())
    val uiState: StateFlow<PagarEnLineaUiState> = _uiState.asStateFlow()

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

    fun cargarDatos(token: String, comercioId: String, comercioNombre: String, monto: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Cargar cuenta de origen (primera cuenta corriente)
            val resCuentas = cuentaRepo.getCuentas(token)
            val cuentaOrigen = resCuentas.getOrNull()
                ?.firstOrNull { it.tipo == "corriente" }
                ?: resCuentas.getOrNull()?.firstOrNull()

            // Buscar comercio por ID en la lista
            val resComercios = plinRepo.getComercios(token)
            val comercio = resComercios.getOrNull()
                ?.firstOrNull { it.id == comercioId }
                ?: Comercio(id = comercioId, nombre = comercioNombre)

            _uiState.value = PagarEnLineaUiState(
                comercio = comercio,
                cuentaOrigen = cuentaOrigen,
                monto = monto,
                isLoading = false,
                error = if (cuentaOrigen == null) "No se encontró cuenta de origen" else null
            )
        }
    }

    fun procesarPago(token: String, userId: String) {
        val state = _uiState.value
        val comercio = state.comercio ?: return
        val cuenta = state.cuentaOrigen ?: return

        if (state.monto <= 0) {
            _uiState.value = state.copy(error = "El monto debe ser mayor a 0")
            return
        }
        if (cuenta.saldo < state.monto) {
            _uiState.value = state.copy(error = "Saldo insuficiente. Saldo disponible: S/ %,.2f".format(cuenta.saldo))
            return
        }

        val realUserId = getUserIdFromToken(token) ?: userId

        viewModelScope.launch {
            val numOp = generarNumeroOperacion()
            _uiState.value = state.copy(isProcessing = true, error = null, numeroOperacion = numOp)

            val result = pagoRepo.procesarPago(
                token = token,
                userId = realUserId,
                comercioId = comercio.id,
                comercioNombre = comercio.nombre,
                cuentaOrigenId = cuenta.id,
                monto = state.monto,
                numeroOperacion = numOp
            )

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    pagoExitoso = true,
                    numeroOperacion = numOp
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = result.exceptionOrNull()?.message ?: "Error al procesar el pago"
                )
            }
        }
    }

    private fun generarNumeroOperacion(): String {
        val ts = System.currentTimeMillis()
        return ts.toString().takeLast(10).padStart(10, '0')
    }
}
