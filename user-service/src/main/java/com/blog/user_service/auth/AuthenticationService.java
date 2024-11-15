package com.blog.user_service.auth;


import com.blog.user_service.auth.DTO.AuthenticationRequest;
import com.blog.user_service.auth.DTO.AuthenticationResponse;
import com.blog.user_service.auth.DTO.RegisterRequest;
import com.blog.user_service.config.JwtService;
import com.blog.user_service.token.Token;
import com.blog.user_service.token.TokenRepository;
import com.blog.user_service.token.TokenType;
import com.blog.user_service.user.Role;
import com.blog.user_service.user.User;
import com.blog.user_service.user.UserRepository;
import com.blog.user_service.util.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    private final UserRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public ApiResponse<AuthenticationResponse> register(RegisterRequest request) {
        try {
            log.info("Starting user registration for email: {}", request.getEmail());

            // Validate email
            if (request.getEmail() == null || request.getEmail().isEmpty() || !isValidEmail(request.getEmail())) {
                return new ApiResponse<>("Invalid email format", null, HttpStatus.BAD_REQUEST.value());
            }

            // Check if email exists
            Optional<User> checkIfUserEmailExists = repository.findByEmail(request.getEmail());
            if (checkIfUserEmailExists.isPresent()) {
                return new ApiResponse<>("A user with this email already exists.", null, HttpStatus.BAD_REQUEST.value());
            }

            // Validate role
            try {
                Role.valueOf(request.getRole());
            } catch (IllegalArgumentException e) {
                return new ApiResponse<>("Invalid role: " + request.getRole(), null, HttpStatus.BAD_REQUEST.value());
            }

            // Build the user entity
            User user = User.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(Role.valueOf(request.getRole()))
                    .build();

            // Save the user to the repository
            User savedUser = repository.save(user);
            log.info("User registered successfully with email: {}", savedUser.getEmail());

            // Generate JWT and refresh tokens
            String jwtToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            saveUserToken(savedUser, jwtToken);

            log.info("JWT and Refresh tokens generated for user: {}", savedUser.getEmail());

            // Prepare authentication response
            AuthenticationResponse authResponse = AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .build();

            return new ApiResponse<>("Sign up successful", authResponse, HttpStatus.CREATED.value());
        } catch (Exception e) {
            log.error("An error occurred during registration", e);
            return new ApiResponse<>("An error occurred", null, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    public ApiResponse<AuthenticationResponse> authenticate(AuthenticationRequest request) {
        log.info("Authenticating user with email: {}", request.getEmail());

        // Try to find the user by email
        var userOptional = repository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            log.warn("User not found with email: {}", request.getEmail());
            return new ApiResponse<>("User not found", null, HttpStatus.NOT_FOUND.value());
        }

        var user = userOptional.get();
        log.info("User found with email: {}", user.getEmail());

        // Authenticate the user
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            log.info("Authentication successful for email: {}", request.getEmail());

        } catch (BadCredentialsException e) {
            log.error("Bad credentials for user: {}", request.getEmail());
            return new ApiResponse<>("Invalid credentials", null, HttpStatus.UNAUTHORIZED.value());
        }

        // Generate JWT and refresh tokens
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        log.info("JWT and refresh tokens generated for user: {}", user.getEmail());

        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        log.info("User tokens saved and revoked previous tokens for user: {}", user.getEmail());

        // Create AuthenticationResponse with tokens
        AuthenticationResponse authResponse = AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();

        log.info("User authenticated successfully: {}", user.getEmail());
        return new ApiResponse<>("Authentication successful", authResponse, HttpStatus.OK.value());
    }

    private void saveUserToken(User user, String jwtToken) {
        log.debug("Saving token for user: {}", user.getEmail());
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
        log.debug("Token saved successfully for user: {}", user.getEmail());
    }

    private void revokeAllUserTokens(User user) {
        log.debug("Revoking all tokens for user: {}", user.getEmail());
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty()) {
            log.debug("No valid tokens found for user: {}", user.getEmail());
            return;
        }
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
        log.debug("All tokens revoked for user: {}", user.getEmail());
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("Refreshing token");
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("No Bearer token found in request");
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            log.debug("Extracted email from refresh token: {}", userEmail);
            var user = this.repository.findByEmail(userEmail)
                    .orElseThrow(() -> {
                        log.error("User not found with email: {}", userEmail);
                        return new RuntimeException("User not found");
                    });
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
                log.info("Token refreshed successfully for user: {}", userEmail);
            } else {
                log.warn("Invalid refresh token for user: {}", userEmail);
            }
        } else {
            log.warn("No user email found in refresh token");
        }
    }

    public static boolean isValidEmail(@Nullable String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}

