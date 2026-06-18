package com.example.appbanco_s8.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CreditScore
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.appbanco_s8.data.model.CreditoCore
import com.example.appbanco_s8.data.model.CronogramaCuota
import com.example.appbanco_s8.data.model.SolicitudCreditoEstado
import com.example.appbanco_s8.navigation.Screen
import com.example.appbanco_s8.ui.viewmodel.DataUiState
import com.example.appbanco_s8.ui.viewmodel.PrestamoViewModel
import java.util.Locale

private val CreditoBg = Color(0xFF020B18)
private val PanelBg = Color(0xFF07152A)
private val PanelBorder = Color(0xFF173B6B)
private val TextSoft = Color(0xFFB0B8C8)
private val Blue = Color(0xFF1973B8)
private val Cyan = Color(0xFF5BBEFF)
private val Green = Color(0xFF48AE64)
private val Red = Color(0xFFFF6B6B)
private val Yellow = Color(0xFFF5C842)

@Composable
fun CreditoHubScreen(
    token: String,
    navController: NavHostController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreditoBg)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = "Credito",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Solicita, revisa estados y consulta tus cuotas.",
            color = TextSoft,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 6.dp, bottom = 22.dp)
        )

        CreditoHubItem(
            icon = Icons.Default.Payments,
            title = "Solicitar credito",
            subtitle = "Formulario empresarial conectado al core BBVA.",
            onClick = { navController.navigate(Screen.Prestamo.createRoute(token)) }
        )
        CreditoHubItem(
            icon = Icons.Default.Assignment,
            title = "Mis solicitudes",
            subtitle = "Estado, expediente, monto y evaluacion actual.",
            onClick = { navController.navigate(Screen.SolicitudesCredito.createRoute(token)) }
        )
        CreditoHubItem(
            icon = Icons.Default.CreditScore,
            title = "Mis creditos",
            subtitle = "Creditos desembolsados y cuotas a pagar.",
            onClick = { navController.navigate(Screen.MisCreditos.createRoute(token)) }
        )
    }
}

@Composable
private fun CreditoHubItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = PanelBg),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, PanelBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                color = Blue.copy(alpha = 0.18f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Cyan,
                    modifier = Modifier.padding(12.dp).size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = TextSoft, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
fun SolicitudesCreditoScreen(
    token: String,
    email: String,
    navController: NavHostController,
    viewModel: PrestamoViewModel = viewModel()
) {
    val solicitudesState by viewModel.solicitudes.collectAsState()

    LaunchedEffect(token, email) {
        if (token.isNotBlank() && email.isNotBlank()) {
            viewModel.cargarSolicitudes(token, email)
        }
    }

    CreditoListScaffold(
        title = "Mis solicitudes",
        subtitle = "Seguimiento de solicitudes enviadas al asesor.",
        onBack = { navController.popBackStack() },
        onRefresh = { viewModel.cargarSolicitudes(token, email) }
    ) {
        when (val state = solicitudesState) {
            is DataUiState.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            is DataUiState.Error -> ErrorText(state.mensaje)
            is DataUiState.Success -> {
                if (state.data.isEmpty()) {
                    EmptyText("Aun no tienes solicitudes registradas.")
                } else {
                    state.data.forEach { solicitud ->
                        SolicitudCard(solicitud)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MisCreditosScreen(
    token: String,
    email: String,
    navController: NavHostController,
    viewModel: PrestamoViewModel = viewModel()
) {
    val prestamosState by viewModel.prestamos.collectAsState()

    LaunchedEffect(token, email) {
        if (token.isNotBlank() && email.isNotBlank()) {
            viewModel.cargarPrestamos(token, email)
        }
    }

    CreditoListScaffold(
        title = "Mis creditos",
        subtitle = "Creditos desembolsados, saldo y cuotas pendientes.",
        onBack = { navController.popBackStack() },
        onRefresh = { viewModel.cargarPrestamos(token, email) }
    ) {
        when (val state = prestamosState) {
            is DataUiState.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            is DataUiState.Error -> ErrorText(state.mensaje)
            is DataUiState.Success -> {
                if (state.data.isEmpty()) {
                    EmptyText("Aun no tienes creditos desembolsados.")
                } else {
                    state.data.forEach { credito ->
                        CreditoCard(credito)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CreditoListScaffold(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreditoBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = TextSoft, fontSize = 13.sp)
            }
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Cyan)
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        content()
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SolicitudCard(solicitud: SolicitudCreditoEstado) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PanelBg),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, PanelBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = solicitud.numero_expediente ?: "Expediente pendiente",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                EstadoBadge(solicitud.estado)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Monto solicitado S/ ${String.format(Locale.US, "%.2f", solicitud.monto_solicitado)}",
                color = TextSoft
            )
            Text(
                text = "Monto aprobado S/ ${String.format(Locale.US, "%.2f", solicitud.monto_aprobado ?: 0.0)}",
                color = TextSoft,
                fontSize = 12.sp
            )
            Text(
                text = "Plazo ${solicitud.plazo_meses ?: 0} meses - Cuota S/ ${String.format(Locale.US, "%.2f", solicitud.cuota_estimada ?: 0.0)}",
                color = TextSoft,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun CreditoCard(credito: CreditoCore) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PanelBg),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Green.copy(alpha = 0.75f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = credito.cod_cuenta_credito,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                EstadoBadge(credito.estado.ifBlank { "vigente" })
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Desembolsado S/ ${String.format(Locale.US, "%.2f", credito.monto_desembolsado)}",
                color = TextSoft
            )
            Text(
                text = "Saldo total S/ ${String.format(Locale.US, "%.2f", credito.saldo_total)} - ${credito.cuotas_pagadas ?: 0}/${credito.cuotas_total ?: 0} cuotas",
                color = TextSoft,
                fontSize = 12.sp
            )
            if (credito.cuotas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Cuotas a pagar",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                credito.cuotas.forEach { cuota ->
                    CuotaRow(cuota)
                }
            }
        }
    }
}

@Composable
private fun CuotaRow(cuota: CronogramaCuota) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AccountBalanceWallet,
            contentDescription = null,
            tint = if (cuota.estado_cuota == "pagada") Green else Cyan,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Cuota ${cuota.nro_cuota}",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = cuota.fecha_vencimiento,
                color = TextSoft,
                fontSize = 11.sp
            )
        }
        Text(
            text = "S/ ${String.format(Locale.US, "%.2f", cuota.monto_cuota)}",
            color = Cyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EstadoBadge(estado: String) {
    val color = when (estado) {
        "aprobado", "desembolsado", "vigente" -> Green
        "rechazado" -> Red
        "condicionado" -> Yellow
        else -> Cyan
    }
    Surface(
        color = color.copy(alpha = 0.16f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = estado.replace("_", " ").uppercase(Locale.US),
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun EmptyText(text: String) {
    Text(
        text = text,
        color = TextSoft,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
private fun ErrorText(text: String) {
    Text(
        text = text,
        color = Red,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}
