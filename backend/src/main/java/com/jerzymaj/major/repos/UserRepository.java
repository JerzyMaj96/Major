package com.jerzymaj.major.repos;

import com.jerzymaj.major.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByName(String username);

    Optional<User> findByNameOrEmail(String username, String email);

    boolean existsByName(String username);

    boolean existsByEmail(String email);
}
