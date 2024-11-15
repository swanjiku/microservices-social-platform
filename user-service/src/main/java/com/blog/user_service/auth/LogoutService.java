package com.blog.user_service.auth;

import com.blog.user_service.token.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

    private final TokenRepository tokenRepository;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        log.info("Initiating logout process");
        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("No Bearer token found in request header");
            return;
        }

        jwt = authHeader.substring(7);
        log.debug("Extracted JWT token from header: {}", jwt);

        var storedToken = tokenRepository.findByToken(jwt)
                .orElse(null);

        if (storedToken != null) {
            log.info("Token found in repository, invalidating token: {}", jwt);
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
            log.debug("Token set as expired and revoked, and saved to repository");

            SecurityContextHolder.clearContext();
            log.info("Security context cleared, user successfully logged out");
        } else {
            log.warn("Token not found in repository, logout process aborted");
        }
    }
}
