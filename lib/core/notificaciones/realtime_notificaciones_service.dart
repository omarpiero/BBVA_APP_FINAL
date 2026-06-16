import 'package:supabase_flutter/supabase_flutter.dart';
import 'notificacion_service.dart';

/// Servicio para escuchar notificaciones Push simuladas via Supabase Realtime.
/// Se suscribe a la tabla `notificaciones` y dispara una alerta local cuando
/// se inserta un nuevo registro para este asesor.
class RealtimeNotificacionesService {
  RealtimeNotificacionesService._();
  
  static RealtimeChannel? _canal;

  /// Inicia la escucha de notificaciones para un [asesorId] especifico.
  static void iniciar(String asesorId) {
    detener(); // Detener si habia una escucha previa

    final supabase = Supabase.instance.client;
    
    _canal = supabase.channel('public:notificaciones:asesor_$asesorId');
    _canal!.onPostgresChanges(
      event: PostgresChangeEvent.insert,
      schema: 'public',
      table: 'notificaciones',
      filter: PostgresChangeFilter(
        type: PostgresChangeFilterType.eq,
        column: 'asesor_id',
        value: asesorId,
      ),
      callback: (payload) {
        final data = payload.newRecord;
        final titulo = data['titulo'] as String?;
        final cuerpo = data['cuerpo'] as String?;
        
        if (titulo != null && cuerpo != null) {
          NotificacionService.mostrarPushLocal(titulo, cuerpo);
          
          // Opcional: Marcar como leida automaticamente o dejarlo para una UI
          // supabase.from('notificaciones').update({'leida': true}).eq('id', data['id']);
        }
      },
    ).subscribe();
  }

  /// Detiene la escucha (ej. al cerrar sesion).
  static void detener() {
    _canal?.unsubscribe();
    _canal = null;
  }
}
