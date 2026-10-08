class CommissionOffer {
  final int id;
  final int commissionId;
  final int artistId;
  final double proposedFee;
  final int estimatedDays;
  final String? message;
  final String status;
  final String? createdAt;

  CommissionOffer({
    required this.id,
    required this.commissionId,
    required this.artistId,
    required this.proposedFee,
    required this.estimatedDays,
    this.message,
    required this.status,
    this.createdAt,
  });

  factory CommissionOffer.fromJson(Map<String, dynamic> json) {
    return CommissionOffer(
      id: json['id'],
      commissionId: json['commissionId'],
      artistId: json['artistId'],
      proposedFee: double.parse(json['proposedFee'].toString()),
      estimatedDays: json['estimatedDays'],
      message: json['message'],
      status: json['status'],
      createdAt: json['createdAt'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'commissionId': commissionId,
      'artistId': artistId,
      'proposedFee': proposedFee,
      'estimatedDays': estimatedDays,
      'message': message,
      'status': status,
      'createdAt': createdAt,
    };
  }
}