package com.unimas.library.repository;

import com.unimas.library.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("SELECT b FROM Book b WHERE " +
            "(:kw IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :kw, '%')) " +
            "  OR LOWER(b.author) LIKE LOWER(CONCAT('%', :kw, '%')) " +
            "  OR b.isbn LIKE CONCAT('%', :kw, '%')) " +
            "AND (:cat IS NULL OR b.category = :cat)")
    Page<Book> search(@Param("kw") String keyword, @Param("cat") String category, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE " +
            "(:kw IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :kw, '%')) " +
            "  OR LOWER(b.author) LIKE LOWER(CONCAT('%', :kw, '%')) " +
            "  OR b.isbn LIKE CONCAT('%', :kw, '%')) " +
            "AND (:cat IS NULL OR b.category = :cat)")
    java.util.List<Book> searchAll(@Param("kw") String keyword, @Param("cat") String category,
                                   org.springframework.data.domain.Sort sort);

    @Query("SELECT DISTINCT b.category FROM Book b ORDER BY b.category")
    java.util.List<String> categories();

    @Query("SELECT b.category, COUNT(b) FROM Book b GROUP BY b.category ORDER BY COUNT(b) DESC")
    java.util.List<Object[]> countByCategory();

    java.util.List<Book> findTop5ByOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(SUM(b.availableCopies),0) FROM Book b")
    long availableCopies();

    Optional<Book> findByIsbn(String isbn);

    /** Most borrowed books report: title + loan count, descending. */
    @Query("SELECT b.title, b.author, COUNT(l) FROM Loan l JOIN l.book b " +
            "GROUP BY b.id, b.title, b.author ORDER BY COUNT(l) DESC")
    List<Object[]> mostBorrowed(Pageable pageable);

    @Query("SELECT COALESCE(SUM(b.totalCopies),0) FROM Book b")
    long totalCopies();
}