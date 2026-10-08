class Commission {
  final int id;
  final int clientId;
  final String title;
  final String description;
  final String? category;
  final double? budget;
  final int requiredDays;
  final String? deadline;
  final String status;
  final String? paymentStatus;
  final String? createdAt;
  final String? updatedAt;

  Commission({
    required this.id,
    required this.clientId,
    required this.title,
    required this.description,
    this.category,
    this.budget,
    required this.requiredDays,
    this.deadline,
    required this.status,
    this.paymentStatus,
    this.createdAt,
    this.updatedAt,
  });

  factory Commission.fromJson(Map<String, dynamic> json) {
    return Commission(
      id: json['id'],
      clientId: json['clientId'],
      title: json['title'],
      description: json['description'],
      category: json['category'],
      budget: json['budget'] != null
          ? double.tryParse(json['budget'].toString())
          : null,
      requiredDays: json['requiredDays'],
      deadline: json['deadline'],
      status: json['status'],
      paymentStatus: json['paymentStatus'],
      createdAt: json['createdAt'],
      updatedAt: json['updatedAt'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'clientId': clientId,
      'title': title,
      'description': description,
      'category': category,
      'budget': budget,
      'requiredDays': requiredDays,
      'deadline': deadline,
      'status': status,
      'paymentStatus': paymentStatus,
      'createdAt': createdAt,
      'updatedAt': updatedAt,
    };
  }
}