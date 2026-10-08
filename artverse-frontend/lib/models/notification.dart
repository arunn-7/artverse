class AppNotification {
  final int id;
  final String title;
  final String message;
  final String type;
  final String? senderName;
  final String? senderProfileImage;
  final bool read;
  final String? createdAt;

  AppNotification({
    required this.id,
    required this.title,
    required this.message,
    required this.type,
    this.senderName,
    this.senderProfileImage,
    required this.read,
    this.createdAt,
  });

  factory AppNotification.fromJson(Map<String, dynamic> json) {
    return AppNotification(
      id: json['id'],
      title: json['title'],
      message: json['message'],
      type: json['type'],
      senderName: json['senderName'],
      senderProfileImage: json['senderProfileImage'],
      read: json['read'] ?? false,
      createdAt: json['createdAt'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'title': title,
      'message': message,
      'type': type,
      'senderName': senderName,
      'senderProfileImage': senderProfileImage,
      'read': read,
      'createdAt': createdAt,
    };
  }
}