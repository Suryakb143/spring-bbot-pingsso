package com.pingsso.app.repository;

import com.pingsso.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPingSsoId(String pingSsoId);
    boolean existsByEmail(String email);
}
