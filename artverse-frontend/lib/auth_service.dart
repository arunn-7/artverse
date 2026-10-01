import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

class AuthService {
  static const String baseUrl = 'http://10.0.2.2:8080';

  Future<Map<String, dynamic>> login(
      String email,
      String password,
      ) async {
    print('AUTH: Starting login request...');

    final response = await http
        .post(
      Uri.parse('$baseUrl/api/auth/login'),
      headers: {
        'Content-Type': 'application/json',
      },
      body: jsonEncode({
        'email': email,
        'password': password,
      }),
    )
        .timeout(
      const Duration(seconds: 10),
    );

    print('AUTH: Response received');
    print('AUTH: Status code = ${response.statusCode}');
    print('AUTH: Body = ${response.body}');

    if (response.statusCode == 200) {
      print('AUTH: Login successful');

      final data = jsonDecode(response.body);

      print('AUTH: Saving token...');

      final prefs = await SharedPreferences.getInstance();

      await prefs.setString(
        'token',
        data['token'],
      );

      await prefs.setString(
        'fullName',
        data['fullName'],
      );

      await prefs.setString(
        'email',
        data['email'],
      );

      await prefs.setString(
        'role',
        data['role'],
      );

      print('AUTH: Token saved successfully');

      return data;
    }

    print('AUTH: Login failed');

    throw Exception(
      'Login failed: ${response.body}',
    );
  }

  Future<String?> getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('token');
  }

  Future<String?> getFullName() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('fullName');
  }

  Future<String?> getEmail() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('email');
  }

  Future<String?> getRole() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('role');
  }

  Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.clear();
  }
}