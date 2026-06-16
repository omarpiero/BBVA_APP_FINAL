package com.example.appbanco_s8.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appbanco_s8.ui.viewmodel.AuthUiState
import com.example.appbanco_s8.ui.viewmodel.AuthViewModel

// ── Paleta local (referencia los valores de Color.kt) ───────
private val AzulMarino  = Color(0xFF020B18)
private val AzulMedio   = Color(0xFF041B3B)
private val AzulBanco   = Color(0xFF1A5DC8)
private val AzulClaro   = Color(0xFF1E3A6E)
private val DoradoBanco = Color(0xFFF5C842)
private val GrisTexto   = Color(0xFFB0B8C8)
private val GrisBorde   = Color(0xFF1C2E4A)

@Composable
fun LoginScreen(
    onLoginSuccess: (token: String, email: String) -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Navegar cuando el login es exitoso
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            val s = uiState as AuthUiState.Success
            onLoginSuccess(s.token, s.email)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AzulMarino, AzulMedio, AzulMarino),
                    startY = 0f,
                    endY   = Float.POSITIVE_INFINITY
                )
            )
    ) {
        // ── Franja decorativa superior ──────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(AzulBanco.copy(alpha = 0.25f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Logo / nombre del banco ─────────────────────
            AnimatedVisibility(
                visible = true,
                enter   = fadeIn() + slideInVertically(initialOffsetY = { -40 })
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    // Ícono M estilizado
                    Surface(
                        shape           = RoundedCornerShape(20.dp),
                        color           = AzulBanco,
                        modifier        = Modifier.size(72.dp),
                        shadowElevation = 12.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text       = "B",
                                fontSize   = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text          = "BBVA",
                        fontSize      = 28.sp,
                        fontWeight    = FontWeight.Bold,
                        color         = Color.White,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text          = "PORTAL FINANCIERO",
                        fontSize      = 11.sp,
                        color         = GrisTexto,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            // ── Card de login ───────────────────────────────
            Surface(
                shape          = RoundedCornerShape(24.dp),
                color          = AzulMedio,
                modifier       = Modifier.fillMaxWidth(),
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier            = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Text(
                        text       = "Iniciar sesión",
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )

                    // ── Campo email ─────────────────────────
                    OutlinedTextField(
                        value         = email,
                        onValueChange = { email = it },
                        label         = { Text("Correo electrónico", color = GrisTexto) },
                        leadingIcon   = {
                            Icon(
                                imageVector        = Icons.Default.Email,
                                contentDescription = null,
                                tint               = GrisTexto
                            )
                        },
                        singleLine      = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction    = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = OutlinedTextFieldDefaults.colors(
                            focusedTextColor        = Color.White,
                            unfocusedTextColor      = Color.White,
                            focusedBorderColor      = AzulBanco,
                            unfocusedBorderColor    = GrisBorde,
                            cursorColor             = AzulBanco,
                            focusedContainerColor   = AzulClaro.copy(alpha = 0.3f),
                            unfocusedContainerColor = Color.Transparent
                        )
                    )

                    // ── Campo contraseña ────────────────────
                    OutlinedTextField(
                        value         = password,
                        onValueChange = { password = it },
                        label         = { Text("Contraseña", color = GrisTexto) },
                        leadingIcon   = {
                            Icon(
                                imageVector        = Icons.Default.Lock,
                                contentDescription = null,
                                tint               = GrisTexto
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible)
                                        Icons.Default.VisibilityOff
                                    else
                                        Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible)
                                        "Ocultar contraseña"
                                    else
                                        "Mostrar contraseña",
                                    tint = GrisTexto
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        singleLine      = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction    = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.login(email.trim(), password)
                            }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = OutlinedTextFieldDefaults.colors(
                            focusedTextColor        = Color.White,
                            unfocusedTextColor      = Color.White,
                            focusedBorderColor      = AzulBanco,
                            unfocusedBorderColor    = GrisBorde,
                            cursorColor             = AzulBanco,
                            focusedContainerColor   = AzulClaro.copy(alpha = 0.3f),
                            unfocusedContainerColor = Color.Transparent
                        )
                    )

                    // ── Mensaje de error ────────────────────
                    AnimatedVisibility(visible = uiState is AuthUiState.Error) {
                        Surface(
                            shape    = RoundedCornerShape(8.dp),
                            color    = Color(0xFFE74C3C).copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text     = (uiState as? AuthUiState.Error)?.mensaje ?: "",
                                color    = Color(0xFFE74C3C),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // ── Botón ingresar ──────────────────────
                    Button(
                        onClick  = {
                            focusManager.clearFocus()
                            viewModel.login(email.trim(), password)
                        },
                        enabled  = uiState !is AuthUiState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape  = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor         = AzulBanco,
                            contentColor           = Color.White,
                            disabledContainerColor = AzulBanco.copy(alpha = 0.5f)
                        )
                    ) {
                        if (uiState is AuthUiState.Loading) {
                            CircularProgressIndicator(
                                color       = Color.White,
                                modifier    = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text       = "Ingresar",
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // ── ¿Olvidaste tu contraseña? ───────────
                    TextButton(
                        onClick  = { /* TODO: pantalla de recuperación */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text      = "¿Olvidaste tu contraseña?",
                            color     = AzulBanco,
                            fontSize  = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Pie de pantalla ─────────────────────────────
            Text(
                text          = "SEGURO · CONFIABLE · DIGITAL",
                fontSize      = 10.sp,
                color         = GrisTexto.copy(alpha = 0.5f),
                letterSpacing = 1.5.sp,
                textAlign     = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            // Punto dorado decorativo
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(DoradoBanco, shape = RoundedCornerShape(50))
            )
        }
    }
}

