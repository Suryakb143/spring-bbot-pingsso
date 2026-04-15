package com.pingsso.app.service;

import com.pingsso.app.entity.Session;
import com.pingsso.app.entity.User;
import com.pingsso.app.repository.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);
    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Value("${session.timeout-minutes:1440}")
    private long sessionTimeoutMinutes;

    @Value("${session.cookie-name:PINGSSO_SESSION}")
    private String sessionCookieName;

    /**
     * Create a new session for the user
     */
    public Session createSession(User user, HttpServletRequest request) {
        String sessionId = generateSessionId();

        Session session = new Session();
        session.setSessionId(sessionId);
        session.setUserId(user.getId());
        session.setEmail(user.getEmail());
        session.setIpAddress(getClientIP(request));
        session.setUserAgent(request.getHeader("User-Agent"));
        session.setCreatedAt(LocalDateTime.now());
        session.setLastAccessedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusMinutes(sessionTimeoutMinutes));
        session.setActive(true);

        Session savedSession = sessionRepository.save(session);
        log.info("Session created for user: {} with sessionId: {}", user.getEmail(), sessionId);

        return savedSession;
    }

    /**
     * Get session by session ID
     */
    public Optional<Session> getSession(String sessionId) {
        Optional<Session> session = sessionRepository.findBySessionId(sessionId);

        if (session.isPresent() && session.get().isValid()) {
            // Update last accessed time
            session.get().refreshLastAccessedTime();
            sessionRepository.save(session.get());
            return session;
        }

        return Optional.empty();
    }

    /**
     * Get all active sessions for a user
     */
    public List<Session> getActiveSessionsForUser(Long userId) {
        return sessionRepository.findActiveSessionsByUserId(userId);
    }

    /**
     * Get all sessions for a user (including expired)
     */
    public List<Session> getAllSessionsForUser(Long userId) {
        return sessionRepository.findByUserId(userId);
    }

    /**
     * Invalidate a specific session
     */
    public void invalidateSession(String sessionId) {
        Optional<Session> session = sessionRepository.findBySessionId(sessionId);

        if (session.isPresent()) {
            session.get().setActive(false);
            sessionRepository.save(session.get());
            log.info("Session invalidated: {}", sessionId);
        }
    }

    /**
     * Invalidate all sessions for a user (logout from all devices)
     */
    public void invalidateAllUserSessions(Long userId) {
        List<Session> sessions = sessionRepository.findByUserId(userId);

        for (Session session : sessions) {
            session.setActive(false);
        }

        sessionRepository.saveAll(sessions);
        log.info("All sessions invalidated for user ID: {}", userId);
    }

    /**
     * Refresh session expiration time
     */
    public void refreshSessionExpiration(String sessionId) {
        Optional<Session> session = sessionRepository.findBySessionId(sessionId);

        if (session.isPresent()) {
            session.get().setExpiresAt(LocalDateTime.now().plusMinutes(sessionTimeoutMinutes));
            sessionRepository.save(session.get());
            log.debug("Session expiration refreshed for sessionId: {}", sessionId);
        }
    }

    /**
     * Count active sessions for a user
     */
    public int countActiveSessionsForUser(Long userId) {
        return sessionRepository.countByUserIdAndActiveTrue(userId);
    }

    /**
     * Check if session belongs to user
     */
    public boolean isSessionOwnedByUser(String sessionId, Long userId) {
        Optional<Session> session = sessionRepository.findByUserIdAndSessionId(userId, sessionId);
        return session.isPresent() && session.get().isValid();
    }

    /**
     * Scheduled task to clean up expired sessions (runs every hour)
     */
    @Scheduled(fixedDelay = 3600000)
    public void cleanupExpiredSessions() {
        List<Session> expiredSessions = sessionRepository.findExpiredSessions();
        
        if (!expiredSessions.isEmpty()) {
            for (Session session : expiredSessions) {
                session.setActive(false);
            }
            sessionRepository.saveAll(expiredSessions);
            log.info("Cleaned up {} expired sessions", expiredSessions.size());
        }
    }

    /**
     * Generate a unique session ID
     */
    private String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Extract client IP address from request
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }
}
