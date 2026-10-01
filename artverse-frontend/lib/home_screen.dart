import 'package:flutter/material.dart';
import 'artwork_service.dart';
import 'artwork_detail_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  static const Color primaryColor = Color(0xFF6C2BD9);
  static const Color backgroundColor = Color(0xFFF9F7FC);

  final ArtworkService artworkService = ArtworkService();

  List<dynamic> artworks = [];
  bool isLoading = true;
  String? errorMessage;

  @override
  void initState() {
    super.initState();
    loadArtworks();
  }

  Future<void> loadArtworks() async {
    try {
      final data = await artworkService.getArtworkFeed();

      if (!mounted) return;

      setState(() {
        artworks = data;
        isLoading = false;
      });
    } catch (e) {
      print('HOME: Error loading artworks = $e');

      if (!mounted) return;

      setState(() {
        isLoading = false;
        errorMessage = 'Unable to load artworks';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: backgroundColor,

      // =========================
      // APP BAR
      // =========================
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        automaticallyImplyLeading: false,

        title: const Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'ArtVerse',
              style: TextStyle(
                color: Colors.black,
                fontSize: 23,
                fontWeight: FontWeight.bold,
              ),
            ),
            Text(
              'Discover beautiful art',
              style: TextStyle(
                color: Colors.grey,
                fontSize: 12,
              ),
            ),
          ],
        ),

        actions: [
          IconButton(
            onPressed: () {},
            icon: const Icon(
              Icons.search,
              color: Colors.black87,
            ),
          ),
          IconButton(
            onPressed: () {},
            icon: const Icon(
              Icons.notifications_none_outlined,
              color: Colors.black87,
            ),
          ),
          const SizedBox(width: 6),
        ],
      ),

      // =========================
      // BODY
      // =========================
      body: RefreshIndicator(
        onRefresh: loadArtworks,

        child: SingleChildScrollView(
          physics: const AlwaysScrollableScrollPhysics(),
          padding: const EdgeInsets.fromLTRB(16, 18, 16, 90),

          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [

              // =========================
              // WELCOME
              // =========================

              const Text(
                'Hello, Arun 👋',
                style: TextStyle(
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                  color: Colors.black87,
                ),
              ),

              const SizedBox(height: 6),

              const Text(
                'Explore creativity from artists around you.',
                style: TextStyle(
                  fontSize: 14,
                  color: Colors.grey,
                ),
              ),

              const SizedBox(height: 22),

              // =========================
              // FEATURED
              // =========================

              const Text(
                'Featured Artwork',
                style: TextStyle(
                  fontSize: 19,
                  fontWeight: FontWeight.bold,
                ),
              ),

              const SizedBox(height: 12),

              Container(
                height: 210,
                width: double.infinity,

                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(20),

                  gradient: const LinearGradient(
                    colors: [
                      Color(0xFF6C2BD9),
                      Color(0xFF9B6DFF),
                    ],
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                  ),
                ),

                child: Stack(
                  children: [

                    Positioned(
                      right: 20,
                      top: 20,

                      child: Icon(
                        Icons.palette_outlined,
                        size: 110,
                        color: Colors.white.withOpacity(0.18),
                      ),
                    ),

                    Positioned(
                      left: 20,
                      bottom: 22,
                      right: 20,

                      child: Column(
                        crossAxisAlignment:
                        CrossAxisAlignment.start,

                        children: [

                          const Text(
                            'Art that tells a story',
                            style: TextStyle(
                              color: Colors.white,
                              fontSize: 24,
                              fontWeight: FontWeight.bold,
                            ),
                          ),

                          const SizedBox(height: 6),

                          const Text(
                            'Discover unique creations from talented artists.',
                            style: TextStyle(
                              color: Colors.white70,
                              fontSize: 13,
                            ),
                          ),

                          const SizedBox(height: 14),

                          ElevatedButton(
                            onPressed: () {},

                            style: ElevatedButton.styleFrom(
                              backgroundColor: Colors.white,
                              foregroundColor: primaryColor,
                              elevation: 0,

                              padding:
                              const EdgeInsets.symmetric(
                                horizontal: 18,
                                vertical: 10,
                              ),

                              shape:
                              RoundedRectangleBorder(
                                borderRadius:
                                BorderRadius.circular(12),
                              ),
                            ),

                            child: const Text(
                              'Explore Now',
                              style: TextStyle(
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 28),

              // =========================
              // CATEGORIES
              // =========================

              Row(
                mainAxisAlignment:
                MainAxisAlignment.spaceBetween,

                children: [

                  const Text(
                    'Categories',
                    style: TextStyle(
                      fontSize: 19,
                      fontWeight: FontWeight.bold,
                    ),
                  ),

                  TextButton(
                    onPressed: () {},

                    child: const Text(
                      'See all',
                      style: TextStyle(
                        color: primaryColor,
                      ),
                    ),
                  ),
                ],
              ),

              const SizedBox(height: 8),

              SizedBox(
                height: 100,

                child: ListView(
                  scrollDirection: Axis.horizontal,

                  children: const [

                    _CategoryCard(
                      icon: Icons.brush_outlined,
                      title: 'Painting',
                    ),

                    _CategoryCard(
                      icon: Icons.edit_outlined,
                      title: 'Sketch',
                    ),

                    _CategoryCard(
                      icon: Icons.color_lens_outlined,
                      title: 'Watercolor',
                    ),

                    _CategoryCard(
                      icon: Icons.auto_awesome_outlined,
                      title: 'Abstract',
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 25),

              // =========================
              // TRENDING ARTWORK
              // =========================

              Row(
                mainAxisAlignment:
                MainAxisAlignment.spaceBetween,

                children: [

                  const Text(
                    'Trending Artwork',
                    style: TextStyle(
                      fontSize: 19,
                      fontWeight: FontWeight.bold,
                    ),
                  ),

                  TextButton(
                    onPressed: () {},

                    child: const Text(
                      'See all',
                      style: TextStyle(
                        color: primaryColor,
                      ),
                    ),
                  ),
                ],
              ),

              const SizedBox(height: 8),

              // =========================
              // REAL API ARTWORKS
              // =========================

              buildArtworkSection(),

              const SizedBox(height: 25),

              // =========================
              // ARTISTS
              // =========================

              const Text(
                'Artists to Discover',
                style: TextStyle(
                  fontSize: 19,
                  fontWeight: FontWeight.bold,
                ),
              ),

              const SizedBox(height: 15),

              const _ArtistCard(
                name: 'Arun',
                description: 'Traditional Artist',
              ),

              const _ArtistCard(
                name: 'Rahul',
                description: 'Sketch Artist',
              ),

              const _ArtistCard(
                name: 'Neha',
                description: 'Watercolor Artist',
              ),
            ],
          ),
        ),
      ),

      // =========================
      // BOTTOM NAVIGATION
      // =========================

      bottomNavigationBar: BottomNavigationBar(
        currentIndex: 0,
        type: BottomNavigationBarType.fixed,
        backgroundColor: Colors.white,

        selectedItemColor: primaryColor,
        unselectedItemColor: Colors.grey,

        onTap: (index) {},

        items: const [

          BottomNavigationBarItem(
            icon: Icon(Icons.home_outlined),
            activeIcon: Icon(Icons.home),
            label: 'Home',
          ),

          BottomNavigationBarItem(
            icon: Icon(Icons.explore_outlined),
            activeIcon: Icon(Icons.explore),
            label: 'Explore',
          ),

          BottomNavigationBarItem(
            icon: Icon(Icons.add_circle_outline),
            activeIcon: Icon(Icons.add_circle),
            label: 'Create',
          ),

          BottomNavigationBarItem(
            icon: Icon(Icons.favorite_border),
            activeIcon: Icon(Icons.favorite),
            label: 'Favorites',
          ),

          BottomNavigationBarItem(
            icon: Icon(Icons.person_outline),
            activeIcon: Icon(Icons.person),
            label: 'Profile',
          ),
        ],
      ),
    );
  }

  // ====================================================
  // ARTWORK SECTION
  // ====================================================

  Widget buildArtworkSection() {

    if (isLoading) {
      return const SizedBox(
        height: 245,
        child: Center(
          child: CircularProgressIndicator(
            color: Color(0xFF6C2BD9),
          ),
        ),
      );
    }

    if (errorMessage != null) {
      return SizedBox(
        height: 245,

        child: Center(
          child: Column(
            mainAxisAlignment:
            MainAxisAlignment.center,

            children: [

              const Icon(
                Icons.error_outline,
                size: 40,
                color: Colors.grey,
              ),

              const SizedBox(height: 10),

              Text(
                errorMessage!,
                style: const TextStyle(
                  color: Colors.grey,
                ),
              ),

              const SizedBox(height: 10),

              ElevatedButton(
                onPressed: loadArtworks,

                style: ElevatedButton.styleFrom(
                  backgroundColor:
                  const Color(0xFF6C2BD9),
                  foregroundColor: Colors.white,
                ),

                child: const Text('Retry'),
              ),
            ],
          ),
        ),
      );
    }

    if (artworks.isEmpty) {
      return const SizedBox(
        height: 245,

        child: Center(
          child: Text(
            'No artworks available',
            style: TextStyle(
              color: Colors.grey,
            ),
          ),
        ),
      );
    }

    return SizedBox(
      height: 275,

      child: ListView.builder(
        scrollDirection: Axis.horizontal,

        itemCount: artworks.length,

        itemBuilder: (context, index) {

          final artwork = artworks[index];

          return GestureDetector(
            onTap: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => ArtworkDetailScreen(
                    artwork: artwork,
                  ),
                ),
              );
            },
            child: _ArtworkCard(
              title: artwork['title'] ?? 'Untitled',
              artist: artwork['artistName'] ?? 'Unknown Artist',
              imageUrl: artwork['imageUrl'] ?? '',
              likeCount: artwork['likeCount'] ?? 0,
              commentCount: artwork['commentCount'] ?? 0,
            ),
          );
        },
      ),
    );
  }
}


// ======================================================
// CATEGORY CARD
// ======================================================

class _CategoryCard extends StatelessWidget {

  final IconData icon;
  final String title;

  const _CategoryCard({
    required this.icon,
    required this.title,
  });

  @override
  Widget build(BuildContext context) {

    return Container(
      width: 105,

      margin: const EdgeInsets.only(right: 12),

      decoration: BoxDecoration(
        color: Colors.white,

        borderRadius:
        BorderRadius.circular(16),

        border: Border.all(
          color: Colors.grey.shade200,
        ),
      ),

      child: Column(
        mainAxisAlignment:
        MainAxisAlignment.center,

        children: [

          Icon(
            icon,
            color: const Color(0xFF6C2BD9),
            size: 30,
          ),

          const SizedBox(height: 8),

          Text(
            title,
            style: const TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }
}


// ======================================================
// REAL ARTWORK CARD
// ======================================================

class _ArtworkCard extends StatelessWidget {

  final String title;
  final String artist;
  final String imageUrl;
  final int likeCount;
  final int commentCount;

  const _ArtworkCard({
    required this.title,
    required this.artist,
    required this.imageUrl,
    required this.likeCount,
    required this.commentCount,
  });

  @override
  Widget build(BuildContext context) {

    return Container(
      width: 185,

      margin: const EdgeInsets.only(right: 14),

      decoration: BoxDecoration(
        color: Colors.white,

        borderRadius:
        BorderRadius.circular(18),

        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 8,
            offset: const Offset(0, 3),
          ),
        ],
      ),

      child: Column(
        crossAxisAlignment:
        CrossAxisAlignment.start,

        children: [

          // =========================
          // IMAGE
          // =========================

          ClipRRect(
            borderRadius:
            const BorderRadius.vertical(
              top: Radius.circular(18),
            ),

            child: SizedBox(
              height: 155,
              width: double.infinity,

              child: imageUrl.isNotEmpty
                  ? Image.network(
                imageUrl,

                fit: BoxFit.cover,

                loadingBuilder:
                    (context, child, loadingProgress) {

                  if (loadingProgress == null) {
                    return child;
                  }

                  return const Center(
                    child:
                    CircularProgressIndicator(
                      color:
                      Color(0xFF6C2BD9),
                    ),
                  );
                },

                errorBuilder:
                    (context, error, stackTrace) {

                  return const Center(
                    child: Icon(
                      Icons.image_not_supported_outlined,
                      size: 45,
                      color: Colors.grey,
                    ),
                  );
                },
              )

                  : const Center(
                child: Icon(
                  Icons.image,
                  size: 45,
                  color: Colors.grey,
                ),
              ),
            ),
          ),

          // =========================
          // ARTWORK DETAILS
          // =========================

          Padding(
            padding:
            const EdgeInsets.fromLTRB(
              12,
              9,
              12,
              8,
            ),

            child: Column(
              crossAxisAlignment:
              CrossAxisAlignment.start,

              children: [

                Text(
                  title,

                  maxLines: 1,

                  overflow:
                  TextOverflow.ellipsis,

                  style: const TextStyle(
                    fontSize: 15,
                    fontWeight: FontWeight.bold,
                  ),
                ),

                const SizedBox(height: 3),

                Text(
                  'by $artist',

                  maxLines: 1,

                  overflow:
                  TextOverflow.ellipsis,

                  style: const TextStyle(
                    fontSize: 12,
                    color: Colors.grey,
                  ),
                ),

                const SizedBox(height: 7),

                Row(
                  children: [

                    const Icon(
                      Icons.favorite_border,
                      size: 15,
                      color: Colors.grey,
                    ),

                    const SizedBox(width: 4),

                    Text(
                      '$likeCount',
                      style: const TextStyle(
                        fontSize: 11,
                        color: Colors.grey,
                      ),
                    ),

                    const SizedBox(width: 12),

                    const Icon(
                      Icons.comment_outlined,
                      size: 15,
                      color: Colors.grey,
                    ),

                    const SizedBox(width: 4),

                    Text(
                      '$commentCount',
                      style: const TextStyle(
                        fontSize: 11,
                        color: Colors.grey,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}


// ======================================================
// ARTIST CARD
// ======================================================

class _ArtistCard extends StatelessWidget {

  final String name;
  final String description;

  const _ArtistCard({
    required this.name,
    required this.description,
  });

  @override
  Widget build(BuildContext context) {

    return Container(
      margin:
      const EdgeInsets.only(bottom: 12),

      padding:
      const EdgeInsets.all(14),

      decoration: BoxDecoration(
        color: Colors.white,

        borderRadius:
        BorderRadius.circular(16),
      ),

      child: Row(
        children: [

          CircleAvatar(
            radius: 27,

            backgroundColor:
            const Color(0xFFEDE7F6),

            child: Text(
              name[0],

              style: const TextStyle(
                color: Color(0xFF6C2BD9),
                fontSize: 20,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),

          const SizedBox(width: 14),

          Expanded(
            child: Column(
              crossAxisAlignment:
              CrossAxisAlignment.start,

              children: [

                Text(
                  name,

                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                  ),
                ),

                const SizedBox(height: 4),

                Text(
                  description,

                  style: const TextStyle(
                    fontSize: 12,
                    color: Colors.grey,
                  ),
                ),
              ],
            ),
          ),

          OutlinedButton(
            onPressed: () {},

            style: OutlinedButton.styleFrom(
              foregroundColor:
              const Color(0xFF6C2BD9),

              side: const BorderSide(
                color: Color(0xFF6C2BD9),
              ),

              shape:
              RoundedRectangleBorder(
                borderRadius:
                BorderRadius.circular(10),
              ),
            ),

            child: const Text('View'),
          ),
        ],
      ),
    );
  }
}