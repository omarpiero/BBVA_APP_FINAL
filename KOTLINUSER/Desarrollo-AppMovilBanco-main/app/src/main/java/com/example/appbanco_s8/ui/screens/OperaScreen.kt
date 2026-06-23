package com.example.appbanco_s8.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.appbanco_s8.data.repository.OperaRepository
import kotlinx.coroutines.launch

private val AzulMarino  = Color(0xFF020B18)
private val AzulBanco   = Color(0xFF1A5DC8)
private val GrisTexto   = Color(0xFFB0B8C8)
private val GrisSurface = Color(0xFF0D1F3C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperaScreen(token: String, email: String, navController: NavHostController) {
    val scope = rememberCoroutineScope()
    var showTransferDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    var origen by remember { mutableStateOf("") }
    var destino by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }

    data class OperaItem(val icon: ImageVector, val label: String)

    val operacionesCuenta = listOf(
        OperaItem(Icons.Default.SwapHoriz,        "Transferir"),
        OperaItem(Icons.Default.MoneyOff,          "Retiro sin tarjeta"),
        OperaItem(Icons.Default.Description,       "Ver estado de cuenta"),
        OperaItem(Icons.Default.PhoneAndroid,      "Recargar celular"),
        OperaItem(Icons.Default.Receipt,           "Pagar servicio"),
        OperaItem(Icons.Default.CreditCard,        "Pagar tarjeta"),
        OperaItem(Icons.Default.CardGiftcard,      "Recargar tarjeta regalo"),
        OperaItem(Icons.Default.PhoneIphone,       "PLIN"),
        OperaItem(Icons.Default.CurrencyExchange,  "T-Cambio"),
        OperaItem(Icons.Default.Link,              "Vincular tarjeta"),
        OperaItem(Icons.Default.FlightTakeoff,     "Transferir al exterior")
    )

    if (showTransferDialog) {
        AlertDialog(
            onDismissRequest = { if (!loading) showTransferDialog = false },
            title = { Text("Transferir Dinero", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = GrisSurface,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Ingresa los datos para realizar la transferencia:", color = GrisTexto, fontSize = 13.sp)

                    OutlinedTextField(
                        value = origen,
                        onValueChange = { origen = it },
                        label = { Text("Cuenta Origen (ej. ahorros)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AzulBanco,
                            unfocusedBorderColor = GrisTexto,
                            focusedLabelColor = AzulBanco,
                            unfocusedLabelColor = GrisTexto
                        )
                    )

                    OutlinedTextField(
                        value = destino,
                        onValueChange = { destino = it },
                        label = { Text("Cuenta Destino") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AzulBanco,
                            unfocusedBorderColor = GrisTexto,
                            focusedLabelColor = AzulBanco,
                            unfocusedLabelColor = GrisTexto
                        )
                    )

                    OutlinedTextField(
                        value = monto,
                        onValueChange = { monto = it },
                        label = { Text("Monto (S/)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AzulBanco,
                            unfocusedBorderColor = GrisTexto,
                            focusedLabelColor = AzulBanco,
                            unfocusedLabelColor = GrisTexto
                        )
                    )

                    if (errorMsg != null) {
                        Text(errorMsg!!, color = Color.Red, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mVal = monto.toDoubleOrNull()
                        if (origen.isNotBlank() && destino.isNotBlank() && mVal != null && mVal > 0) {
                            loading = true
                            errorMsg = null
                            scope.launch {
                                val repo = OperaRepository()
                                val res = repo.registrarOperacion(
                                    token = token,
                                    email = email,
                                    codCuentaOrigen = origen,
                                    codCuentaDestino = destino,
                                    tipo = "transferencia",
                                    monto = mVal
                                )
                                loading = false
                                if (res.isSuccess) {
                                    showTransferDialog = false
                                    showSuccessDialog = true
                                    origen = ""
                                    destino = ""
                                    monto = ""
                                } else {
                                    errorMsg = res.exceptionOrNull()?.message ?: "Error al transferir"
                                }
                            }
                        } else {
                            errorMsg = "Por favor completa todos los campos correctamente."
                        }
                    },
                    enabled = !loading,
                    colors = ButtonDefaults.buttonColors(containerColor = AzulBanco)
                ) {
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Transferir")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showTransferDialog = false },
                    enabled = !loading
                ) {
                    Text("Cancelar", color = GrisTexto)
                }
            }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Operación Exitosa", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = GrisSurface,
            text = {
                Text("La transferencia se ha registrado y encolado correctamente en el sistema.", color = GrisTexto)
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AzulBanco)
                ) {
                    Text("Entendido")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulMarino)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            Spacer(Modifier.height(16.dp))
            Text(
                text       = "Operaciones frecuentes",
                color      = Color.White,
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.padding(bottom = 16.dp)
            )
        }

        item {
            Text(
                text     = "Operaciones con cuentas",
                color    = AzulBanco,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Grid de operaciones 3 columnas
        item {
            val rows = operacionesCuenta.chunked(3)
            rows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { item ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (item.label == "Transferir") {
                                        showTransferDialog = true
                                    }
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(GrisSurface, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector        = item.icon,
                                    contentDescription = item.label,
                                    tint               = Color.White,
                                    modifier           = Modifier.size(22.dp)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text      = item.label,
                                color     = GrisTexto,
                                fontSize  = 10.sp,
                                maxLines  = 2,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                    // Rellenar si la fila no tiene 3 elementos
                    repeat(3 - row.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}