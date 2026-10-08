import 'package:flutter/material.dart';
import '../../services/auth_service.dart';
import '../user/home_screen.dart';
import '../artist/artist_home_screen.dart';
import 'login_screen.dart';

class SplashScreen extends StatefulWidget {
  const SplashScreen({super.key});

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> {
  final AuthService authService = AuthService();

  @override
  void initState() {
    super.initState();
    checkLogin();
  }

  Future<void> checkLogin() async {
    await Future.delayed(const Duration(seconds: 2));

    final token = await authService.getToken();
    final role = await authService.getRole();

    if (!mounted) return;

    if (token != null && token.isNotEmpty) {

      print('SPLASH ROLE: $role');

      if (role == 'ARTIST') {
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(
            builder: (_) => const ArtistHomeScreen(),
          ),
        );
      } else if (role == 'USER') {
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(
            builder: (_) => const HomeScreen(),
          ),
        );
      } else {
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(
            builder: (_) => const LoginScreen(),
          ),
        );
      }

    } else {
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(
          builder: (_) => const LoginScreen(),
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      backgroundColor: Color(0xFFF8F6FC),
      body: Center(
        child: Text(
          'ArtVerse',
          style: TextStyle(
            color: Color(0xFF6C2BD9),
            fontSize: 32,
            fontWeight: FontWeight.bold,
          ),
        ),
      ),
    );
  }
}