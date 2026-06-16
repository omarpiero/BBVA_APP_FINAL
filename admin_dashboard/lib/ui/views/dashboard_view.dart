import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import '../../core/constants.dart';

class DashboardView extends StatefulWidget {
  const DashboardView({super.key});

  @override
  State<DashboardView> createState() => _DashboardViewState();
}

class _DashboardViewState extends State<DashboardView> {
  int totalSolicitudes = 0;
  int totalClientes = 0;
  int totalAsesores = 0;
  bool isLoading = true;
  List<Map<String, dynamic>> recientes = [];

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    try {
      final supabase = Supabase.instance.client;
      
      final solResp = await supabase.from('solicitudes_credito').select('id, estado, monto_solicitado, created_at').order('created_at', ascending: false);
      final cliResp = await supabase.from('clientes').select('id');
      final aseResp = await supabase.from('asesores').select('id');

      setState(() {
        totalSolicitudes = solResp.length;
        totalClientes = cliResp.length;
        totalAsesores = aseResp.length;
        recientes = List<Map<String, dynamic>>.from(solResp.take(5));
        isLoading = false;
      });
    } catch (e) {
      setState(() => isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    if (isLoading) {
      return const Center(child: CircularProgressIndicator());
    }
    
    return Padding(
      padding: const EdgeInsets.all(32.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(child: _StatCard(title: 'Solicitudes', value: '$totalSolicitudes', icon: Icons.description_rounded, color: AppColors.primary)),
              const SizedBox(width: 24),
              Expanded(child: _StatCard(title: 'Clientes', value: '$totalClientes', icon: Icons.people_alt_rounded, color: AppColors.secondary)),
              const SizedBox(width: 24),
              Expanded(child: _StatCard(title: 'Asesores', value: '$totalAsesores', icon: Icons.badge_rounded, color: AppColors.success)),
            ],
          ),
          const SizedBox(height: 32),
          const Text('Solicitudes Recientes', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: AppColors.textPrimary)),
          const SizedBox(height: 16),
          Expanded(
            child: Card(
              child: ListView.separated(
                padding: const EdgeInsets.all(16),
                itemCount: recientes.length,
                separatorBuilder: (c, i) => const Divider(height: 1),
                itemBuilder: (context, index) {
                  final sol = recientes[index];
                  return ListTile(
                    leading: CircleAvatar(
                      backgroundColor: AppColors.accent.withAlpha(51),
                      child: const Icon(Icons.description, color: AppColors.primaryDark),
                    ),
                    title: Text('Monto: S/ ${sol['monto_solicitado']}'),
                    subtitle: Text('Estado: ${sol['estado']}'),
                    trailing: const Icon(Icons.chevron_right),
                  );
                },
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _StatCard extends StatelessWidget {
  final String title;
  final String value;
  final IconData icon;
  final Color color;

  const _StatCard({required this.title, required this.value, required this.icon, required this.color});

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: color.withAlpha(25),
                borderRadius: BorderRadius.circular(16),
              ),
              child: Icon(icon, color: color, size: 32),
            ),
            const SizedBox(width: 24),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title, style: const TextStyle(color: AppColors.textSecondary, fontSize: 16)),
                const SizedBox(height: 4),
                Text(value, style: const TextStyle(color: AppColors.textPrimary, fontSize: 32, fontWeight: FontWeight.bold)),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
