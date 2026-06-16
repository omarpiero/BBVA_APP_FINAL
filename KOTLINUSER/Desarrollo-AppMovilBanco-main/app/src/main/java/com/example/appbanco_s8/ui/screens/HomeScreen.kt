package com.example.appbanco_s8.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.appbanco_s8.data.model.Cuenta
import com.example.appbanco_s8.data.model.Transaccion
import com.example.appbanco_s8.navigation.Screen
import com.example.appbanco_s8.ui.viewmodel.DataUiState
import com.example.appbanco_s8.ui.viewmodel.HomeViewModel
import com.example.appbanco_s8.ui.viewmodel.ConfigViewModel
import com.example.appbanco_s8.ui.viewmodel.ConfigUiState

// ── Paleta BBVA Claro ───────────────────────────────────────
private val BbvaAzulPrimario = Color(0xFF004481)
private val BbvaAzulMedio    = Color(0xFF1973B8)
private val BbvaAzulClaro    = Color(0xFFD4EDFC)
private val BbvaBlanco       = Color(0xFFFFFFFF)
private val BbvaGrisFondo    = Color(0xFFF4F4F4)
private val BbvaGrisTexto    = Color(0xFF666666)
private val BbvaGrisBorde    = Color(0xFFE0E0E0)
private val BbvaDorado       = Color(0xFFF5C842)
private val BbvaNegro        = Color(0xFF1D252D)
private val BbvaVerdeExito   = Color(0xFF2ECC71)
private val BbvaRojoError    = Color(0xFFE74C3C)

// ── Pantalla principal ───────────────────────────────────────
@Composable
fun HomeScreen(
    token: String,
    email: String,
    navController: NavHostController,
    onLogout: () -> Unit,
    onMenuClick: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val cuentasState       by viewModel.cuentas.collectAsStateWithLifecycle()
    val transaccionesState by viewModel.transacciones.collectAsStateWithLifecycle()

    val configViewModel: ConfigViewModel = viewModel()
    val perfilState by configViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(token) {
        viewModel.cargarDatos(token)
        configViewModel.cargarPerfil(token, email)
    }

    val nombreCorto = if (perfilState is ConfigUiState.Success) {
        val perfil = (perfilState as ConfigUiState.Success).perfil
        perfil?.nombre?.takeIf { it.isNotEmpty() } ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
    } else {
        email.substringBefore("@").replaceFirstChar { it.uppercase() }
    }

    var showBanner by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BbvaGrisFondo),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // ── 1. Header BBVA ───────────────────────────────────
        item {
            HeaderBBVA(
                nombreCorto = nombreCorto,
                onMenuClick = onMenuClick
            )
        }

        // ── 2. Resumen ingresos / gastos ─────────────────────
        item {
            when (val state = transaccionesState) {
                is DataUiState.Success -> ResumenBBVA(
                    ingresos = viewModel.ingresosMes,
                    gastos   = viewModel.gastosMes
                )
                is DataUiState.Loading -> LoadingCardBBVA()
                is DataUiState.Error   -> Unit
            }
        }

        // ── 3. Accesos rápidos ───────────────────────────────
        item {
            AccesosRapidosBBVA(
                navController = navController,
                token = token
            )
        }

        // ── 4. Banner promocional ────────────────────────────
        if (showBanner) {
            item {
                BannerPromoBBVA(onDismiss = { showBanner = false })
            }
        }

        // ── 5. Cuentas ──────────────────────────────────────
        item {
            val saldoTotal = when (val s = cuentasState) {
                is DataUiState.Success -> s.data.sumOf { it.saldo }
                else -> 0.0
            }
            CuentasSectionHeader(saldoTotal = saldoTotal)
        }

        when (val state = cuentasState) {
            is DataUiState.Loading -> item { LoadingCardBBVA() }
            is DataUiState.Error   -> item { ErrorCardBBVA(mensaje = state.mensaje) }
            is DataUiState.Success -> {
                items(state.data) { cuenta ->
                    CuentaCardBBVA(
                        cuenta  = cuenta,
                        onClick = {
                            navController.navigate(Screen.Cuenta.createRoute(token))
                        }
                    )
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ── Header BBVA ──────────────────────────────────────────────
@Composable
private fun HeaderBBVA(
    nombreCorto: String,
    onMenuClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BbvaBlanco,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text       = "Hola, $nombreCorto",
                    color      = BbvaNegro,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector        = Icons.Outlined.HelpOutline,
                        contentDescription = "Ayuda",
                        tint               = BbvaAzulPrimario
                    )
                }
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector        = Icons.Default.Menu,
                        contentDescription = "Menú",
                        tint               = BbvaAzulPrimario
                    )
                }
            }
        }
    }
}

// ── Resumen ingresos / gastos BBVA ───────────────────────────
@Composable
private fun ResumenBBVA(ingresos: Double, gastos: Double) {
    val total    = ingresos + gastos
    val progIng  = if (total > 0) (ingresos / total).toFloat().coerceIn(0f, 1f) else 0.5f
    val progGas  = if (total > 0) (gastos / total).toFloat().coerceIn(0f, 1f) else 0.5f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = BbvaBlanco,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Ingresos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Ingresos", color = BbvaGrisTexto, fontSize = 14.sp)
                Text(
                    text       = "S/ %,.2f".format(ingresos),
                    color      = BbvaNegro,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress   = { progIng },
                modifier   = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color      = BbvaDorado,
                trackColor = BbvaGrisBorde
            )

            Spacer(Modifier.height(16.dp))

            // Gastos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Gastos", color = BbvaGrisTexto, fontSize = 14.sp)
                Text(
                    text       = "S/ %,.2f".format(gastos),
                    color      = BbvaNegro,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress   = { progGas },
                modifier   = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color      = BbvaAzulMedio,
                trackColor = BbvaGrisBorde
            )

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text     = "Ir a mi día a día",
                    color    = BbvaAzulMedio,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { }
                )
            }
        }
    }
}

// ── Accesos rápidos BBVA ─────────────────────────────────────
@Composable
private fun AccesosRapidosBBVA(
    navController: NavHostController,
    token: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AccesoItemBBVA(
            icon    = Icons.Default.SwapHoriz,
            label   = "Transferir",
            onClick = { /* futuro */ }
        )
        AccesoItemBBVA(
            icon    = Icons.Default.PhoneAndroid,
            label   = "PLIN",
            isPlin  = true,
            onClick = { navController.navigate(Screen.Plin.createRoute(token)) }
        )
        AccesoItemBBVA(
            icon    = Icons.Default.CurrencyExchange,
            label   = "T-Cambio",
            onClick = { /* futuro */ }
        )
        AccesoItemBBVA(
            icon    = Icons.Default.MoreHoriz,
            label   = "Más",
            onClick = { navController.navigate(Screen.Opera.createRoute(token)) }
        )
    }
}

@Composable
private fun AccesoItemBBVA(
    icon: ImageVector,
    label: String,
    isPlin: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    if (isPlin) BbvaAzulPrimario else BbvaBlanco,
                    CircleShape
                )
                .then(
                    if (!isPlin) Modifier.shadow(1.dp, CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isPlin) {
                Text(
                    text       = "plin",
                    color      = BbvaBlanco,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(
                    imageVector        = icon,
                    contentDescription = label,
                    tint               = BbvaAzulPrimario,
                    modifier           = Modifier.size(24.dp)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = BbvaNegro, fontSize = 11.sp)
    }
}

// ── Banner promocional ───────────────────────────────────────
@Composable
private fun BannerPromoBBVA(onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = BbvaGrisFondo,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Ícono
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(BbvaVerdeExito.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = BbvaVerdeExito,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = "¡Felicidades!",
                    color      = BbvaNegro,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text     = "Tienes nuevas ofertas de productos.",
                    color    = BbvaGrisTexto,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text       = "Consúltalas aquí",
                    color      = BbvaAzulMedio,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier   = Modifier.clickable { }
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = BbvaGrisTexto,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ── Sección Cuentas Header ───────────────────────────────────
@Composable
private fun CuentasSectionHeader(saldoTotal: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text       = "Cuentas",
            color      = BbvaNegro,
            fontSize   = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text       = "S/ %,.2f".format(saldoTotal),
            color      = BbvaNegro,
            fontSize   = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ── Card de cuenta BBVA ──────────────────────────────────────
@Composable
private fun CuentaCardBBVA(cuenta: Cuenta, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = BbvaBlanco,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = when (cuenta.tipo) {
                        "corriente" -> "Cuenta independencia"
                        "ahorro"    -> "Cuenta ahorro"
                        else -> cuenta.tipo.replaceFirstChar { it.uppercase() }
                    },
                    color      = BbvaNegro,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text     = "•${cuenta.numeroCuenta.takeLast(4)}",
                    color    = BbvaGrisTexto,
                    fontSize = 13.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text       = "S/ %,.2f".format(cuenta.saldo),
                    color      = BbvaNegro,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text     = "Saldo disponible",
                    color    = BbvaGrisTexto,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ── Componentes auxiliares ────────────────────────────────────
@Composable
private fun LoadingCardBBVA() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color       = BbvaAzulMedio,
            strokeWidth = 2.dp,
            modifier    = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun ErrorCardBBVA(mensaje: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = BbvaRojoError.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint               = BbvaRojoError,
                modifier           = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text     = mensaje,
                color    = BbvaRojoError,
                fontSize = 13.sp
            )
        }
    }
}
