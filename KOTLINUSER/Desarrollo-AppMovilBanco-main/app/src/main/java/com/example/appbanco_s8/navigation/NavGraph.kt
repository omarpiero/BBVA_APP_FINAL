package com.example.appbanco_s8.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.appbanco_s8.ui.components.AppScaffold
import com.example.appbanco_s8.ui.screens.*

@Composable
fun AppNavGraph(navController: NavHostController) {

    var tokenGlobal by remember { mutableStateOf("") }
    var emailGlobal by remember { mutableStateOf("") }

    // ── Lambda de logout reutilizable ────────────────────────
    val doLogout: () -> Unit = {
        tokenGlobal = ""
        emailGlobal = ""
        navController.navigate(Screen.Login.route) {
            popUpTo(0) { inclusive = true }
        }
    }

    NavHost(
        navController    = navController,
        startDestination = Screen.Login.route
    ) {

        // ── Login — sin scaffold ─────────────────────────────
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { token, email ->
                    tokenGlobal = token
                    emailGlobal = email
                    navController.navigate(Screen.Home.createRoute(token, email)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Home ─────────────────────────────────────────────────────
        composable(
            route     = Screen.Home.route,
            arguments = listOf(
                navArgument("token") { type = NavType.StringType },
                navArgument("email") { type = NavType.StringType }
            )
        ) { back ->
            val token = back.arguments?.getString("token") ?: tokenGlobal
            val email = back.arguments?.getString("email") ?: emailGlobal
            if (token.isNotEmpty()) {
                tokenGlobal = token
                emailGlobal = email
            }

            // openDrawer guarda la función que AppScaffold expone
            var openDrawer by remember { mutableStateOf<(() -> Unit)?>(null) }

            AppScaffold(
                token         = tokenGlobal,
                email         = emailGlobal,
                navController = navController,
                onLogout      = doLogout,
                onOpenDrawer  = { fn -> openDrawer = fn }
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    HomeScreen(
                        token         = tokenGlobal,
                        email         = emailGlobal,
                        navController = navController,
                        onLogout      = doLogout,
                        onMenuClick   = { openDrawer?.invoke() }
                    )
                }
            }
        }

        // ── Cuenta ───────────────────────────────────────────
        composable(
            route     = Screen.Cuenta.route,
            arguments = listOf(navArgument("token") { type = NavType.StringType })
        ) { back ->
            val token = back.arguments?.getString("token") ?: tokenGlobal
            AppScaffold(
                token         = tokenGlobal,
                email         = emailGlobal,
                navController = navController,
                onLogout      = doLogout
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    CuentaScreen(token = token, navController = navController)
                }
            }
        }

        // ── Tarjeta ──────────────────────────────────────────
        composable(
            route     = Screen.Tarjeta.route,
            arguments = listOf(navArgument("token") { type = NavType.StringType })
        ) { back ->
            val token = back.arguments?.getString("token") ?: tokenGlobal
            AppScaffold(
                token         = tokenGlobal,
                email         = emailGlobal,
                navController = navController,
                onLogout      = doLogout
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    TarjetaScreen(token = token, navController = navController)
                }
            }
        }

        // ── Prestamo ─────────────────────────────────────────
        composable(
            route     = Screen.Prestamo.route,
            arguments = listOf(navArgument("token") { type = NavType.StringType })
        ) { back ->
            val token = back.arguments?.getString("token") ?: tokenGlobal
            AppScaffold(
                token         = tokenGlobal,
                email         = emailGlobal,
                navController = navController,
                onLogout      = doLogout
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    PrestamoScreen(token = token, navController = navController)
                }
            }
        }

        // ── Opera ────────────────────────────────────────────
        composable(
            route     = Screen.Opera.route,
            arguments = listOf(navArgument("token") { type = NavType.StringType })
        ) { back ->
            val token = back.arguments?.getString("token") ?: tokenGlobal
            AppScaffold(
                token         = tokenGlobal,
                email         = emailGlobal,
                navController = navController,
                onLogout      = doLogout
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    OperaScreen(token = token, navController = navController)
                }
            }
        }

        // ── Notifica ─────────────────────────────────────────
        composable(
            route     = Screen.Notifica.route,
            arguments = listOf(navArgument("token") { type = NavType.StringType })
        ) { back ->
            val token = back.arguments?.getString("token") ?: tokenGlobal
            AppScaffold(
                token         = tokenGlobal,
                email         = emailGlobal,
                navController = navController,
                onLogout      = doLogout
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    NotificaScreen(token = token, navController = navController)
                }
            }
        }

        // ── Contacto ─────────────────────────────────────────
        composable(Screen.Contacto.route) {
            AppScaffold(
                token         = tokenGlobal,
                email         = emailGlobal,
                navController = navController,
                onLogout      = doLogout
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    ContactoScreen(navController = navController)
                }
            }
        }

        // ── Perfil ───────────────────────────────────────────
        composable(
            route     = Screen.Perfil.route,
            arguments = listOf(navArgument("token") { type = NavType.StringType })
        ) { back ->
            val token = back.arguments?.getString("token") ?: tokenGlobal
            AppScaffold(
                token         = tokenGlobal,
                email         = emailGlobal,
                navController = navController,
                onLogout      = doLogout
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    PerfilScreen(token = token, navController = navController)
                }
            }
        }

        // ── Configuración ────────────────────────────────────
        composable(
            route     = Screen.Configuracion.route,
            arguments = listOf(
                navArgument("token") { type = NavType.StringType },
                navArgument("email") { type = NavType.StringType }
            )
        ) { back ->
            val token = back.arguments?.getString("token") ?: tokenGlobal
            val email = back.arguments?.getString("email") ?: emailGlobal
            ConfigScreen(
                token = token,
                email = email,
                navController = navController
            )
        }

        // ══════════════════════════════════════════════════════
        // ── NUEVAS RUTAS SPRINT 1: Flujo PLIN ────────────────
        // ══════════════════════════════════════════════════════

        // ── PLIN (sin bottom nav — pantalla propia) ──────────
        composable(
            route     = Screen.Plin.route,
            arguments = listOf(navArgument("token") { type = NavType.StringType })
        ) { back ->
            val token = back.arguments?.getString("token") ?: tokenGlobal
            PlinScreen(
                token         = token,
                navController = navController
            )
        }

        // ── Pagar en Línea ───────────────────────────────────
        composable(
            route     = Screen.PagarEnLinea.route,
            arguments = listOf(
                navArgument("token")          { type = NavType.StringType },
                navArgument("comercioId")     { type = NavType.StringType },
                navArgument("comercioNombre") { type = NavType.StringType },
                navArgument("monto")          { type = NavType.StringType }
            )
        ) { back ->
            val token          = back.arguments?.getString("token") ?: tokenGlobal
            val comercioId     = back.arguments?.getString("comercioId") ?: ""
            val comercioNombre = back.arguments?.getString("comercioNombre") ?: ""
            val monto          = back.arguments?.getString("monto")?.toDoubleOrNull() ?: 0.0
            PagarEnLineaScreen(
                token          = token,
                comercioId     = comercioId,
                comercioNombre = comercioNombre,
                monto          = monto,
                email          = emailGlobal,
                navController  = navController
            )
        }

        // ── Confirmación de Pago ─────────────────────────────
        composable(
            route     = Screen.ConfirmacionPago.route,
            arguments = listOf(
                navArgument("token")            { type = NavType.StringType },
                navArgument("comercioNombre")   { type = NavType.StringType },
                navArgument("monto")            { type = NavType.StringType },
                navArgument("numeroOperacion")  { type = NavType.StringType },
                navArgument("cuentaDisplay")    { type = NavType.StringType },
                navArgument("emailUser")        { type = NavType.StringType }
            )
        ) { back ->
            val token            = back.arguments?.getString("token") ?: tokenGlobal
            val comercioNombre   = back.arguments?.getString("comercioNombre") ?: ""
            val monto            = back.arguments?.getString("monto")?.toDoubleOrNull() ?: 0.0
            val numeroOperacion  = back.arguments?.getString("numeroOperacion") ?: ""
            val cuentaDisplay    = back.arguments?.getString("cuentaDisplay") ?: ""
            val emailUser        = back.arguments?.getString("emailUser") ?: emailGlobal
            ConfirmacionPagoScreen(
                token           = token,
                comercioNombre  = comercioNombre,
                monto           = monto,
                numeroOperacion = numeroOperacion,
                cuentaDisplay   = cuentaDisplay,
                emailUser       = emailUser,
                navController   = navController
            )
        }
    }
}