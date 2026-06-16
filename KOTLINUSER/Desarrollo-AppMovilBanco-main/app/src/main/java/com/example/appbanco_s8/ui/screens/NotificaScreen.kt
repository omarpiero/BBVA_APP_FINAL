package com.example.appbanco_s8.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
fun NotificaScreen(token: String, navController: NavHostController) {

    data class NotifItem(val icon: ImageVector, val label: String, val sublabel: String = "")

    val notificaciones = listOf(
        NotifItem(Icons.Outlined.CreditCard,    "Compras con tarjeta",     "Notificaciones"),
        NotifItem(Icons.Default.Info,           "Avisos y recordatorios",  "Notificaciones")
    )
    val oportunidades = listOf(
        NotifItem(Icons.Outlined.LocalOffer,    "Ofertas y promociones",   "Oportunidades")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulMarino)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text       = "Centro de mensajes",
                color      = Color.White,
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector        = Icons.Default.Menu,
                contentDescription = "Menú",
                tint               = Color.White
            )
        }

        HorizontalDivider(color = GrisSurface, thickness = 0.5.dp)

        Spacer(Modifier.height(8.dp))

        // Notificaciones
        Text(
            text     = "Notificaciones",
            color    = GrisTexto,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        notificaciones.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(item.icon, contentDescription = null, tint = AzulBanco, modifier = Modifier.size(22.dp))
                Text(item.label, color = AzulBanco, fontSize = 15.sp)
            }
            HorizontalDivider(color = GrisSurface, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
        }

        Spacer(Modifier.height(8.dp))

        // Oportunidades
        Text(
            text     = "Oportunidades",
            color    = GrisTexto,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        oportunidades.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(item.icon, contentDescription = null, tint = AzulBanco, modifier = Modifier.size(22.dp))
                Text(item.label, color = AzulBanco, fontSize = 15.sp)
            }
        }
    }
}