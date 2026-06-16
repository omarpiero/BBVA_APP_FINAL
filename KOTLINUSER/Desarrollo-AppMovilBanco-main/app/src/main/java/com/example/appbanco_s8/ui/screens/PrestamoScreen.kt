package com.example.appbanco_s8.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.appbanco_s8.ui.viewmodel.DataUiState
import com.example.appbanco_s8.ui.viewmodel.PrestamoViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrestamoScreen(
    token: String,
    navController: NavHostController,
    viewModel: PrestamoViewModel = viewModel()
) {
    var montoStr by remember { mutableStateOf("") }
    var plazoStr by remember { mutableStateOf("") }
    var cuota by remember { mutableStateOf(0.0) }

    val solicitudState by viewModel.solicitudState.collectAsState()

    // Para la demo, usamos un cliente existente en bd_core_mobile
    val demoClienteId = "5636fc6e-93b0-4cf3-b30a-4188c0a6cd94"

    LaunchedEffect(montoStr, plazoStr) {
        val monto = montoStr.toDoubleOrNull() ?: 0.0
        val plazo = plazoStr.toIntOrNull() ?: 0
        if (monto > 0 && plazo > 0) {
            cuota = viewModel.calcularCuota(monto, plazo)
        } else {
            cuota = 0.0
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020B18))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Solicitar Préstamo",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp, top = 32.dp)
            )

            OutlinedTextField(
                value = montoStr,
                onValueChange = { montoStr = it },
                label = { Text("Monto a Solicitar (S/)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    focusedBorderColor = Color(0xFF004481),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFF004481),
                    unfocusedLabelColor = Color.Gray
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = plazoStr,
                onValueChange = { plazoStr = it },
                label = { Text("Plazo (Meses)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    focusedBorderColor = Color(0xFF004481),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFF004481),
                    unfocusedLabelColor = Color.Gray
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A182D)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Resumen de Simulación", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("TEA Referencial: 43.92%", color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cuota Mensual: S/ ${String.format(Locale.US, "%.2f", cuota)}",
                        color = Color(0xFF5BBEFF),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val monto = montoStr.toDoubleOrNull() ?: 0.0
                    val plazo = plazoStr.toIntOrNull() ?: 0
                    if (monto > 0 && plazo > 0) {
                        viewModel.enviarSolicitud(token, demoClienteId, monto, plazo)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1973B8))
            ) {
                Text("Enviar Solicitud", color = Color.White, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (solicitudState) {
                is DataUiState.Loading -> {
                    // No show anything or a tiny progress indicator
                }
                is DataUiState.Success -> {
                    Text(
                        text = "¡Solicitud enviada correctamente!",
                        color = Color.Green,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                is DataUiState.Error -> {
                    Text(
                        text = "Error: ${(solicitudState as DataUiState.Error).message}",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}
