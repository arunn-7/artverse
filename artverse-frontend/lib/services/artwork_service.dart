import 'dart:convert';
import 'package:http/http.dart' as http;
import 'auth_service.dart';

class ArtworkService {
  static const String baseUrl = 'http://10.229.36.59:8080';

  final AuthService authService = AuthService();

  // =====================================================
  // GET ARTWORK FEED
  // Used by User Home / Artist Discover
  // =====================================================

  Future<List<Map<String, dynamic>>> getArtworkFeed() async {
    final token = await authService.getToken();

    print(
      'ARTWORK FEED TOKEN EXISTS: '
          '${token != null && token.isNotEmpty}',
    );

    if (token == null || token.isEmpty) {
      throw Exception('User is not authenticated');
    }

    final response = await http.get(
      Uri.parse('$baseUrl/api/artworks/feed'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    print('ARTWORK FEED: Status = ${response.statusCode}');
    print('ARTWORK FEED: Body = ${response.body}');

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(response.body);

      return data
          .map((item) => Map<String, dynamic>.from(item))
          .toList();
    }

    throw Exception(
      'Failed to load artwork feed: ${response.body}',
    );
  }


  // =====================================================
  // GET MY ARTWORKS
  // Used by Artist Dashboard / My Artworks
  // =====================================================

  Future<List<Map<String, dynamic>>> getMyArtworks() async {
    final token = await authService.getToken();

    print(
      'MY ARTWORKS TOKEN EXISTS: '
          '${token != null && token.isNotEmpty}',
    );

    if (token == null || token.isEmpty) {
      throw Exception('User is not authenticated');
    }

    final response = await http.get(
      Uri.parse('$baseUrl/api/artworks/my-artworks'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    print('MY ARTWORKS: Status = ${response.statusCode}');
    print('MY ARTWORKS: Body = ${response.body}');

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(response.body);

      return data
          .map((item) => Map<String, dynamic>.from(item))
          .toList();
    }

    throw Exception(
      'Failed to load my artworks: ${response.body}',
    );
  }
}