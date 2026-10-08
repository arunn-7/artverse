import 'dart:io';

import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';

import '../../services/auth_service.dart';

class RegisterScreen extends StatefulWidget {
  final String accountType;
  final String? artistLevel;

  const RegisterScreen({
    super.key,
    required this.accountType,
    this.artistLevel,
  });

  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  final AuthService _authService = AuthService();

  final TextEditingController fullNameController =
  TextEditingController();

  final TextEditingController emailController =
  TextEditingController();

  final TextEditingController passwordController =
  TextEditingController();

  final TextEditingController confirmPasswordController =
  TextEditingController();

  PlatformFile? selectedCertificate;


  bool isLoading = false;

  bool obscurePassword = true;
  bool obscureConfirmPassword = true;


  bool get isArtist =>
      widget.accountType.toUpperCase() == 'ARTIST';

  bool get certificateRequired =>
      isArtist &&
          (widget.artistLevel == 'INTERMEDIATE' ||
              widget.artistLevel == 'PROFESSIONAL');

  @override
  void dispose() {
    fullNameController.dispose();
    emailController.dispose();
    passwordController.dispose();
    confirmPasswordController.dispose();

    super.dispose();
  }

  Future<void> pickCertificate() async {
    try {
      final file = await FilePicker.pickFile(
        type: FileType.custom,
        allowedExtensions: [
          'pdf',
          'jpg',
          'jpeg',
          'png',
        ],
      );

      if (file == null) {
        debugPrint('No certificate selected');
        return;
      }

      setState(() {
        selectedCertificate = file;
      });

      debugPrint('Certificate selected: ${file.name}');
      debugPrint('Certificate path: ${file.path}');
    } catch (e) {
      debugPrint('Certificate picker error: $e');
    }
  }

  Future<void> registerUser() async {
    final fullName = fullNameController.text.trim();
    final email = emailController.text.trim();
    final password = passwordController.text;
    final confirmPassword = confirmPasswordController.text;

    // -----------------------------
    // BASIC VALIDATION
    // -----------------------------

    if (fullName.isEmpty ||
        email.isEmpty ||
        password.isEmpty ||
        confirmPassword.isEmpty) {
      showMessage('Please fill all fields');
      return;
    }

    if (password.length < 8) {
      showMessage(
        'Password must contain at least 8 characters',
      );
      return;
    }

    if (password != confirmPassword) {
      showMessage('Passwords do not match');
      return;
    }

    // -----------------------------
    // ARTIST VALIDATION
    // -----------------------------

    if (isArtist && widget.artistLevel == null) {
      showMessage('Artist level is missing');
      return;
    }

    // -----------------------------
    // CERTIFICATE VALIDATION
    // -----------------------------

    if (certificateRequired && selectedCertificate == null) {
      showMessage('Please upload your certificate');
      return;
    }

    setState(() {
      isLoading = true;
    });

    try {
      // -----------------------------
      // STEP 1: REGISTER ACCOUNT
      // -----------------------------

      final result = await _authService.register(
        fullName: fullName,
        email: email,
        password: password,
        accountType: widget.accountType,
        artistLevel: widget.artistLevel,
      );

      debugPrint('REGISTER: $result');

      // -----------------------------
      // STEP 2: CERTIFICATE UPLOAD
      // -----------------------------

      if (certificateRequired && selectedCertificate != null) {
        debugPrint(
          'REGISTER: Logging in to upload certificate...',
        );

        // Login automatically after registration
        await _authService.login(
          email,
          password,
        );

        debugPrint(
          'REGISTER: Login successful. Uploading certificate...',
        );

        // Upload certificate
        final uploadResult =
        await _authService.uploadCertificate(
          selectedCertificate!,
        );

        debugPrint(
          'REGISTER: $uploadResult',
        );
      }

      if (!mounted) return;

      showMessage(
        certificateRequired
            ? 'Registration successful. Certificate submitted for verification.'
            : 'Registration successful.',
      );

      await Future.delayed(
        const Duration(milliseconds: 1200),
      );

      if (!mounted) return;

      Navigator.pop(context);
    } catch (e) {
      if (!mounted) return;

      showMessage(
        e.toString().replaceFirst(
          'Exception: ',
          '',
        ),
      );
    } finally {
      if (mounted) {
        setState(() {
          isLoading = false;
        });
      }
    }
  }

  void showMessage(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
      ),
    );
  }

  String get screenTitle {
    if (!isArtist) {
      return 'Create Account';
    }

    if (widget.artistLevel == 'BEGINNER') {
      return 'Create Artist Account';
    }

    if (widget.artistLevel == 'INTERMEDIATE') {
      return 'Intermediate Artist';
    }

    if (widget.artistLevel == 'PROFESSIONAL') {
      return 'Professional Artist';
    }

    return 'Create Artist Account';
  }

  String get buttonText {
    if (!isArtist) {
      return 'Create User Account';
    }

    if (widget.artistLevel == 'BEGINNER') {
      return 'Create Artist Account';
    }

    return 'Submit for Verification';
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF9F7FC),

      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        foregroundColor: Colors.black,
        title: Text(
          screenTitle,
          style: const TextStyle(
            fontWeight: FontWeight.bold,
          ),
        ),
      ),

      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),

          child: Column(
            crossAxisAlignment:
            CrossAxisAlignment.start,

            children: [
              const SizedBox(height: 15),

              Text(
                isArtist
                    ? 'Join ArtVerse as an Artist'
                    : 'Join ArtVerse',
                style: const TextStyle(
                  fontSize: 30,
                  fontWeight: FontWeight.bold,
                ),
              ),

              const SizedBox(height: 8),

              Text(
                isArtist
                    ? 'Create your artist profile and showcase your creativity.'
                    : 'Create your account and explore the world of art.',
                style: TextStyle(
                  fontSize: 15,
                  color: Colors.grey.shade600,
                ),
              ),

              const SizedBox(height: 25),

              // ARTIST LEVEL DISPLAY
              if (isArtist)
                _buildArtistLevelCard(),

              const SizedBox(height: 25),

              // FULL NAME
              buildField(
                controller: fullNameController,
                label: 'Full Name',
                icon: Icons.person_outline,
              ),

              const SizedBox(height: 18),

              // EMAIL
              buildField(
                controller: emailController,
                label: 'Email',
                icon: Icons.email_outlined,
                keyboardType:
                TextInputType.emailAddress,
              ),

              const SizedBox(height: 18),

              // PASSWORD
              buildField(
                controller: passwordController,
                label: 'Password',
                icon: Icons.lock_outline,
                obscureText: obscurePassword,
                suffixIcon: IconButton(
                  icon: Icon(
                    obscurePassword
                        ? Icons.visibility_off
                        : Icons.visibility,
                  ),
                  onPressed: () {
                    setState(() {
                      obscurePassword =
                      !obscurePassword;
                    });
                  },
                ),
              ),

              const SizedBox(height: 18),

              // CONFIRM PASSWORD
              buildField(
                controller: confirmPasswordController,
                label: 'Confirm Password',
                icon: Icons.lock_outline,
                obscureText:
                obscureConfirmPassword,
                suffixIcon: IconButton(
                  icon: Icon(
                    obscureConfirmPassword
                        ? Icons.visibility_off
                        : Icons.visibility,
                  ),
                  onPressed: () {
                    setState(() {
                      obscureConfirmPassword =
                      !obscureConfirmPassword;
                    });
                  },
                ),
              ),

              const SizedBox(height: 25),

              // CERTIFICATE SECTION
              if (certificateRequired)
                _buildCertificateSection(),

              if (certificateRequired)
                const SizedBox(height: 30),

              // REGISTER BUTTON
              SizedBox(
                width: double.infinity,
                height: 55,

                child: ElevatedButton(
                  onPressed:
                  isLoading ? null : registerUser,

                  style:
                  ElevatedButton.styleFrom(
                    backgroundColor:
                    const Color(0xFF6C2BD9),

                    foregroundColor:
                    Colors.white,

                    shape:
                    RoundedRectangleBorder(
                      borderRadius:
                      BorderRadius.circular(14),
                    ),
                  ),

                  child: isLoading
                      ? const SizedBox(
                    width: 24,
                    height: 24,
                    child:
                    CircularProgressIndicator(
                      color: Colors.white,
                      strokeWidth: 2.5,
                    ),
                  )
                      : Text(
                    buttonText,
                    style:
                    const TextStyle(
                      fontSize: 16,
                      fontWeight:
                      FontWeight.bold,
                    ),
                  ),
                ),
              ),

              const SizedBox(height: 20),

              Center(
                child: TextButton(
                  onPressed: () {
                    Navigator.pop(context);
                  },
                  child: const Text(
                    'Already have an account? Login',
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildArtistLevelCard() {
    String level = widget.artistLevel ?? '';

    String description;

    if (level == 'BEGINNER') {
      description =
      'You can start creating your artist profile without certificate verification.';
    } else if (level == 'INTERMEDIATE') {
      description =
      'Certificate verification is required for Intermediate artists.';
    } else {
      description =
      'Professional qualification certificate is required for verification.';
    }

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),

      decoration: BoxDecoration(
        color: const Color(0xFFF0E8FF),
        borderRadius: BorderRadius.circular(16),
      ),

      child: Row(
        crossAxisAlignment:
        CrossAxisAlignment.start,

        children: [
          const Icon(
            Icons.palette_outlined,
            color: Color(0xFF6C2BD9),
            size: 28,
          ),

          const SizedBox(width: 12),

          Expanded(
            child: Column(
              crossAxisAlignment:
              CrossAxisAlignment.start,

              children: [
                Text(
                  'Artist Level: $level',
                  style: const TextStyle(
                    fontSize: 17,
                    fontWeight: FontWeight.bold,
                  ),
                ),

                const SizedBox(height: 5),

                Text(
                  description,
                  style: TextStyle(
                    fontSize: 13,
                    color: Colors.grey.shade700,
                    height: 1.4,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCertificateSection() {
    return Column(
      crossAxisAlignment:
      CrossAxisAlignment.start,

      children: [
        const Text(
          'Certificate',
          style: TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.bold,
          ),
        ),

        const SizedBox(height: 8),

        Text(
          'Upload a certificate for artist verification.',
          style: TextStyle(
            fontSize: 13,
            color: Colors.grey.shade600,
          ),
        ),

        const SizedBox(height: 12),

        InkWell(
          onTap: isLoading
              ? null
              : pickCertificate,

          borderRadius:
          BorderRadius.circular(14),

          child: Container(
            width: double.infinity,
            padding: const EdgeInsets.all(18),

            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius:
              BorderRadius.circular(14),

              border: Border.all(
                color: const Color(
                  0xFF6C2BD9,
                ),
              ),
            ),

            child: Row(
              children: [
                const Icon(
                  Icons.upload_file,
                  color:
                  Color(0xFF6C2BD9),
                  size: 30,
                ),

                const SizedBox(width: 14),

                Expanded(
                  child: Column(
                    crossAxisAlignment:
                    CrossAxisAlignment.start,

                    children: [
                      Text(
                        selectedCertificate?.name ??
                            'Upload Certificate',
                        style:
                        const TextStyle(
                          fontWeight:
                          FontWeight.w600,
                        ),
                      ),

                      const SizedBox(height: 4),

                      Text(
                        selectedCertificate == null
                            ? 'PDF, JPG, JPEG or PNG'
                            : 'Certificate selected',
                        style: TextStyle(
                          fontSize: 12,
                          color:
                          Colors.grey.shade600,
                        ),
                      ),
                    ],
                  ),
                ),

                const Icon(
                  Icons.arrow_forward_ios,
                  size: 16,
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Widget buildField({
    required TextEditingController controller,
    required String label,
    required IconData icon,
    bool obscureText = false,
    TextInputType? keyboardType,
    Widget? suffixIcon,
  }) {
    return TextField(
      controller: controller,

      obscureText: obscureText,

      keyboardType: keyboardType,

      decoration: InputDecoration(
        labelText: label,

        prefixIcon: Icon(icon),

        suffixIcon: suffixIcon,

        filled: true,

        fillColor: Colors.white,

        border: OutlineInputBorder(
          borderRadius:
          BorderRadius.circular(14),

          borderSide: BorderSide.none,
        ),

        enabledBorder:
        OutlineInputBorder(
          borderRadius:
          BorderRadius.circular(14),

          borderSide: BorderSide.none,
        ),

        focusedBorder:
        OutlineInputBorder(
          borderRadius:
          BorderRadius.circular(14),

          borderSide:
          const BorderSide(
            color:
            Color(0xFF6C2BD9),
            width: 1.5,
          ),
        ),
      ),
    );
  }
}