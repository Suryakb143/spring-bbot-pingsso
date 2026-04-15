package com.pingsso.app.security;

import com.pingsso.app.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter for Bearer token validation.
 * Validates access tokens from Authorization header and loads user roles from database.
 */
@Component
public class BearerTokenFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(BearerTokenFilter.class);

    private final CustomUserDetailsService customUserDetailsService;
    private final BearerTokenProvider bearerTokenProvider;

    public BearerTokenFilter(CustomUserDetailsService customUserDetailsService, BearerTokenProvider bearerTokenProvider) {
        this.customUserDetailsService = customUserDetailsService;
        this.bearerTokenProvider = bearerTokenProvider;
    }

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = extractTokenFromRequest(request);

            if (token != null) {
                // Validate Bearer token
                if (bearerTokenProvider.validateToken(token)) {
                    // Extract user email/username from token
                    String email = bearerTokenProvider.getEmailFromToken(token);

                    if (email != null && StringUtils.hasText(email)) {
                        // Load user details from database (includes roles)
                        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

                        // Create authentication token with user roles
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // Set authentication in security context
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.debug("Authenticated user: {} with Bearer token", email);
                    }
                } else {
                    log.debug("Invalid or expired Bearer token");
                }
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication from Bearer token", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract Bearer token from Authorization header
     * Expected format: Authorization: Bearer <token>
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}
