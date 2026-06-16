package com.example.appbanco_s8.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.appbanco_s8.ui.viewmodel.ConfigUiState
import com.example.appbanco_s8.ui.viewmodel.ConfigViewModel
import kotlinx.coroutines.launch

private val AzulMarino  = Color(0xFF020C1B)
private val AzulMedio   = Color(0xFF04132B)
private val AzulBanco   = Color(0xFF84DFE2)
private val GrisTexto   = Color(0xFFB0B8C8)
private val GrisBorde   = Color(0xFF1C2E4A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    token: String,
    email: String,
    navController: NavHostController,
    viewModel: ConfigViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var nombre by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // We assume the user ID is part of the JWT payload, but for this demo 
    // we might need to extract it or we just use email as a fallback ID.
    // In a real app, token parsing provides the sub (userId). Let's use email for now to simulate.
    val userIdMock = email

    LaunchedEffect(Unit) {
        viewModel.cargarPerfil(token, userIdMock)
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ConfigUiState.Success -> {
                val perfil = (uiState as ConfigUiState.Success).perfil
                if (perfil != null && nombre.isEmpty()) {
                    nombre = perfil.nombre ?: ""
                }
            }
            is ConfigUiState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar((uiState as ConfigUiState.Error).mensaje)
                }
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = AzulMarino,
        topBar = {
            TopAppBar(
                title = { Text("Configuración", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulMedio)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Actualizar Perfil",
                fontSize = 22.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Este nombre se mostrará en todas las interfaces y reemplazará al nombre de usuario por defecto.",
                fontSize = 14.sp,
                color = GrisTexto,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre de usuario", color = GrisTexto) },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = GrisTexto)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AzulBanco,
                    unfocusedBorderColor = GrisBorde,
                    cursorColor = AzulBanco,
                    focusedContainerColor = AzulMedio,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.guardarPerfil(token, userIdMock, nombre, email)
                    scope.launch {
                        snackbarHostState.showSnackbar("Perfil actualizado. Los cambios se verán reflejados al recargar.")
                    }
                },
                enabled = uiState !is ConfigUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulBanco,
                    contentColor = Color.Black
                )
            ) {
                if (uiState is ConfigUiState.Loading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar Cambios", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
