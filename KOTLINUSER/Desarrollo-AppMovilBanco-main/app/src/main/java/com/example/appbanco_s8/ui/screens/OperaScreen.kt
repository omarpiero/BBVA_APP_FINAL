package com.example.appbanco_s8.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

private val AzulMarino  = Color(0xFF020B18)
private val AzulBanco   = Color(0xFF1A5DC8)
private val GrisTexto   = Color(0xFFB0B8C8)
private val GrisSurface = Color(0xFF0D1F3C)

@Composable
fun OperaScreen(token: String, navController: NavHostController) {

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
                                .clickable { },
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