package com.pingsso.app.security;

import com.pingsso.app.service.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Optional;

@Component
public class SessionFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SessionFilter.class);
    private final SessionService sessionService;

    public SessionFilter(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Value("${session.cookie-name:PINGSSO_SESSION}")
    private String sessionCookieName;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String sessionId = extractSessionIdFromCookie(request);

            if (sessionId != null) {
                // Validate session exists and is active
                var session = sessionService.getSession(sessionId);

                if (session.isPresent()) {
                    // Session is valid, add to request attribute
                    request.setAttribute("sessionId", sessionId);
                    request.setAttribute("userId", session.get().getUserId());
                    request.setAttribute("userEmail", session.get().getEmail());
                    log.debug("Valid session found for sessionId: {}", sessionId);
                } else {
                    log.debug("Invalid or expired session: {}", sessionId);
                    // Remove invalid session cookie
                    removeSessionCookie(response);
                }
            }
        } catch (Exception ex) {
            log.error("Error processing session", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract session ID from cookies
     */
    private String extractSessionIdFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (sessionCookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Remove session cookie from response
     */
    private void removeSessionCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(sessionCookieName, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
