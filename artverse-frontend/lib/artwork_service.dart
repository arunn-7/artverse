import 'dart:convert';

import 'package:http/http.dart' as http;
import 'auth_service.dart';

class ArtworkService {
  static const String baseUrl = 'http://10.0.2.2:8080';

  final AuthService authService = AuthService();

  Future<List<dynamic>> getArtworkFeed() async {
    final token = await authService.getToken();

    if (token == null) {
      throw Exception('User is not logged in');
    }

    print('ARTWORK: Fetching artwork feed...');

    final response = await http
        .get(
      Uri.parse('$baseUrl/api/artworks/feed'),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer $token',
      },
    )
        .timeout(
      const Duration(seconds: 10),
    );

    print('ARTWORK: Status code = ${response.statusCode}');
    print('ARTWORK: Body = ${response.body}');

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);

      print('ARTWORK: Feed loaded successfully');

      return List<dynamic>.from(data);
    }

    throw Exception(
      'Failed to load artworks: ${response.body}',
    );
  }
}