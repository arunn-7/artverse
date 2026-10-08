import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import 'package:file_picker/file_picker.dart';
import 'package:flutter/foundation.dart';
import '../core/constants/app_constants.dart';

class AuthService {
  static const String baseUrl = 'http://10.229.36.59:8080';

  Future<Map<String, dynamic>> login(
      String email,
      String password,
      ) async {
    print('AUTH: Starting login request...');

    final response = await http
        .post(
      Uri.parse(
        '${AppConstants.baseUrl}/api/auth/login',
      ),
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
        AppConstants.tokenKey,
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
        AppConstants.roleKey,
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

    return prefs.getString(
      AppConstants.tokenKey,
    );
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

    return prefs.getString(
      AppConstants.roleKey,
    );
  }

  Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();

    await prefs.clear();
  }
  Future<String> register({
    required String fullName,
    required String email,
    required String password,
    required String accountType,
    String? artistLevel,
  }) async {
    print('AUTH: Starting registration request...');

    final response = await http
        .post(
      Uri.parse('$baseUrl/api/auth/register'),
      headers: {
        'Content-Type': 'application/json',
      },
      body: jsonEncode({
        'fullName': fullName,
        'email': email,
        'password': password,
        'accountType': accountType,
        'artistLevel': artistLevel,
      }),
    )
        .timeout(
      const Duration(seconds: 10),
    );

    print(
      'AUTH: Registration status = ${response.statusCode}',
    );

    print(
      'AUTH: Registration body = ${response.body}',
    );

    if (response.statusCode == 200) {
      return response.body;
    }

    throw Exception(
      'Registration failed: ${response.body}',
    );
  }
  Future<String> uploadCertificate(PlatformFile file) async {
    if (file.path == null) {
      throw Exception('Certificate file path is not available');
    }

    final token = await getToken();

    if (token == null || token.isEmpty) {
      throw Exception('User is not authenticated');
    }

    final request = http.MultipartRequest(
      'PUT',
      Uri.parse('$baseUrl/api/users/me/certificate'),
    );

    request.headers['Authorization'] = 'Bearer $token';

    request.files.add(
      await http.MultipartFile.fromPath(
        'certificate',
        file.path!,
      ),
    );

    debugPrint('CERTIFICATE: Uploading ${file.name}');

    final response = await request.send();

    final responseBody = await response.stream.bytesToString();

    debugPrint('CERTIFICATE: Status = ${response.statusCode}');
    debugPrint('CERTIFICATE: Response = $responseBody');

    if (response.statusCode == 200) {
      return responseBody;
    }

    throw Exception(
      'Certificate upload failed: $responseBody',
    );
  }
}