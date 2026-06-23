package com.example.appbanco_s8.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.appbanco_s8.ui.viewmodel.CuentaViewModel
import com.example.appbanco_s8.ui.viewmodel.DataUiState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuentaScreen(
    token: String,
    email: String,
    navController: NavHostController,
    viewModel: CuentaViewModel = viewModel()
) {
    val ahorroState by viewModel.ahorro.collectAsState()
    val movimientosState by viewModel.movimientosCore.collectAsState()

    LaunchedEffect(token, email) {
        viewModel.cargarDatos(token, email)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020B18))
            .padding(16.dp)
    ) {
        Text(
            text = "Mis Ahorros",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp, top = 16.dp)
        )

        when (val state = ahorroState) {
            is DataUiState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1973B8))
                }
            }
            is DataUiState.Error -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C0B0B)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Error: ${state.mensaje}",
                        color = Color(0xFFFF6B6B),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            is DataUiState.Success -> {
                val ahorro = state.data
                if (ahorro != null) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A182D)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Ahorro Capital", color = Color(0xFFB0B8C8), fontSize = 14.sp)
                            Text(
                                text = "S/ ${String.format(Locale.US, "%.2f", ahorro.saldo)}",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tasa de Interés (TEA)", color = Color(0xFFB0B8C8), fontSize = 13.sp)
                                Text("${ahorro.tasaInteres}%", color = Color(0xFF5BBEFF), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    Text("No tienes una cuenta de ahorros activa.", color = Color(0xFFB0B8C8))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Historial de Movimientos",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(modifier = Modifier.weight(1f)) {
            when (val mState = movimientosState) {
                is DataUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF1973B8))
                    }
                }
                is DataUiState.Error -> {
                    Text("Error al cargar movimientos: ${mState.mensaje}", color = Color(0xFFFF6B6B))
                }
                is DataUiState.Success -> {
                    val movimientos = mState.data
                    if (movimientos.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No hay movimientos registrados", color = Color(0xFFB0B8C8))
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(movimientos) { mov ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1A2C)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(mov.concepto, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                            Text("Canal: ${mov.canal} • Ref: ${mov.codOperacion}", color = Color(0xFFB0B8C8), fontSize = 12.sp)
                                        }
                                        Text(
                                            text = "${if (mov.tipo == "DEB") "-" else "+"} S/ ${String.format(Locale.US, "%.2f", mov.monto)}",
                                            color = if (mov.tipo == "DEB") Color(0xFFFF6B6B) else Color(0xFF2ECC71),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
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
}
