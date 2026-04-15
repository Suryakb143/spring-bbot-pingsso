package com.pingsso.app.controller;

import com.pingsso.app.entity.Session;
import com.pingsso.app.entity.User;
import com.pingsso.app.security.BearerTokenProvider;
import com.pingsso.app.service.SessionService;
import com.pingsso.app.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@Tag(name = "Authentication", description = "Authentication and session management endpoints")
public class AuthController {

    private final UserService userService;
    private final SessionService sessionService;
    private final BearerTokenProvider bearerTokenProvider;
    private static final String SESSION_COOKIE_NAME = "PINGSSO_SESSION";

    public AuthController(UserService userService, SessionService sessionService, BearerTokenProvider bearerTokenProvider) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.bearerTokenProvider = bearerTokenProvider;
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user with email and PingSSO profile information. Returns Bearer token and session cookie.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful. Returns user info, session ID, and Bearer token."),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> login(@org.springframework.web.bind.annotation.RequestBody Map<String, String> loginRequest, 
                                  HttpServletRequest request, 
                                  HttpServletResponse response) {
        String email = loginRequest.get("email");
        String pingSsoId = loginRequest.get("pingSsoId");
        String name = loginRequest.get("name");
        String picture = loginRequest.get("picture");

        User user = userService.createOrUpdateUser(email, name, picture, pingSsoId);
        userService.recordLogin(user.getId(), request.getRemoteAddr());

        // Create new session
        Session session = sessionService.createSession(user, request);

        // Generate Bearer token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("roles", user.getRoles());
        String bearerToken = bearerTokenProvider.generateToken(email, claims);

        // Set session cookie
        setSessionCookie(response, session.getSessionId());

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Login successful");
        responseData.put("user", user);
        responseData.put("sessionId", session.getSessionId());
        responseData.put("expiresAt", session.getExpiresAt());
        responseData.put("bearerToken", bearerToken);
        responseData.put("tokenType", "Bearer");
        responseData.put("expiresIn", bearerTokenProvider.getTokenExpirationSeconds());

        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/callback")
    @Operation(summary = "Handle OAuth callback", description = "Process OAuth callback from PingSSO provider with user token data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Callback processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid callback data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> handleCallback(@org.springframework.web.bind.annotation.RequestBody Map<String, Object> tokenData,
                                           HttpServletRequest request,
                                           HttpServletResponse response) {
        @SuppressWarnings("unchecked")
        Map<String, Object> userInfo = (Map<String, Object>) tokenData.get("userInfo");

        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        String picture = (String) userInfo.get("picture");
        String sub = (String) userInfo.get("sub");

        User user = userService.createOrUpdateUser(email, name, picture, sub);
        userService.recordLogin(user.getId(), request.getRemoteAddr());

        // Create new session
        Session session = sessionService.createSession(user, request);

        // Generate Bearer token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("roles", user.getRoles());
        String bearerToken = bearerTokenProvider.generateToken(email, claims);

        // Set session cookie
        setSessionCookie(response, session.getSessionId());

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("user", user);
        responseData.put("sessionId", session.getSessionId());
        responseData.put("expiresAt", session.getExpiresAt());
        responseData.put("bearerToken", bearerToken);
        responseData.put("tokenType", "Bearer");
        responseData.put("expiresIn", bearerTokenProvider.getTokenExpirationSeconds());

        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Invalidate the current user session and remove session cookie.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid session")
    })
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String sessionId = (String) request.getAttribute("sessionId");

        if (sessionId != null) {
            sessionService.invalidateSession(sessionId);
        }

        // Remove session cookie
        removeSessionCookie(response);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Logout successful");

        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout all sessions", description = "Invalidate all sessions for the current user across all devices.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All sessions logged out"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid user")
    })
    public ResponseEntity<?> logoutAllSessions(HttpServletRequest request, HttpServletResponse response) {
        Long userId = (Long) request.getAttribute("userId");

        if (userId != null) {
            sessionService.invalidateAllUserSessions(userId);
        }

        // Remove session cookie
        removeSessionCookie(response);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Logged out from all sessions");

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/sessions")
    @Operation(summary = "Get active sessions", description = "Retrieve all active sessions for the current user.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of active sessions"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid user")
    })
    public ResponseEntity<?> getActiveSessions(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        List<Session> sessions = sessionService.getActiveSessionsForUser(userId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("sessions", sessions);
        responseData.put("count", sessions.size());

        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/sessions/{sessionId}/revoke")
    @Operation(summary = "Revoke a session", description = "Invalidate a specific session. Only the session owner can revoke their own sessions.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session revoked successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid user"),
            @ApiResponse(responseCode = "403", description = "Forbidden - session not owned by user")
    })
    public ResponseEntity<?> revokeSession(@PathVariable String sessionId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        if (!sessionService.isSessionOwnedByUser(sessionId, userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        sessionService.invalidateSession(sessionId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Session revoked successfully");

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/session-info")
    @Operation(summary = "Get session information", description = "Retrieve details about the current session.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session details retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no active session or invalid session")
    })
    public ResponseEntity<?> getSessionInfo(HttpServletRequest request) {
        String sessionId = (String) request.getAttribute("sessionId");

        if (sessionId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "No active session"));
        }

        Optional<Session> session = sessionService.getSession(sessionId);

        if (session.isPresent()) {
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("sessionId", session.get().getSessionId());
            responseData.put("userId", session.get().getUserId());
            responseData.put("email", session.get().getEmail());
            responseData.put("ipAddress", session.get().getIpAddress());
            responseData.put("createdAt", session.get().getCreatedAt());
            responseData.put("expiresAt", session.get().getExpiresAt());
            responseData.put("lastAccessedAt", session.get().getLastAccessedAt());

            return ResponseEntity.ok(responseData);
        }

        return ResponseEntity.status(401).body(Map.of("error", "Invalid session"));
    }

    @GetMapping("/user-info")
    @Operation(summary = "Get user information", description = "Retrieve user details by email address.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User information retrieved"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<?> getUserInfo(@RequestParam String email) {
        User user = userService.getUserByEmail(email);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/token")
    @Operation(summary = "Generate new token", description = "Generate a new Bearer token for an authenticated user.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bearer token generated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid user"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<?> generateToken(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String email = (String) request.getAttribute("userEmail");

        if (userId == null || email == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        User user = userService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        // Generate Bearer token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("roles", user.getRoles());
        String bearerToken = bearerTokenProvider.generateToken(email, claims);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("bearerToken", bearerToken);
        responseData.put("tokenType", "Bearer");
        responseData.put("expiresIn", bearerTokenProvider.getTokenExpirationSeconds());

        return ResponseEntity.ok(responseData);
    }

    /**
     * Set session cookie in response
     */
    private void setSessionCookie(HttpServletResponse response, String sessionId) {
        ResponseCookie cookie = ResponseCookie
                .from(SESSION_COOKIE_NAME, sessionId)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * Remove session cookie from response
     */
    private void removeSessionCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie
                .from(SESSION_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
