import 'package:flutter/material.dart';

/// Paleta de marca — BBVA.
class AppColors {
  AppColors._();

  // Marca principal BBVA
  static const Color primary = Color(0xFF004481); // Core Blue
  static const Color primaryDark = Color(0xFF072146); // Navy
  static const Color secondary = Color(0xFF1464A5); // Light Blue
  static const Color accent = Color(0xFF2DCCCD); // Aqua

  // Colores secundarios BBVA
  static const Color logoMagenta = Color(0xFFE6007E);
  static const Color logoRojo = Color(0xFFD81E05); // Core Red
  static const Color logoNaranja = Color(0xFFF39200);
  static const Color logoAmarillo = Color(0xFFFFD500);
  static const Color logoVerde = Color(0xFF95C11F);
  static const Color logoRosa = Color(0xFFEC619F);

  /// Degradado de marca: mezcla de los colores del logo de BBVA
  static const List<Color> brandGradient = [
    Color(0xFF072146), // Navy
    Color(0xFF004481), // Core Blue
    Color(0xFF1464A5), // Light Blue
    Color(0xFF2DCCCD), // Aqua
  ];

  // Superficies
  static const Color background = Color(0xFFF4F4F4);
  static const Color surface = Colors.white;
  static const Color visitedTile = Color(0xFFE0E0E0);

  // Texto
  static const Color textPrimary = Color(0xFF121212);
  static const Color textSecondary = Color(0xFF666666);
  static const Color onPrimary = Colors.white;

  // Estados / semaforo
  static const Color success = Color(0xFF48AE64);
  static const Color warning = Color(0xFFF8CD51);
  static const Color danger = Color(0xFFD81E05);
  static const Color info = Color(0xFF2DCCCD);
  static const Color neutral = Color(0xFFD3D3D3);

  // Tipos de gestion de cartera (RF-10) — semanticos
  static const Color renovacion = Color(0xFF1464A5);
  static const Color ampliacion = Color(0xFF48AE64);
  static const Color nuevaSolicitud = Color(0xFFF39200);
  static const Color seguimiento = Color(0xFF666666);
  static const Color recuperacionMora = Color(0xFFD81E05);
  static const Color desertor = Color(0xFF072146);

  // Prioridad
  static const Color prioridadAlta = recuperacionMora;
  static const Color prioridadMedia = warning;
  static const Color prioridadNormal = success;
}
