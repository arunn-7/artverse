import 'package:flutter/material.dart';
import 'splash_screen.dart';

void main() {
  runApp(const ArtVerseApp());
}

class ArtVerseApp extends StatelessWidget {
  const ArtVerseApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'ArtVerse',
      home: const SplashScreen(),
    );
  }
}