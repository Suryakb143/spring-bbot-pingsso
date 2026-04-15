package com.pingsso.app.repository;

import com.pingsso.app.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findBySessionId(String sessionId);

    List<Session> findByUserId(Long userId);

    List<Session> findByUserIdAndActiveTrue(Long userId);

    Optional<Session> findByUserIdAndSessionId(Long userId, String sessionId);

    @Query("SELECT s FROM Session s WHERE s.userId = :userId AND s.active = true AND s.expiresAt > CURRENT_TIMESTAMP")
    List<Session> findActiveSessionsByUserId(Long userId);

    @Query("SELECT s FROM Session s WHERE s.expiresAt <= CURRENT_TIMESTAMP AND s.active = true")
    List<Session> findExpiredSessions();

    void deleteByExpiresAtBefore(LocalDateTime expiresAt);

    int countByUserId(Long userId);

    int countByUserIdAndActiveTrue(Long userId);
}
