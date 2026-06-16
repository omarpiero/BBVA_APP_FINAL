package com.example.appbanco_s8.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.appbanco_s8.navigation.Screen
import com.example.appbanco_s8.ui.viewmodel.PagarEnLineaViewModel

// ── Paleta BBVA ──────────────────────────────────────────────
private val BbvaAzulPrimario = Color(0xFF004481)
private val BbvaAzulMedio    = Color(0xFF1973B8)
private val BbvaBlanco       = Color(0xFFFFFFFF)
private val BbvaGrisFondo    = Color(0xFFF4F4F4)
private val BbvaGrisTexto    = Color(0xFF666666)
private val BbvaNegro        = Color(0xFF1D252D)
private val BbvaGrisBorde    = Color(0xFFE0E0E0)
private val BbvaVerde        = Color(0xFF00A650)
private val BbvaTeal         = Color(0xFF008B8B)
private val BbvaRojoSlide    = Color(0xFF5C2D2D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagarEnLineaScreen(
    token: String,
    comercioId: String,
    comercioNombre: String,
    monto: Double,
    email: String,
    navController: NavHostController,
    viewModel: PagarEnLineaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(token, comercioId) {
        viewModel.cargarDatos(token, comercioId, comercioNombre, monto)
    }

    // Navegar al éxito cuando el pago se procesa
    LaunchedEffect(uiState.pagoExitoso) {
        if (uiState.pagoExitoso) {
            val cuenta = uiState.cuentaOrigen
            val cuentaDisplay = when (cuenta?.tipo) {
                "corriente" -> "Cuenta Digital •${cuenta.numeroCuenta.takeLast(4)}"
                "ahorro"    -> "Cuenta Ahorro •${cuenta.numeroCuenta.takeLast(4)}"
                else -> "Cuenta •${cuenta?.numeroCuenta?.takeLast(4) ?: "----"}"
            }
            navController.navigate(
                Screen.ConfirmacionPago.createRoute(
                    token           = token,
                    comercioNombre  = uiState.comercio?.nombre ?: comercioNombre,
                    monto           = uiState.monto,
                    numeroOperacion = uiState.numeroOperacion,
                    cuentaDisplay   = cuentaDisplay,
                    emailUser       = email
                )
            ) {
                // Quitar PagarEnLinea del back stack
                popUpTo(Screen.Plin.route) { inclusive = false }
            }
        }
    }

    Scaffold(
        containerColor = BbvaBlanco,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = "Pagar compra en línea",
                        color      = BbvaNegro,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp
                    )
                },
                actions = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector        = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint               = BbvaNegro
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BbvaBlanco)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BbvaAzulMedio)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // ── Información del pago ─────────────────────
                Text(
                    text       = "Información del pago",
                    color      = BbvaNegro,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )

                InfoRow(label = "Comercio",       value = uiState.comercio?.nombre ?: comercioNombre)
                InfoRow(label = "Importe",         value = "S/ %,.0f".format(uiState.monto))
                InfoRow(label = "Medio de pago",   value = uiState.medioPago)

                Spacer(Modifier.height(24.dp))

                // ── Cuenta de origen ─────────────────────────
                Text(
                    text       = "Cuenta de origen",
                    color      = BbvaNegro,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(12.dp))

                val cuenta = uiState.cuentaOrigen
                if (cuenta != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = BbvaGrisFondo
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Ícono cuenta
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(BbvaTeal, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = BbvaBlanco,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when (cuenta.tipo) {
                                        "corriente" -> "Cuenta Digital"
                                        "ahorro"    -> "Cuenta Ahorro"
                                        else -> cuenta.tipo.replaceFirstChar { it.uppercase() }
                                    },
                                    color      = BbvaNegro,
                                    fontSize   = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text  = "•${cuenta.numeroCuenta.takeLast(4)}",
                                    color = BbvaGrisTexto,
                                    fontSize = 13.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text       = "S/ %,.0f".format(cuenta.saldo),
                                    color      = BbvaNegro,
                                    fontSize   = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text  = "Saldo disponible",
                                    color = BbvaGrisTexto,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // ── Importe a pagar ──────────────────────────
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    Text(
                        text  = "Importe a pagar",
                        color = BbvaGrisTexto,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text       = "S/ %,.0f".format(uiState.monto),
                        color      = BbvaAzulPrimario,
                        fontSize   = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = "Comisión: Gratis",
                        color = BbvaGrisTexto,
                        fontSize = 13.sp
                    )
                }

                // ── Error ────────────────────────────────────
                uiState.error?.let { errorMsg ->
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE74C3C).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text     = errorMsg,
                            color    = Color(0xFFE74C3C),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                // ── Botón Confirmar ──────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.procesarPago(token, email)
                        },
                        enabled = !uiState.isProcessing && uiState.cuentaOrigen != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BbvaVerde,
                            contentColor = BbvaBlanco,
                            disabledContainerColor = BbvaVerde.copy(alpha = 0.5f)
                        )
                    ) {
                        if (uiState.isProcessing) {
                            CircularProgressIndicator(
                                color = BbvaBlanco,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Confirmar",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.width(12.dp))
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(BbvaRojoSlide, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        tint = BbvaBlanco,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Fila de información ──────────────────────────────────────
@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text  = label,
            color = BbvaGrisTexto,
            fontSize = 14.sp
        )
        Text(
            text       = value,
            color      = BbvaNegro,
            fontSize   = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
    HorizontalDivider(
        modifier  = Modifier.padding(horizontal = 20.dp),
        color     = BbvaGrisBorde,
        thickness = 0.5.dp
    )
}
