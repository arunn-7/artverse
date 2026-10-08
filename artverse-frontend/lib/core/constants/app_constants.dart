class AppConstants {
  // App information
  static const String appName = 'ArtVerse';

  // Backend
  static const String baseUrl = 'http://10.229.36.59:8080';
  // API endpoints
  static const String loginEndpoint = '/api/auth/login';
  static const String registerEndpoint = '/api/auth/register';

  // User roles
  static const String roleUser = 'USER';
  static const String roleArtist = 'ARTIST';
  static const String roleAdmin = 'ADMIN';

  // Artist levels
  static const String beginner = 'BEGINNER';
  static const String intermediate = 'INTERMEDIATE';
  static const String professional = 'PROFESSIONAL';

  // Storage keys
  static const String tokenKey = 'auth_token';
  static const String userIdKey = 'user_id';
  static const String roleKey = 'user_role';
}