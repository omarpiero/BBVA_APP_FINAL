package com.example.appbanco_s8.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.appbanco_s8.navigation.Screen
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appbanco_s8.ui.viewmodel.ConfigViewModel
import com.example.appbanco_s8.ui.viewmodel.ConfigUiState
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

// ── Paleta BBVA Claro ────────────────────────────────────────
private val BbvaAzulPrimario = Color(0xFF004481)
private val BbvaAzulMedio    = Color(0xFF1973B8)
private val BbvaBlanco       = Color(0xFFFFFFFF)
private val BbvaGrisFondo    = Color(0xFFF4F4F4)
private val BbvaGrisTexto    = Color(0xFF666666)
private val BbvaNegro        = Color(0xFF1D252D)
private val BbvaGrisBorde    = Color(0xFFE0E0E0)
private val BbvaDorado       = Color(0xFFF5C842)
private val BbvaRojoSalir    = Color(0xFFE74C3C)

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
    val isCentral: Boolean = false
)

data class DrawerItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val isDestructive: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    token: String,
    email: String,
    navController: NavHostController,
    onLogout: () -> Unit,
    onOpenDrawer: ((openFn: () -> Unit) -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope       = rememberCoroutineScope()

    // ── Pasar la lambda al caller usando SideEffect ──
    SideEffect {
        onOpenDrawer?.invoke {
            scope.launch { drawerState.open() }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route?.substringBefore("/") ?: ""

    val configViewModel: ConfigViewModel = viewModel()
    val perfilState by configViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(token) {
        if (token.isNotEmpty() && email.isNotEmpty()) {
            configViewModel.cargarPerfil(token, email)
        }
    }

    val nombreCorto = if (perfilState is ConfigUiState.Success) {
        val perfil = (perfilState as ConfigUiState.Success).perfil
        perfil?.nombre?.takeIf { it.isNotEmpty() } ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
    } else {
        email.substringBefore("@").replaceFirstChar { it.uppercase() }
    }
    val iniciales = nombreCorto.take(2).uppercase()

    val bottomItems = listOf(
        BottomNavItem("Inicio",   Icons.Filled.Home,          Screen.Home.route.substringBefore("/")),
        BottomNavItem("Opera",    Icons.Filled.SwapHoriz,     Screen.Opera.route.substringBefore("/")),
        BottomNavItem("Para mí",  Icons.Filled.Add,           Screen.Cuenta.route.substringBefore("/"), isCentral = true),
        BottomNavItem("Notifica", Icons.Filled.Notifications, Screen.Notifica.route.substringBefore("/")),
        BottomNavItem("Contacto", Icons.Filled.HeadsetMic,    Screen.Contacto.route)
    )

    val drawerItems = listOf(
        DrawerItem("Configuración",          Icons.Outlined.Settings,       {
            navController.navigate(Screen.Configuracion.createRoute(token, email)) {
                launchSingleTop = true
            }
        }),
        DrawerItem("Seguridad y privacidad", Icons.Outlined.Lock,           {}),
        DrawerItem("Tu ahorro energético",   Icons.Outlined.Bolt,           {}),
        DrawerItem("Hacer una operación",    Icons.Outlined.AccountBalanceWallet, {}),
        DrawerItem("Experiencias",           Icons.Outlined.Explore,        {}),
        DrawerItem("Oficinas y cajeros",     Icons.Outlined.LocationOn,     {}),
        DrawerItem("Invita a un amigo",      Icons.Outlined.PersonAdd,      {}),
        DrawerItem("Mis promociones",        Icons.Outlined.LocalOffer,     {}),
        DrawerItem("Valorar la app",         Icons.Outlined.StarRate,       {}),
        DrawerItem("Acerca de",              Icons.Outlined.Info,           {}),
        DrawerItem("Salir",                  Icons.Outlined.ArrowBackIosNew, onLogout, isDestructive = true)
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState   = drawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    DrawerContent(
                        nombreCorto = nombreCorto,
                        iniciales   = iniciales,
                        items       = drawerItems,
                        onClose     = { scope.launch { drawerState.close() } }
                    )
                }
            },
            scrimColor = Color.Black.copy(alpha = 0.5f)
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Scaffold(
                    containerColor = BbvaGrisFondo,
                    bottomBar = {
                        BottomNavBar(
                            items        = bottomItems,
                            currentRoute = currentRoute,
                            onItemClick  = { item ->
                                if (!item.isCentral) {
                                    val route = when (item.label) {
                                        "Inicio"   -> Screen.Home.createRoute(token, email)
                                        "Opera"    -> Screen.Opera.createRoute(token)
                                        "Notifica" -> Screen.Notifica.createRoute(token)
                                        "Contacto" -> Screen.Contacto.route
                                        else       -> null
                                    }
                                    route?.let {
                                        navController.navigate(it) {
                                            popUpTo(Screen.Home.route.substringBefore("/")) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState    = true
                                        }
                                    }
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    content(paddingValues)
                }
            }
        }
    }
}

@Composable
private fun BottomNavBar(
    items: List<BottomNavItem>,
    currentRoute: String,
    onItemClick: (BottomNavItem) -> Unit
) {
    NavigationBar(
        containerColor = BbvaBlanco,
        tonalElevation = 4.dp,
        modifier       = Modifier.height(64.dp)
    ) {
        items.forEach { item ->
            val selected = currentRoute.startsWith(item.route.substringBefore("/"))
            if (item.isCentral) {
                NavigationBarItem(
                    selected = false,
                    onClick  = { onItemClick(item) },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(BbvaAzulPrimario, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector        = item.icon,
                                contentDescription = item.label,
                                tint               = BbvaBlanco,
                                modifier           = Modifier.size(24.dp)
                            )
                        }
                    },
                    label  = { Text(item.label, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = BbvaGrisTexto,
                        unselectedTextColor = BbvaGrisTexto,
                        indicatorColor      = Color.Transparent
                    )
                )
            } else {
                NavigationBarItem(
                    selected = selected,
                    onClick  = { onItemClick(item) },
                    icon = {
                        Icon(
                            imageVector        = item.icon,
                            contentDescription = item.label,
                            modifier           = Modifier.size(22.dp)
                        )
                    },
                    label  = { Text(item.label, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor   = BbvaAzulPrimario,
                        selectedTextColor   = BbvaAzulPrimario,
                        unselectedIconColor = BbvaGrisTexto,
                        unselectedTextColor = BbvaGrisTexto,
                        indicatorColor      = BbvaAzulPrimario.copy(alpha = 0.1f)
                    )
                )
            }
        }
    }
}

@Composable
private fun DrawerContent(
    nombreCorto: String,
    iniciales: String,
    items: List<DrawerItem>,
    onClose: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = BbvaBlanco,
        modifier             = Modifier.width(300.dp)
    ) {
        // Header del drawer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BbvaAzulPrimario)
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text       = nombreCorto,
                        color      = BbvaBlanco,
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text     = "Mi perfil",
                        color    = BbvaBlanco.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { onClose() }.padding(vertical = 4.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(BbvaBlanco.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = iniciales,
                        color      = BbvaBlanco,
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        item.onClick()
                        if (!item.isDestructive) onClose()
                    }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector        = item.icon,
                    contentDescription = item.label,
                    tint               = if (item.isDestructive) BbvaRojoSalir else BbvaAzulPrimario,
                    modifier           = Modifier.size(20.dp)
                )
                Text(
                    text       = item.label,
                    color      = if (item.isDestructive) BbvaRojoSalir else BbvaNegro,
                    fontSize   = 14.sp,
                    fontWeight = if (item.isDestructive) FontWeight.Bold else FontWeight.Normal
                )
            }

            if (item.label == "Hacer una operación" || item.label == "Acerca de") {
                HorizontalDivider(
                    color     = BbvaGrisBorde,
                    thickness = 0.5.dp,
                    modifier  = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}