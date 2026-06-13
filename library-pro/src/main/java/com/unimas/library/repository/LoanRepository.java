package com.unimas.library.repository;

import com.unimas.library.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query("SELECT l FROM Loan l JOIN l.member m JOIN l.book b " +
           "WHERE LOWER(m.fullName) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "   OR LOWER(b.title)    LIKE LOWER(CONCAT('%', :kw, '%'))")
    Page<Loan> search(@Param("kw") String keyword, Pageable pageable);

    long countByStatus(String status);
    List<Loan> findByStatus(String status);
    long countByLoanDate(LocalDate date);
    long countByReturnDate(LocalDate date);
    long countByStatusIn(List<String> statuses);
    long countByMemberIdAndStatusIn(Long memberId, List<String> statuses);

    List<Loan> findTop5ByOrderByLoanDateDesc();

    @Query("SELECT l.loanDate FROM Loan l WHERE l.loanDate >= :from")
    List<LocalDate> loanDatesSince(@Param("from") LocalDate from);

    /** Flip BORROWED loans past their due date to OVERDUE. */
    @Modifying
    @Query("UPDATE Loan l SET l.status = 'OVERDUE' " +
           "WHERE l.status = 'BORROWED' AND l.dueDate < :today")
    int markOverdue(@Param("today") LocalDate today);
}
