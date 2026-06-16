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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.appbanco_s8.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Paleta BBVA ──────────────────────────────────────────────
private val BbvaAzulPrimario = Color(0xFF004481)
private val BbvaBlanco       = Color(0xFFFFFFFF)
private val BbvaGrisTexto    = Color(0xFF666666)
private val BbvaNegro        = Color(0xFF1D252D)
private val BbvaGrisBorde    = Color(0xFFE0E0E0)
private val BbvaVerdeExito   = Color(0xFF00A650)
private val BbvaAzulInfo     = Color(0xFFD4EDFC)
private val BbvaAzulInfoText = Color(0xFF1973B8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmacionPagoScreen(
    token: String,
    comercioNombre: String,
    monto: Double,
    numeroOperacion: String,
    cuentaDisplay: String,
    emailUser: String,
    navController: NavHostController
) {
    val fechaHora = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("es", "PE")).format(Date())
    val emailEnmascarado = enmascararEmail(emailUser)

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
                    IconButton(onClick = {
                        // Volver al Home
                        navController.navigate(Screen.Home.createRoute(token, emailUser)) {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            // ── Check verde ──────────────────────────────────
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(BbvaVerdeExito, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.Check,
                    contentDescription = "Éxito",
                    tint               = BbvaBlanco,
                    modifier           = Modifier.size(44.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Título ───────────────────────────────────────
            Text(
                text       = "Pago realizado",
                color      = BbvaNegro,
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text      = "Ya puedes validar tu pago en la página del\ncomercio donde realizaste la compra.",
                color     = BbvaGrisTexto,
                fontSize  = 14.sp,
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(Modifier.height(24.dp))

            // ── Importe pagado ───────────────────────────────
            Text(
                text  = "Importe pagado",
                color = BbvaGrisTexto,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text       = "S/ %,.0f".format(monto),
                color      = BbvaAzulPrimario,
                fontSize   = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(24.dp))

            // ── Detalles ─────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                DetalleRow(label = "Fecha y hora",        value = fechaHora)
                DetalleRow(label = "Número de operación", value = numeroOperacion)
                DetalleRow(label = "Comercio",            value = comercioNombre)
                DetalleRow(label = "Medio de pago",       value = "Plin")
                DetalleRow(label = "Cuenta de origen",    value = cuentaDisplay)
                DetalleRow(label = "Comisión",            value = "Gratis", isLast = true)
            }

            Spacer(Modifier.height(24.dp))

            // ── Banner de constancia ─────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(12.dp),
                color = BbvaAzulInfo
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = BbvaAzulInfoText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text  = "Enviaremos la constancia a este correo: $emailEnmascarado",
                        color = BbvaAzulInfoText,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Fila de detalle ──────────────────────────────────────────
@Composable
private fun DetalleRow(label: String, value: String, isLast: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
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
    if (!isLast) {
        HorizontalDivider(color = BbvaGrisBorde, thickness = 0.5.dp)
    }
}

// ── Enmascarar email ─────────────────────────────────────────
private fun enmascararEmail(email: String): String {
    val parts = email.split("@")
    if (parts.size != 2) return email
    val nombre = parts[0]
    val dominio = parts[1]
    val visibleLen = 4.coerceAtMost(nombre.length)
    val visible = nombre.takeLast(visibleLen)
    val masked = "•".repeat((nombre.length - visibleLen).coerceAtLeast(0))
    return "$masked$visible@$dominio"
}
