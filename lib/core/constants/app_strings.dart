/// Textos y etiquetas centralizados.
///
/// Mantener los literales aqui facilita el branding por entidad y futuras
/// traducciones. Agrupados por modulo.
class AppStrings {
  AppStrings._();

  // App / marca
  static const String appName = 'BBVA — Fuerza de Ventas';
  static const String entidad = 'BBVA';

  // Auth (M0)
  static const String loginTitle = 'Iniciar sesion';
  static const String correoAsesor = 'Correo electrónico';
  static const String password = 'Contrasena';
  static const String ingresar = 'Ingresar';
  static const String problemasIngresar = 'Problemas para ingresar';
  static const String cerrarSesion = 'Cerrar sesion';
  static const String bloqueoIntentos =
      'Demasiados intentos fallidos. Intenta de nuevo en';

  // Cartera (M1)
  static const String carteraTitle = 'Mi cartera del dia';
  static const String actualizar = 'Actualizar';
  static const String sinClientes = 'No hay clientes en tu cartera hoy.';
  static const String buscarCliente = 'Buscar por nombre o documento';
  static const String ultimaActualizacion = 'Ultima actualizacion';

  // Offline
  static const String modoOffline = 'Modo offline — mostrando datos en cache';
  static const String sinSincronizar = 'solicitudes sin sincronizar';

  // Generico
  static const String reintentar = 'Reintentar';
  static const String cancelar = 'Cancelar';
  static const String aceptar = 'Aceptar';
  static const String errorGenerico = 'Ocurrio un error. Intenta nuevamente.';
}
