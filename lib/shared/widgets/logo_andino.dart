import 'package:flutter/material.dart';

/// Isotipo de Banco Andino: flor de 6 petalos multicolor.
/// Dibujado con CustomPaint para poder rotarlo en el splash/carga.
class LogoAndino extends StatelessWidget {
  final double size;
  const LogoAndino({super.key, this.size = 96});

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: size,
      height: size,
      child: Image.asset(
        'assets/logo.jpg',
        fit: BoxFit.contain,
      ),
    );
  }
}
