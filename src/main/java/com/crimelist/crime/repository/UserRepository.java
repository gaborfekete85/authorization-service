package com.crimelist.crime.repository;

import com.crimelist.crime.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.id IN (:userIds)")
    List<User> findByIds(List<UUID> userIds);

    Optional<User> findByEmailVerificationCode(String emailVerificationCode);
}
