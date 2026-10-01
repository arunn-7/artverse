import 'package:http/http.dart' as http;
import 'auth_service.dart';

class LikeService {
  static const String baseUrl =
      'http://10.0.2.2:8080';

  final AuthService authService =
  AuthService();

  Future<String> likeArtwork(
      int artworkId,
      ) async {
    final token =
    await authService.getToken();

    if (token == null) {
      throw Exception(
        'User is not logged in',
      );
    }

    print(
      'LIKE: Liking artwork $artworkId',
    );

    final response = await http
        .post(
      Uri.parse(
        '$baseUrl/api/artworks/$artworkId/like',
      ),
      headers: {
        'Content-Type':
        'application/json',
        'Authorization':
        'Bearer $token',
      },
    )
        .timeout(
      const Duration(seconds: 10),
    );

    print(
      'LIKE: Status = ${response.statusCode}',
    );

    print(
      'LIKE: Body = ${response.body}',
    );

    if (response.statusCode == 200) {
      return response.body;
    }

    throw Exception(
      'Failed to like artwork: '
          '${response.body}',
    );
  }

  Future<String> unlikeArtwork(
      int artworkId,
      ) async {
    final token =
    await authService.getToken();

    if (token == null) {
      throw Exception(
        'User is not logged in',
      );
    }

    print(
      'LIKE: Unliking artwork $artworkId',
    );

    final response = await http
        .post(
      Uri.parse(
        '$baseUrl/api/artworks/$artworkId/unlike',
      ),
      headers: {
        'Content-Type':
        'application/json',
        'Authorization':
        'Bearer $token',
      },
    )
        .timeout(
      const Duration(seconds: 10),
    );

    print(
      'LIKE: Status = ${response.statusCode}',
    );

    print(
      'LIKE: Body = ${response.body}',
    );

    if (response.statusCode == 200) {
      return response.body;
    }

    throw Exception(
      'Failed to unlike artwork: '
          '${response.body}',
    );
  }
}