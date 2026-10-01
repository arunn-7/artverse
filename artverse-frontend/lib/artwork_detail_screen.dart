import 'package:flutter/material.dart';
import 'like_service.dart';

class ArtworkDetailScreen extends StatefulWidget {
  final dynamic artwork;

  const ArtworkDetailScreen({
    super.key,
    required this.artwork,
  });

  @override
  State<ArtworkDetailScreen> createState() =>
      _ArtworkDetailScreenState();
}

class _ArtworkDetailScreenState
    extends State<ArtworkDetailScreen> {
  static const Color primaryColor = Color(0xFF6C2BD9);

  final LikeService likeService = LikeService();

  late bool isLiked;
  late int likeCount;

  bool isLoadingLike = false;

  @override
  void initState() {
    super.initState();

    isLiked =
        widget.artwork['likedByCurrentUser'] ?? false;

    likeCount =
        widget.artwork['likeCount'] ?? 0;
  }

  // =====================================================
  // LIKE / UNLIKE
  // =====================================================

  Future<void> toggleLike() async {
    if (isLoadingLike) return;

    setState(() {
      isLoadingLike = true;
    });

    try {
      final int artworkId =
      widget.artwork['id'];

      if (isLiked) {
        // UNLIKE
        await likeService.unlikeArtwork(
          artworkId,
        );

        if (!mounted) return;

        setState(() {
          isLiked = false;

          if (likeCount > 0) {
            likeCount--;
          }
        });
      } else {
        // LIKE
        await likeService.likeArtwork(
          artworkId,
        );

        if (!mounted) return;

        setState(() {
          isLiked = true;
          likeCount++;
        });
      }
    } catch (e) {
      print('LIKE ERROR: $e');

      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text(
            'Unable to update like',
          ),
        ),
      );
    } finally {
      if (mounted) {
        setState(() {
          isLoadingLike = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final artwork = widget.artwork;

    // =====================================================
    // ARTWORK DATA
    // =====================================================

    final String title =
        artwork['title'] ?? 'Untitled';

    final String description =
        artwork['description'] ??
            'No description available.';

    final String category =
        artwork['category'] ?? 'Artwork';

    final String imageUrl =
        artwork['imageUrl'] ?? '';

    final String artistName =
        artwork['artistName'] ??
            'Unknown Artist';

    final int commentCount =
        artwork['commentCount'] ?? 0;

    return Scaffold(
      backgroundColor: const Color(0xFFF9F7FC),

      // ===================================================
      // APP BAR
      // ===================================================

      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,

        title: const Text(
          'Artwork',
          style: TextStyle(
            color: Colors.black,
            fontWeight: FontWeight.bold,
          ),
        ),

        iconTheme: const IconThemeData(
          color: Colors.black,
        ),

        actions: [
          IconButton(
            onPressed: () {},
            icon: const Icon(
              Icons.more_vert,
            ),
          ),
        ],
      ),

      // ===================================================
      // BODY
      // ===================================================

      body: SingleChildScrollView(
        child: Column(
          crossAxisAlignment:
          CrossAxisAlignment.start,

          children: [

            // =================================================
            // ARTWORK IMAGE
            // =================================================

            if (imageUrl.isNotEmpty)
              SizedBox(
                width: double.infinity,
                height: 350,

                child: Image.network(
                  imageUrl,
                  fit: BoxFit.cover,

                  loadingBuilder:
                      (
                      context,
                      child,
                      loadingProgress,
                      ) {
                    if (loadingProgress == null) {
                      return child;
                    }

                    return const Center(
                      child:
                      CircularProgressIndicator(
                        color: primaryColor,
                      ),
                    );
                  },

                  errorBuilder:
                      (
                      context,
                      error,
                      stackTrace,
                      ) {
                    return const Center(
                      child: Icon(
                        Icons
                            .image_not_supported_outlined,
                        size: 60,
                        color: Colors.grey,
                      ),
                    );
                  },
                ),
              )
            else
              const SizedBox(
                height: 350,
                child: Center(
                  child: Icon(
                    Icons.image,
                    size: 60,
                    color: Colors.grey,
                  ),
                ),
              ),

            // =================================================
            // CONTENT
            // =================================================

            Padding(
              padding: const EdgeInsets.all(18),

              child: Column(
                crossAxisAlignment:
                CrossAxisAlignment.start,

                children: [

                  // ===========================================
                  // CATEGORY
                  // ===========================================

                  Container(
                    padding:
                    const EdgeInsets.symmetric(
                      horizontal: 12,
                      vertical: 6,
                    ),

                    decoration: BoxDecoration(
                      color:
                      const Color(0xFFEDE7F6),

                      borderRadius:
                      BorderRadius.circular(20),
                    ),

                    child: Text(
                      category,

                      style: const TextStyle(
                        color: primaryColor,
                        fontSize: 12,
                        fontWeight:
                        FontWeight.bold,
                      ),
                    ),
                  ),

                  const SizedBox(height: 14),

                  // ===========================================
                  // TITLE
                  // ===========================================

                  Text(
                    title,

                    style: const TextStyle(
                      fontSize: 27,
                      fontWeight:
                      FontWeight.bold,
                      color: Colors.black87,
                    ),
                  ),

                  const SizedBox(height: 10),

                  // ===========================================
                  // ARTIST
                  // ===========================================

                  Row(
                    children: [

                      CircleAvatar(
                        radius: 21,

                        backgroundColor:
                        const Color(
                          0xFFEDE7F6,
                        ),

                        child: Text(
                          artistName
                              .isNotEmpty
                              ? artistName[0]
                              : '?',

                          style:
                          const TextStyle(
                            color:
                            primaryColor,
                            fontSize: 18,
                            fontWeight:
                            FontWeight.bold,
                          ),
                        ),
                      ),

                      const SizedBox(width: 10),

                      Column(
                        crossAxisAlignment:
                        CrossAxisAlignment.start,

                        children: [

                          const Text(
                            'Created by',
                            style: TextStyle(
                              fontSize: 11,
                              color:
                              Colors.grey,
                            ),
                          ),

                          Text(
                            artistName,

                            style:
                            const TextStyle(
                              fontSize: 15,
                              fontWeight:
                              FontWeight.w600,
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),

                  const SizedBox(height: 22),

                  // ===========================================
                  // LIKE + COMMENT STATS
                  // ===========================================

                  Row(
                    children: [

                      // LIKE COUNT
                      Row(
                        children: [

                          Icon(
                            isLiked
                                ? Icons.favorite
                                : Icons
                                .favorite_border,
                            color: isLiked
                                ? Colors.red
                                : primaryColor,
                            size: 23,
                          ),

                          const SizedBox(width: 7),

                          Text(
                            '$likeCount likes',

                            style:
                            const TextStyle(
                              fontSize: 14,
                              fontWeight:
                              FontWeight.w500,
                            ),
                          ),
                        ],
                      ),

                      const SizedBox(width: 25),

                      // COMMENT COUNT
                      Row(
                        children: [

                          const Icon(
                            Icons
                                .comment_outlined,
                            color:
                            primaryColor,
                            size: 23,
                          ),

                          const SizedBox(width: 7),

                          Text(
                            '$commentCount comments',

                            style:
                            const TextStyle(
                              fontSize: 14,
                              fontWeight:
                              FontWeight.w500,
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),

                  const SizedBox(height: 28),

                  // ===========================================
                  // DESCRIPTION TITLE
                  // ===========================================

                  const Text(
                    'About this artwork',

                    style: TextStyle(
                      fontSize: 19,
                      fontWeight:
                      FontWeight.bold,
                    ),
                  ),

                  const SizedBox(height: 10),

                  // ===========================================
                  // DESCRIPTION
                  // ===========================================

                  Text(
                    description,

                    style: const TextStyle(
                      fontSize: 15,
                      height: 1.5,
                      color: Colors.black87,
                    ),
                  ),

                  const SizedBox(height: 30),

                  // ===========================================
                  // LIKE BUTTON
                  // ===========================================

                  SizedBox(
                    width: double.infinity,
                    height: 54,

                    child:
                    ElevatedButton.icon(
                      onPressed:
                      isLoadingLike
                          ? null
                          : toggleLike,

                      icon: isLoadingLike
                          ? const SizedBox(
                        width: 21,
                        height: 21,

                        child:
                        CircularProgressIndicator(
                          color:
                          Colors.white,
                          strokeWidth: 2.5,
                        ),
                      )
                          : Icon(
                        isLiked
                            ? Icons
                            .favorite
                            : Icons
                            .favorite_border,
                      ),

                      label: Text(
                        isLiked
                            ? 'Unlike Artwork'
                            : 'Like Artwork',

                        style:
                        const TextStyle(
                          fontSize: 16,
                          fontWeight:
                          FontWeight.bold,
                        ),
                      ),

                      style:
                      ElevatedButton.styleFrom(
                        backgroundColor:
                        primaryColor,

                        foregroundColor:
                        Colors.white,

                        disabledBackgroundColor:
                        primaryColor
                            .withOpacity(
                          0.6,
                        ),

                        shape:
                        RoundedRectangleBorder(
                          borderRadius:
                          BorderRadius
                              .circular(
                            14,
                          ),
                        ),
                      ),
                    ),
                  ),

                  const SizedBox(height: 15),

                  // ===========================================
                  // COMMENTS BUTTON
                  // ===========================================

                  SizedBox(
                    width: double.infinity,
                    height: 52,

                    child:
                    OutlinedButton.icon(
                      onPressed: () {},

                      icon: const Icon(
                        Icons
                            .comment_outlined,
                      ),

                      label: const Text(
                        'View Comments',
                        style: TextStyle(
                          fontSize: 15,
                          fontWeight:
                          FontWeight.w600,
                        ),
                      ),

                      style:
                      OutlinedButton.styleFrom(
                        foregroundColor:
                        primaryColor,

                        side:
                        const BorderSide(
                          color:
                          primaryColor,
                        ),

                        shape:
                        RoundedRectangleBorder(
                          borderRadius:
                          BorderRadius
                              .circular(
                            14,
                          ),
                        ),
                      ),
                    ),
                  ),

                  const SizedBox(height: 20),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}