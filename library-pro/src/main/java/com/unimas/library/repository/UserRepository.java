package com.unimas.library.repository;

import com.unimas.library.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    long countByRole(String role);

    @Query("SELECT u FROM User u WHERE " +
            "(:kw IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :kw, '%')) " +
            "  OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :kw, '%')) " +
            "  OR LOWER(u.email) LIKE LOWER(CONCAT('%', :kw, '%'))) " +
            "AND (:role IS NULL OR u.role = :role) " +
            "AND (:status IS NULL OR (:status = 'ACTIVE' AND u.active = true) " +
            "                  OR (:status = 'INACTIVE' AND u.active = false))")
    Page<User> search(@Param("kw") String keyword, @Param("role") String role,
                      @Param("status") String status, Pageable pageable);
}