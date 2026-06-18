import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:pdf/pdf.dart';
import 'package:pdf/widgets.dart' as pw;
import 'package:printing/printing.dart';

import '../../../core/constants/app_colors.dart';
import '../../../shared/utils/formatters.dart';
import '../../auth/presentation/login_viewmodel.dart';
import '../../solicitud/data/solicitud_repository.dart';
import '../../solicitud/domain/solicitud_model.dart';
import '../../solicitud/presentation/solicitud_viewmodel.dart';

/// M9 — Tablero de estado de solicitudes (HU-27/28).
class EstadoScreen extends ConsumerWidget {
  const EstadoScreen({super.key});

  static const _tabs = <String, List<String>>{
    'Enviadas': ['enviado'],
    'En comite': ['recibido_comite', 'en_evaluacion'],
    'Aprobadas': ['aprobado', 'condicionado'],
    'Desembolsadas': ['desembolsado'],
    'Rechazadas': ['rechazado'],
  };

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final async = ref.watch(solicitudesHistorialProvider);

    return async.when(
      loading: () =>
          const Scaffold(body: Center(child: CircularProgressIndicator())),
      error: (_, __) => const Scaffold(
          body: Center(child: Text('No se pudo cargar el tablero.'))),
      data: (lista) => DefaultTabController(
        length: _tabs.length,
        child: Scaffold(
          appBar: AppBar(
            backgroundColor: Colors.transparent,
            surfaceTintColor: Colors.transparent,
            elevation: 0,
            foregroundColor: AppColors.onPrimary,
            iconTheme: const IconThemeData(color: AppColors.onPrimary),
            title: const Text('Estado de solicitudes',
                style: TextStyle(color: AppColors.onPrimary)),
            flexibleSpace: Container(
              decoration: const BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: AppColors.brandGradient,
                ),
              ),
            ),
            bottom: TabBar(
              isScrollable: true,
              indicatorColor: Colors.white,
              labelColor: Colors.white,
              unselectedLabelColor: Colors.white70,
              tabs: _tabs.entries.map((e) {
                final n = lista.where((s) => e.value.contains(s.estado)).length;
                return Tab(text: '${e.key} ($n)'); // contador por pestana
              }).toList(),
            ),
          ),
          body: TabBarView(
            children: _tabs.values.map((estados) {
              final items =
                  lista.where((s) => estados.contains(s.estado)).toList();
              if (items.isEmpty) {
                return const Center(child: Text('Sin solicitudes.'));
              }
              return RefreshIndicator(
                onRefresh: () async =>
                    ref.invalidate(solicitudesHistorialProvider),
                child: ListView.builder(
                  itemCount: items.length,
                  itemBuilder: (_, i) => _SolicitudCard(s: items[i]),
                ),
              );
            }).toList(),
          ),
        ),
      ),
    );
  }
}

class _EstadoAction {
  final String estado;
  final String label;
  final IconData icon;

  const _EstadoAction(this.estado, this.label, this.icon);
}

class _EstadoActions extends StatelessWidget {
  final List<_EstadoAction> acciones;
  final String? estadoEnProceso;
  final ValueChanged<String> onPressed;

  const _EstadoActions({
    required this.acciones,
    required this.estadoEnProceso,
    required this.onPressed,
  });

  @override
  Widget build(BuildContext context) {
    if (acciones.isEmpty) {
      return const Text(
        'No hay acciones pendientes para este estado.',
        style: TextStyle(color: AppColors.textSecondary),
      );
    }

    return Wrap(
      spacing: 8,
      runSpacing: 8,
      children: acciones.map((accion) {
        final procesando = estadoEnProceso == accion.estado;
        return FilledButton.icon(
          icon: procesando
              ? const SizedBox(
                  width: 16,
                  height: 16,
                  child: CircularProgressIndicator(strokeWidth: 2),
                )
              : Icon(accion.icon, size: 18),
          label: Text(accion.label),
          onPressed:
              estadoEnProceso == null ? () => onPressed(accion.estado) : null,
        );
      }).toList(),
    );
  }
}

class _InfoChip extends StatelessWidget {
  final IconData icon;
  final String label;

  const _InfoChip({required this.icon, required this.label});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: AppColors.primary.withValues(alpha: 0.08),
        borderRadius: BorderRadius.circular(6),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 16, color: AppColors.primary),
          const SizedBox(width: 6),
          Text(label, style: const TextStyle(fontSize: 12)),
        ],
      ),
    );
  }
}

class _DecisionBox extends StatelessWidget {
  final IconData icon;
  final Color color;
  final String title;
  final String body;

  const _DecisionBox({
    required this.icon,
    required this.color,
    required this.title,
    required this.body,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.10),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: color.withValues(alpha: 0.30)),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(icon, color: color, size: 20),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: TextStyle(color: color, fontWeight: FontWeight.w700),
                ),
                const SizedBox(height: 4),
                Text(body),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

Color _colorEstado(String estado) {
  switch (estado) {
    case 'aprobado':
    case 'desembolsado':
      return AppColors.success;
    case 'rechazado':
      return AppColors.danger;
    case 'condicionado':
      return AppColors.warning;
    default:
      return AppColors.info;
  }
}

class _SolicitudCard extends StatelessWidget {
  final SolicitudResumen s;
  const _SolicitudCard({required this.s});

  @override
  Widget build(BuildContext context) {
    final color = _colorEstado(s.estado);
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 5),
      child: ListTile(
        title: Text(s.clienteNombre,
            style: const TextStyle(fontWeight: FontWeight.w600)),
        subtitle: Text(
            '${s.numeroExpediente} · ${Formatters.soles(s.montoSolicitado)}'),
        trailing: Container(
          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
          decoration: BoxDecoration(
            color: color.withValues(alpha: 0.12),
            borderRadius: BorderRadius.circular(6),
          ),
          child: Text(s.estado.toUpperCase(),
              style: TextStyle(
                  color: color, fontSize: 10, fontWeight: FontWeight.w700)),
        ),
        onTap: () => showModalBottomSheet<void>(
          context: context,
          isScrollControlled: true,
          builder: (_) => _DetalleSheet(s: s),
        ),
      ),
    );
  }
}

/// Detalle con linea de tiempo, notas internas (RF-72) y compartir PDF (RF-71).
class _DetalleSheet extends ConsumerStatefulWidget {
  final SolicitudResumen s;
  const _DetalleSheet({required this.s});
  @override
  ConsumerState<_DetalleSheet> createState() => _DetalleSheetState();
}

class _DetalleSheetState extends ConsumerState<_DetalleSheet> {
  final _nota = TextEditingController();
  bool _guardando = false;
  String? _estadoEnProceso;

  @override
  void dispose() {
    _nota.dispose();
    super.dispose();
  }

  Future<void> _agregarNota() async {
    if (_nota.text.trim().isEmpty) return;
    setState(() => _guardando = true);
    final asesor = ref.read(loginViewModelProvider).asesor;
    await ref
        .read(solicitudRepositoryProvider)
        .agregarNota(widget.s.id, _nota.text.trim(), asesor!.id);
    _nota.clear();
    ref.invalidate(notasProvider(widget.s.id));
    if (mounted) setState(() => _guardando = false);
  }

  Future<void> _cambiarEstado(String estado) async {
    setState(() => _estadoEnProceso = estado);
    try {
      await ref.read(solicitudRepositoryProvider).actualizarEstado(
            solicitudId: widget.s.id,
            estado: estado,
            montoAprobado: _requiereMonto(estado)
                ? (widget.s.montoAprobado > 0
                    ? widget.s.montoAprobado
                    : widget.s.montoSolicitado)
                : null,
            condicionAdicional: estado == 'condicionado'
                ? 'Validar sustento adicional de ingresos del negocio.'
                : null,
            motivoRechazo: estado == 'rechazado'
                ? 'No cumple politica crediticia segun evaluacion de campo.'
                : null,
          );
      ref.invalidate(solicitudesHistorialProvider);
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Solicitud actualizada a $estado')),
      );
      Navigator.of(context).pop();
    } catch (_) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('No se pudo actualizar la solicitud en Supabase.'),
        ),
      );
    } finally {
      if (mounted) setState(() => _estadoEnProceso = null);
    }
  }

  bool _requiereMonto(String estado) =>
      estado == 'aprobado' ||
      estado == 'desembolsado' ||
      estado == 'condicionado';

  List<_EstadoAction> _accionesDisponibles(SolicitudResumen s) {
    switch (s.estado) {
      case 'enviado':
        return const [
          _EstadoAction('recibido_comite', 'Recibir', Icons.inbox_rounded),
          _EstadoAction('rechazado', 'Rechazar', Icons.block_rounded),
        ];
      case 'recibido_comite':
        return const [
          _EstadoAction('en_evaluacion', 'Evaluar', Icons.fact_check_rounded),
          _EstadoAction(
              'condicionado', 'Condicionar', Icons.assignment_late_rounded),
          _EstadoAction('rechazado', 'Rechazar', Icons.block_rounded),
        ];
      case 'en_evaluacion':
        return const [
          _EstadoAction('aprobado', 'Aprobar', Icons.verified_rounded),
          _EstadoAction(
              'condicionado', 'Condicionar', Icons.assignment_late_rounded),
          _EstadoAction('rechazado', 'Rechazar', Icons.block_rounded),
        ];
      case 'aprobado':
      case 'condicionado':
        return const [
          _EstadoAction('desembolsado', 'Desembolsar', Icons.payments_rounded),
        ];
      default:
        return const [];
    }
  }

  Future<void> _compartirPdf() async {
    final s = widget.s;
    final doc = pw.Document();
    doc.addPage(
      pw.Page(
        pageFormat: PdfPageFormat.a4,
        build: (_) => pw.Column(
          crossAxisAlignment: pw.CrossAxisAlignment.start,
          children: [
            pw.Text('BBVA - Estado de solicitud',
                style:
                    pw.TextStyle(fontSize: 18, fontWeight: pw.FontWeight.bold)),
            pw.Divider(),
            pw.SizedBox(height: 8),
            pw.Text('Cliente: ${s.clienteNombre}'),
            pw.Text('Expediente: ${s.numeroExpediente}'),
            pw.Text('Monto solicitado: ${Formatters.soles(s.montoSolicitado)}'),
            pw.Text('Estado actual: ${s.estado.toUpperCase()}'),
            if (s.createdAt != null) pw.Text('Fecha: ${s.createdAt}'),
            pw.SizedBox(height: 20),
            pw.Text('Documento generado desde la App Fuerza de Ventas.',
                style: const pw.TextStyle(fontSize: 10)),
          ],
        ),
      ),
    );
    await Printing.sharePdf(
        bytes: await doc.save(), filename: 'estado_${s.numeroExpediente}.pdf');
  }

  @override
  Widget build(BuildContext context) {
    final s = widget.s;
    const etapas = [
      'enviado',
      'recibido_comite',
      'en_evaluacion',
      'aprobado',
      'desembolsado'
    ];
    final idxActual = etapas.indexOf(s.estado);
    final notas = ref.watch(notasProvider(s.id));

    return Padding(
      padding: EdgeInsets.only(
          bottom: MediaQuery.of(context).viewInsets.bottom,
          left: 16,
          right: 16,
          top: 16),
      child: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Expanded(
                  child: Text(s.clienteNombre,
                      style: const TextStyle(
                          fontSize: 16, fontWeight: FontWeight.bold)),
                ),
                IconButton(
                  icon: const Icon(Icons.share, color: AppColors.primary),
                  tooltip: 'Compartir PDF',
                  onPressed: _compartirPdf,
                ),
              ],
            ),
            Text(
                '${s.numeroExpediente} · ${Formatters.soles(s.montoSolicitado)}',
                style: const TextStyle(color: AppColors.textSecondary)),
            const SizedBox(height: 6),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: [
                _InfoChip(
                  icon: Icons.calendar_month_rounded,
                  label: '${s.plazoMeses} meses',
                ),
                _InfoChip(
                  icon: Icons.percent_rounded,
                  label: 'TEA ${s.teaReferencial.toStringAsFixed(2)}%',
                ),
                _InfoChip(
                  icon: Icons.request_quote_rounded,
                  label: 'Cuota ${Formatters.soles(s.cuotaEstimada)}',
                ),
              ],
            ),
            const Divider(height: 20),
            // Linea de tiempo (RF-70)
            ...List.generate(etapas.length, (i) {
              final hecho = idxActual >= 0 && i <= idxActual;
              return Row(
                children: [
                  Icon(
                    hecho ? Icons.check_circle : Icons.radio_button_unchecked,
                    color: hecho ? AppColors.success : AppColors.neutral,
                    size: 20,
                  ),
                  const SizedBox(width: 10),
                  Text(etapas[i].replaceAll('_', ' '),
                      style: TextStyle(
                          color: hecho
                              ? AppColors.textPrimary
                              : AppColors.textSecondary)),
                ],
              );
            }),
            if (s.condicionAdicional != null &&
                s.condicionAdicional!.isNotEmpty) ...[
              const SizedBox(height: 10),
              _DecisionBox(
                icon: Icons.assignment_late_rounded,
                color: AppColors.warning,
                title: 'Condicion adicional',
                body: s.condicionAdicional!,
              ),
            ],
            if (s.motivoRechazo != null && s.motivoRechazo!.isNotEmpty) ...[
              const SizedBox(height: 10),
              _DecisionBox(
                icon: Icons.block_rounded,
                color: AppColors.danger,
                title: 'Motivo de rechazo',
                body: s.motivoRechazo!,
              ),
            ],
            const Divider(height: 20),
            const Text('Gestion de flujo',
                style: TextStyle(fontWeight: FontWeight.w700)),
            const SizedBox(height: 8),
            OutlinedButton.icon(
              icon: const Icon(Icons.cloud_upload_rounded, size: 18),
              label: const Text('Documentos'),
              onPressed: () => context.push('/documentos', extra: s.id),
            ),
            const SizedBox(height: 8),
            _EstadoActions(
              acciones: _accionesDisponibles(s),
              estadoEnProceso: _estadoEnProceso,
              onPressed: _cambiarEstado,
            ),
            const Divider(height: 20),
            const Text('Notas internas (privadas)',
                style: TextStyle(fontWeight: FontWeight.w700)),
            notas.when(
              loading: () => const Padding(
                  padding: EdgeInsets.all(8), child: LinearProgressIndicator()),
              error: (_, __) => const Text('No se pudieron cargar las notas.'),
              data: (lista) => lista.isEmpty
                  ? const Padding(
                      padding: EdgeInsets.symmetric(vertical: 6),
                      child: Text('Sin notas.',
                          style: TextStyle(color: AppColors.textSecondary)))
                  : Column(
                      children: lista
                          .map((n) => ListTile(
                                dense: true,
                                leading:
                                    const Icon(Icons.sticky_note_2, size: 18),
                                title: Text(n),
                              ))
                          .toList(),
                    ),
            ),
            Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: _nota,
                    maxLength: 500,
                    decoration: const InputDecoration(
                        hintText: 'Agregar nota interna...', counterText: ''),
                  ),
                ),
                const SizedBox(width: 8),
                IconButton(
                  icon: _guardando
                      ? const SizedBox(
                          width: 18,
                          height: 18,
                          child: CircularProgressIndicator(strokeWidth: 2))
                      : const Icon(Icons.send, color: AppColors.primary),
                  onPressed: _guardando ? null : _agregarNota,
                ),
              ],
            ),
            const SizedBox(height: 12),
          ],
        ),
      ),
    );
  }
}
