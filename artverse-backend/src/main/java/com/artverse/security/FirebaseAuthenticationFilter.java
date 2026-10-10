
package com.artverse.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

@Component
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private final Firestore firestore;

    public FirebaseAuthenticationFilter(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();

        try {
            FirebaseToken decodedToken =
                    FirebaseAuth.getInstance().verifyIdToken(token);

            String email = decodedToken.getEmail();

            if (email == null || email.isBlank()) {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Firebase account has no email");
                return;
            }

            email = email.trim().toLowerCase(Locale.ROOT);

            DocumentSnapshot document = firestore
                    .collection("users")
                    .document(email)
                    .get()
                    .get();

            if (!document.exists()) {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "User profile not found");
                return;
            }

            String role = document.getString("role");

            if (role == null || role.isBlank()) {
                role = "USER";
            } else {
                role = role.trim();
            }
            var authorities = List.of(
                    new SimpleGrantedAuthority(
                            "ROLE_" + role.toUpperCase(Locale.ROOT)));

            var authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            authorities);

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        } catch (ExecutionException e) {
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load user profile");
            return;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "User lookup interrupted");
            return;
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid or expired Firebase ID token");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
