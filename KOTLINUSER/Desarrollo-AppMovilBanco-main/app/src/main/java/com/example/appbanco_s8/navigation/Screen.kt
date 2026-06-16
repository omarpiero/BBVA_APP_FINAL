
package com.example.appbanco_s8.navigation

sealed class Screen(val route: String) {
    object Login     : Screen("login")
    object Home      : Screen("home/{token}/{email}") {
        fun createRoute(token: String, email: String) = "home/$token/$email"
    }
    object Cuenta    : Screen("cuenta/{token}") {
        fun createRoute(token: String) = "cuenta/$token"
    }
    object Tarjeta   : Screen("tarjeta/{token}") {
        fun createRoute(token: String) = "tarjeta/$token"
    }
    object Prestamo  : Screen("prestamo/{token}") {
        fun createRoute(token: String) = "prestamo/$token"
    }
    object Opera     : Screen("opera/{token}") {
        fun createRoute(token: String) = "opera/$token"
    }
    object Notifica  : Screen("notifica/{token}") {
        fun createRoute(token: String) = "notifica/$token"
    }
    object Contacto  : Screen("contacto")
    object Perfil    : Screen("perfil/{token}") {
        fun createRoute(token: String) = "perfil/$token"
    }
    object Configuracion : Screen("configuracion/{token}/{email}") {
        fun createRoute(token: String, email: String) = "configuracion/$token/$email"
    }

    // ── Nuevas rutas Sprint 1: Flujo PLIN ────────────────────
    object Plin : Screen("plin/{token}") {
        fun createRoute(token: String) = "plin/$token"
    }
    object PagarEnLinea : Screen("pagar_en_linea/{token}/{comercioId}/{comercioNombre}/{monto}") {
        fun createRoute(token: String, comercioId: String, comercioNombre: String, monto: Double) =
            "pagar_en_linea/$token/$comercioId/$comercioNombre/$monto"
    }
    object ConfirmacionPago : Screen("confirmacion_pago/{token}/{comercioNombre}/{monto}/{numeroOperacion}/{cuentaDisplay}/{emailUser}") {
        fun createRoute(
            token: String,
            comercioNombre: String,
            monto: Double,
            numeroOperacion: String,
            cuentaDisplay: String,
            emailUser: String
        ) = "confirmacion_pago/$token/$comercioNombre/$monto/$numeroOperacion/$cuentaDisplay/$emailUser"
    }
}