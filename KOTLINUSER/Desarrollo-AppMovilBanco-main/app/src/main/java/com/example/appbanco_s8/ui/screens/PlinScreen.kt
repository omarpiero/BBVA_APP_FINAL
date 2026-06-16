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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.appbanco_s8.data.model.Comercio
import com.example.appbanco_s8.navigation.Screen
import com.example.appbanco_s8.ui.viewmodel.PlinViewModel

// ── Paleta BBVA ──────────────────────────────────────────────
private val BbvaAzulPrimario = Color(0xFF004481)
private val BbvaAzulMedio    = Color(0xFF1973B8)
private val BbvaBlanco       = Color(0xFFFFFFFF)
private val BbvaGrisFondo    = Color(0xFFF4F4F4)
private val BbvaGrisTexto    = Color(0xFF666666)
private val BbvaNegro        = Color(0xFF1D252D)
private val BbvaGrisBorde    = Color(0xFFE0E0E0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlinScreen(
    token: String,
    navController: NavHostController,
    viewModel: PlinViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showComercioDialog by remember { mutableStateOf(false) }

    LaunchedEffect(token) {
        viewModel.cargarDatos(token)
    }

    Scaffold(
        containerColor = BbvaBlanco,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = "Plin",
                        color      = BbvaNegro,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector        = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint               = BbvaAzulPrimario
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Outlined.HelpOutline, "Ayuda", tint = BbvaAzulPrimario)
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Menu, "Menú", tint = BbvaAzulPrimario)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BbvaBlanco)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BbvaBlanco),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── Hero Card PLIN ───────────────────────────────
            item {
                PlinHeroCard()
            }

            // ── Acciones principales (3 botones) ─────────────
            item {
                PlinMainActions()
            }

            // ── Otras operaciones ────────────────────────────
            item {
                Text(
                    text       = "Otras operaciones",
                    color      = BbvaNegro,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }

            item {
                PlinOtrasOperaciones(
                    onPagarEnLinea = { showComercioDialog = true }
                )
            }

            // ── Últimos movimientos ──────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text       = "Últimos movimientos",
                        color      = BbvaNegro,
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text     = "Buscar",
                        color    = BbvaAzulMedio,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { }
                    )
                }
            }

            // ── Tabs Finalizados / Pendientes ────────────────
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = BbvaBlanco,
                    contentColor = BbvaAzulPrimario,
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Finalizados",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Pendientes",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // ── Lista de movimientos ─────────────────────────
            val movimientos = if (selectedTab == 0)
                uiState.movimientosFinalizados
            else
                uiState.movimientosPendientes

            if (movimientos.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = "Sin movimientos",
                            color = BbvaGrisTexto,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(movimientos) { tx ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = tx.comercio ?: tx.destinatario ?: "Operación",
                                color = BbvaNegro,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = tx.fecha.take(10),
                                color = BbvaGrisTexto,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text       = tx.montoFormateado(),
                            color      = BbvaNegro,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = BbvaGrisBorde,
                        thickness = 0.5.dp
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    // ── Dialog seleccionar comercio ───────────────────────────
    if (showComercioDialog) {
        var dialogMonto by remember { mutableStateOf(0.0) }
        SeleccionarComercioDialog(
            comercios = uiState.comercios,
            isLoading = uiState.isLoading,
            onComercioSelected = { comercio, monto ->
                showComercioDialog = false
                navController.navigate(
                    Screen.PagarEnLinea.createRoute(
                        token = token,
                        comercioId = comercio.id,
                        comercioNombre = comercio.nombre,
                        monto = monto
                    )
                )
            },
            onDismiss = { showComercioDialog = false }
        )
    }
}

// ── Hero Card PLIN ───────────────────────────────────────────
@Composable
private fun PlinHeroCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = BbvaAzulPrimario
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text       = "Plin",
                color      = BbvaBlanco,
                fontSize   = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text      = "Paga más rápido a negocios y personas con la opción 'Ver / Pagar con QR'.",
                color     = BbvaBlanco.copy(alpha = 0.85f),
                fontSize  = 13.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text       = "Configuración de Plin",
                color      = Color(0xFF90CAF9),
                fontSize   = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier   = Modifier.clickable { }
            )
        }
    }
}

// ── Acciones principales PLIN ────────────────────────────────
@Composable
private fun PlinMainActions() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        PlinActionItem(
            icon  = Icons.Default.PersonAdd,
            label = "Enviar a\ncontactos",
            onClick = { /* Sprint 2 */ }
        )
        PlinActionItem(
            icon  = Icons.Default.AttachMoney,
            label = "Enviar a otro\nnúmero",
            onClick = { /* Sprint 2 */ }
        )
        PlinActionItem(
            icon  = Icons.Default.QrCodeScanner,
            label = "Ver / Pagar\ncon QR",
            onClick = { /* Sprint 2 */ }
        )
    }
}

@Composable
private fun PlinActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(90.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(BbvaGrisFondo, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = label,
                tint               = BbvaAzulPrimario,
                modifier           = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text      = label,
            color     = BbvaNegro,
            fontSize  = 11.sp,
            textAlign = TextAlign.Center,
            maxLines  = 2
        )
    }
}

// ── Otras operaciones ────────────────────────────────────────
@Composable
private fun PlinOtrasOperaciones(
    onPagarEnLinea: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        OtraOpItem(icon = Icons.Default.DirectionsBus, label = "Recarga\nde bus",     onClick = { })
        OtraOpItem(icon = Icons.Default.Computer,      label = "Pagar en\nlínea",     onClick = onPagarEnLinea)
        OtraOpItem(icon = Icons.Default.Description,   label = "Pagar\nimpuestos",   onClick = { })
        OtraOpItem(icon = Icons.Default.ConfirmationNumber, label = "Raspa y\ngana", onClick = { })
        OtraOpItem(icon = Icons.Default.PhoneAndroid,  label = "Recarga\nde celular", onClick = { })
    }
}

@Composable
private fun OtraOpItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(64.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(BbvaGrisFondo, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = BbvaAzulPrimario,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text      = label,
            color     = BbvaNegro,
            fontSize  = 10.sp,
            textAlign = TextAlign.Center,
            maxLines  = 2
        )
    }
}

// ── Dialog selección de comercio ─────────────────────────────
@Composable
private fun SeleccionarComercioDialog(
    comercios: List<Comercio>,
    isLoading: Boolean,
    onComercioSelected: (Comercio, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var montoTexto by remember { mutableStateOf("") }
    var selectedComercio by remember { mutableStateOf<Comercio?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BbvaBlanco,
        title = {
            Text(
                text = "Pagar compra en línea",
                color = BbvaNegro,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Selecciona el comercio y el monto a pagar:",
                    color = BbvaGrisTexto,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(16.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BbvaAzulMedio)
                    }
                } else if (comercios.isEmpty()) {
                    Text(
                        text  = "No hay comercios disponibles",
                        color = BbvaGrisTexto
                    )
                } else {
                    // Lista de comercios
                    Text("Comercio:", color = BbvaNegro, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))
                    Column {
                        comercios.take(6).forEach { comercio ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (selectedComercio?.id == comercio.id)
                                            BbvaAzulPrimario.copy(alpha = 0.1f)
                                        else Color.Transparent
                                    )
                                    .clickable { selectedComercio = comercio }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedComercio?.id == comercio.id,
                                    onClick = { selectedComercio = comercio },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = BbvaAzulPrimario
                                    )
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = comercio.nombre,
                                    color = BbvaNegro,
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.weight(1f))
                                Text(
                                    text = comercio.categoria,
                                    color = BbvaGrisTexto,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Campo monto
                    Text("Importe (S/):", color = BbvaNegro, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = montoTexto,
                        onValueChange = { montoTexto = it.filter { c -> c.isDigit() || c == '.' } },
                        placeholder = { Text("Ej: 2000", color = BbvaGrisTexto) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BbvaAzulPrimario,
                            unfocusedBorderColor = BbvaGrisBorde,
                            focusedTextColor = BbvaNegro,
                            unfocusedTextColor = BbvaNegro
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val monto = montoTexto.toDoubleOrNull() ?: 0.0
                    selectedComercio?.let { comercio ->
                        if (monto > 0) {
                            onComercioSelected(comercio, monto)
                        }
                    }
                },
                enabled = selectedComercio != null && (montoTexto.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BbvaAzulPrimario,
                    contentColor = BbvaBlanco
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Continuar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = BbvaGrisTexto)
            }
        }
    )
}
