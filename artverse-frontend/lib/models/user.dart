class User {
  final int id;
  final String fullName;
  final String email;
  final String? profileImage;
  final String? phone;
  final String? role;
  final String? artistLevel;
  final String? verificationStatus;
  final bool? enabled;
  final String? createdAt;

  User({
    required this.id,
    required this.fullName,
    required this.email,
    this.profileImage,
    this.phone,
    this.role,
    this.artistLevel,
    this.verificationStatus,
    this.enabled,
    this.createdAt,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id'],
      fullName: json['fullName'] ?? '',
      email: json['email'] ?? '',
      profileImage: json['profileImage'],
      phone: json['phone'],
      role: json['role'],
      artistLevel: json['artistLevel'],
      verificationStatus: json['verificationStatus'],
      enabled: json['enabled'],
      createdAt: json['createdAt'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'fullName': fullName,
      'email': email,
      'profileImage': profileImage,
      'phone': phone,
      'role': role,
      'artistLevel': artistLevel,
      'verificationStatus': verificationStatus,
      'enabled': enabled,
      'createdAt': createdAt,
    };
  }
}