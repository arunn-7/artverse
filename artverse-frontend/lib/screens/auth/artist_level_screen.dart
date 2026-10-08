import 'package:flutter/material.dart';
import 'register_screen.dart';

class ArtistLevelScreen extends StatelessWidget {
  const ArtistLevelScreen({super.key});

  void _selectLevel(BuildContext context, String level) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => RegisterScreen(
          accountType: 'ARTIST',
          artistLevel: level,
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF9F7FC),
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        foregroundColor: Colors.black,
        title: const Text(
          'Artist Level',
          style: TextStyle(
            fontWeight: FontWeight.bold,
          ),
        ),
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const SizedBox(height: 10),

              const Text(
                'Choose your artist level',
                style: TextStyle(
                  fontSize: 28,
                  fontWeight: FontWeight.bold,
                ),
              ),

              const SizedBox(height: 10),

              Text(
                'Select the level that best describes your current experience.',
                style: TextStyle(
                  fontSize: 15,
                  color: Colors.grey.shade600,
                ),
              ),

              const SizedBox(height: 30),

              _artistLevelCard(
                context: context,
                icon: Icons.brush_outlined,
                title: 'Beginner',
                description:
                'Starting your artistic journey and building your skills.',
                certificateRequired: false,
                level: 'BEGINNER',
              ),

              const SizedBox(height: 18),

              _artistLevelCard(
                context: context,
                icon: Icons.auto_awesome_outlined,
                title: 'Intermediate',
                description:
                'You have experience and want to showcase your artistic skills.',
                certificateRequired: true,
                level: 'INTERMEDIATE',
              ),

              const SizedBox(height: 18),

              _artistLevelCard(
                context: context,
                icon: Icons.workspace_premium_outlined,
                title: 'Professional',
                description:
                'An experienced artist with professional qualifications.',
                certificateRequired: true,
                level: 'PROFESSIONAL',
              ),

              const SizedBox(height: 30),

              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: const Color(0xFFF0E8FF),
                  borderRadius: BorderRadius.circular(14),
                ),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Icon(
                      Icons.info_outline,
                      color: Color(0xFF6C2BD9),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Text(
                        'Intermediate and Professional artists must provide a certificate for verification.',
                        style: TextStyle(
                          color: Colors.grey.shade800,
                          fontSize: 13,
                          height: 1.4,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _artistLevelCard({
    required BuildContext context,
    required IconData icon,
    required String title,
    required String description,
    required bool certificateRequired,
    required String level,
  }) {
    return InkWell(
      borderRadius: BorderRadius.circular(18),
      onTap: () => _selectLevel(context, level),
      child: Container(
        width: double.infinity,
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(18),
          border: Border.all(
            color: Colors.grey.shade200,
          ),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.04),
              blurRadius: 10,
              offset: const Offset(0, 4),
            ),
          ],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  width: 52,
                  height: 52,
                  decoration: BoxDecoration(
                    color: const Color(0xFFF0E8FF),
                    borderRadius: BorderRadius.circular(14),
                  ),
                  child: const Icon(
                    Icons.brush_outlined,
                    color: Color(0xFF6C2BD9),
                    size: 28,
                  ),
                ),

                const SizedBox(width: 14),

                Expanded(
                  child: Text(
                    title,
                    style: const TextStyle(
                      fontSize: 20,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),

                const Icon(
                  Icons.arrow_forward_ios,
                  size: 18,
                  color: Colors.grey,
                ),
              ],
            ),

            const SizedBox(height: 15),

            Text(
              description,
              style: TextStyle(
                fontSize: 14,
                color: Colors.grey.shade600,
                height: 1.4,
              ),
            ),

            const SizedBox(height: 15),

            Container(
              padding: const EdgeInsets.symmetric(
                horizontal: 12,
                vertical: 7,
              ),
              decoration: BoxDecoration(
                color: certificateRequired
                    ? Colors.orange.shade50
                    : Colors.green.shade50,
                borderRadius: BorderRadius.circular(20),
              ),
              child: Text(
                certificateRequired
                    ? 'Certificate required'
                    : 'No certificate required',
                style: TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.w600,
                  color: certificateRequired
                      ? Colors.orange.shade800
                      : Colors.green.shade700,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}