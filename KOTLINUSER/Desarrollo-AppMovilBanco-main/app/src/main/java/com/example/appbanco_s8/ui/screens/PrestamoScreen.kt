package com.example.appbanco_s8.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
    email: String,
    navController: NavHostController,
    viewModel: PrestamoViewModel = viewModel()
) {
    var montoStr by remember { mutableStateOf("") }
    var plazoStr by remember { mutableStateOf("12") }
    var destino by remember { mutableStateOf("Capital de trabajo") }
    var garantia by remember { mutableStateOf("sin garantia") }
    var gastosStr by remember { mutableStateOf("") }
    var tea by remember { mutableStateOf(43.92) }
    var cuota by remember { mutableStateOf(0.0) }

    val solicitudState by viewModel.solicitudState.collectAsState()
    var validationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(montoStr, plazoStr, tea) {
        validationError = null
        val monto = montoStr.toDoubleOrNull() ?: 0.0
        val plazo = plazoStr.toIntOrNull() ?: 0
        cuota = if (monto > 0 && plazo > 0) viewModel.calcularCuota(monto, plazo, tea) else 0.0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020B18))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Credito empresarial",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp, top = 24.dp)
        )
        Text(
            text = "Solicitud conectada al core BBVA",
            color = Color(0xFFB0B8C8),
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = montoStr,
            onValueChange = { montoStr = it },
            label = { Text("Monto a solicitar (S/)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = plazoStr,
            onValueChange = { plazoStr = it },
            label = { Text("Plazo en meses") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = gastosStr,
            onValueChange = { gastosStr = it },
            label = { Text("Gasto mensual (S/)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = destino,
            onValueChange = { destino = it },
            label = { Text("Destino del credito") },
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = garantia,
            onValueChange = { garantia = it },
            label = { Text("Garantia") },
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = tea == 43.92,
                onClick = { tea = 43.92 },
                label = { Text("Sin seguro 43.92%") }
            )
            FilterChip(
                selected = tea == 40.92,
                onClick = { tea = 40.92 },
                label = { Text("Con seguro 40.92%") }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A182D)),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFF173B6B))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Simulacion francesa", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("TEA referencial: ${String.format(Locale.US, "%.2f", tea)}%", color = Color(0xFFB0B8C8))
                Text(
                    text = "Cuota mensual: S/ ${String.format(Locale.US, "%.2f", cuota)}",
                    color = Color(0xFF5BBEFF),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                val monto = montoStr.toDoubleOrNull() ?: 0.0
                val plazo = plazoStr.toIntOrNull() ?: 0
                val gastos = gastosStr.toDoubleOrNull() ?: 0.0
                
                if (monto < 500) {
                    validationError = "El monto mínimo a solicitar es S/ 500.00"
                } else if (plazo <= 0) {
                    validationError = "El plazo debe ser mayor a 0 meses"
                } else {
                    validationError = null
                    viewModel.enviarSolicitud(
                        token = token,
                        email = email,
                        monto = monto,
                        plazo = plazo,
                        destino = destino,
                        garantia = garantia,
                        tea = tea,
                        gastosMensuales = gastos
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1973B8))
        ) {
            Text("Enviar solicitud", color = Color.White, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (validationError != null) {
            Text(
                text = validationError!!,
                color = Color(0xFFFF6B6B),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        when (solicitudState) {
            is DataUiState.Success -> Text(
                text = "Solicitud enviada correctamente.",
                color = Color(0xFF48AE64),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            is DataUiState.Error -> Text(
                text = "Error: ${(solicitudState as DataUiState.Error).mensaje}",
                color = Color(0xFFFF6B6B),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            is DataUiState.Loading -> Unit
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Color(0xFF1973B8),
    unfocusedBorderColor = Color(0xFF5A6B85),
    focusedLabelColor = Color(0xFF5BBEFF),
    unfocusedLabelColor = Color(0xFFB0B8C8),
    cursorColor = Color(0xFF5BBEFF)
)
