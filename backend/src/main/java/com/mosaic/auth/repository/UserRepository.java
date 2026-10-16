package com.mosaic.auth.repository;

import com.mosaic.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile LEFT JOIN FETCH u.preferences WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findByIdWithDetails(@Param("id") UUID id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile LEFT JOIN FETCH u.preferences WHERE LOWER(u.email) = LOWER(:email) AND u.deletedAt IS NULL")
    Optional<User> findByEmailWithDetails(@Param("email") String email);
}
