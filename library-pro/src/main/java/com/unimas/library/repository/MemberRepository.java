package com.unimas.library.repository;

import com.unimas.library.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("SELECT m FROM Member m WHERE LOWER(m.fullName) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "OR LOWER(m.matricNo) LIKE LOWER(CONCAT('%', :kw, '%'))")
    Page<Member> search(@Param("kw") String keyword, Pageable pageable);

    Optional<Member> findByEmail(String email);
    Optional<Member> findByMatricNo(String matricNo);

    List<Member> findTop5ByOrderByCreatedAtDesc();
    long countByCreatedAtAfter(java.time.LocalDateTime from);

    /** Most active members report: name + loan count, descending. */
    @Query("SELECT m.fullName, m.matricNo, COUNT(l) FROM Loan l JOIN l.member m " +
           "GROUP BY m.id, m.fullName, m.matricNo ORDER BY COUNT(l) DESC")
    List<Object[]> mostActive(Pageable pageable);
}
