package com.pingsso.app.config;

import com.pingsso.app.entity.User;
import com.pingsso.app.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataInitializer {

    private final UserRepository userRepository;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public CommandLineRunner initializeTestData() {
        return args -> {
            // Clear existing data
            userRepository.deleteAll();

            // Create sample users
            User user1 = new User();
            user1.setEmail("john.doe@example.com");
            user1.setName("John Doe");
            user1.setPicture("https://via.placeholder.com/150?text=John");
            user1.setPingSsoId("pingsso-001");
            user1.setActive(true);
            Set<String> roles1 = new HashSet<>();
            roles1.add("ROLE_USER");
            roles1.add("ROLE_ADMIN");
            user1.setRoles(roles1);
            user1.setCreatedAt(LocalDateTime.now().minusDays(30));
            user1.setLastLoginTime(LocalDateTime.now().minusDays(1));
            user1.setLastLoginIp("192.168.1.100");

            User user2 = new User();
            user2.setEmail("jane.smith@example.com");
            user2.setName("Jane Smith");
            user2.setPicture("https://via.placeholder.com/150?text=Jane");
            user2.setPingSsoId("pingsso-002");
            user2.setActive(true);
            Set<String> roles2 = new HashSet<>();
            roles2.add("ROLE_USER");
            user2.setRoles(roles2);
            user2.setCreatedAt(LocalDateTime.now().minusDays(20));
            user2.setLastLoginTime(LocalDateTime.now().minusHours(5));
            user2.setLastLoginIp("192.168.1.101");

            User user3 = new User();
            user3.setEmail("bob.wilson@example.com");
            user3.setName("Bob Wilson");
            user3.setPicture("https://via.placeholder.com/150?text=Bob");
            user3.setPingSsoId("pingsso-003");
            user3.setActive(true);
            Set<String> roles3 = new HashSet<>();
            roles3.add("ROLE_USER");
            roles3.add("ROLE_MANAGER");
            user3.setRoles(roles3);
            user3.setCreatedAt(LocalDateTime.now().minusDays(15));
            user3.setLastLoginTime(LocalDateTime.now().minusHours(2));
            user3.setLastLoginIp("192.168.1.102");

            User user4 = new User();
            user4.setEmail("alice.johnson@example.com");
            user4.setName("Alice Johnson");
            user4.setPicture("https://via.placeholder.com/150?text=Alice");
            user4.setPingSsoId("pingsso-004");
            user4.setActive(true);
            Set<String> roles4 = new HashSet<>();
            roles4.add("ROLE_USER");
            user4.setRoles(roles4);
            user4.setCreatedAt(LocalDateTime.now().minusDays(10));
            user4.setLastLoginTime(LocalDateTime.now().minusHours(12));
            user4.setLastLoginIp("192.168.1.103");

            // Save users
            userRepository.save(user1);
            userRepository.save(user2);
            userRepository.save(user3);
            userRepository.save(user4);

            System.out.println("Sample test data initialized successfully!");
        };
    }
}
