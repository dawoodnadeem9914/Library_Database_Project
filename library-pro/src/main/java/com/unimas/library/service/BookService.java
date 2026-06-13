package com.unimas.library.service;

import com.unimas.library.entity.Book;
import com.unimas.library.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/** Book module: CRUD + paginated search/filter/sort + availability counters. */
@Service
@Transactional
public class BookService {

    public static final int PAGE_SIZE = 8;
    private static final Set<String> SORTABLE =
            Set.of("title", "author", "publicationYear", "availableCopies", "category", "createdAt");

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /** Builds a safe Sort from request params (whitelist prevents injection). */
    public static Sort sortOf(String sort, String dir) {
        String field = (sort != null && SORTABLE.contains(sort)) ? sort : "title";
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, field);
    }

    @Transactional(readOnly = true)
    public Page<Book> page(String keyword, String category, int page, String sort, String dir) {

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                PAGE_SIZE,
                sortOf(sort, dir)
        );

        if ((keyword == null || keyword.isBlank())
                && (category == null || category.isBlank())) {

            return bookRepository.findAll(pageable);
        }

        return bookRepository.search(
                norm(keyword),
                norm(category),
                pageable
        );
    }

    @Transactional(readOnly = true)
    public List<Book> searchAll(String keyword, String category) {
        return bookRepository.searchAll(norm(keyword), norm(category), Sort.by("title").ascending());
    }

    private static String norm(String s) {
        if (s == null) return null;
        String t = s.trim();
        return (t.isEmpty() || t.equalsIgnoreCase("null")) ? null : t;  // null = no filter (Oracle-safe)
    }

    @Transactional(readOnly = true)
    public List<String> categories() { return bookRepository.categories(); }

    @Transactional(readOnly = true)
    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + id));
    }

    public Book create(Book book) {
        validateUniqueIsbn(book.getIsbn(), null);
        book.setAvailableCopies(book.getTotalCopies());
        Book saved = bookRepository.save(book);
        return saved;
    }

    public Book update(Long id, Book form) {
        Book existing = findById(id);
        validateUniqueIsbn(form.getIsbn(), id);
        int borrowed = existing.getTotalCopies() - existing.getAvailableCopies();
        existing.setTitle(form.getTitle());
        existing.setAuthor(form.getAuthor());
        existing.setIsbn(form.getIsbn());
        existing.setCategory(form.getCategory());
        existing.setDescription(form.getDescription());
        existing.setPublisher(form.getPublisher());
        existing.setPublicationYear(form.getPublicationYear());
        if (form.getImageUrl() != null && !form.getImageUrl().isBlank()) {
            existing.setImageUrl(form.getImageUrl());
        }
        existing.setTotalCopies(form.getTotalCopies());
        existing.setAvailableCopies(Math.max(0, form.getTotalCopies() - borrowed));
        Book saved = bookRepository.save(existing);
        return saved;
    }

    public void delete(Long id) {
        Book book = findById(id);
        if (!book.getLoans().isEmpty()) {
            throw new IllegalStateException(
                    "Cannot delete \"" + book.getTitle() + "\" - loan records exist for this book.");
        }
        bookRepository.delete(book);
    }

    private void validateUniqueIsbn(String isbn, Long selfId) {
        bookRepository.findByIsbn(isbn).ifPresent(other -> {
            if (selfId == null || !other.getId().equals(selfId)) {
                throw new IllegalStateException("ISBN " + isbn + " is already registered.");
            }
        });
    }

    public void decrementAvailable(Book book) {
        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("No available copies of \"" + book.getTitle() + "\".");
        }
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);
    }

    public void incrementAvailable(Book book) {
        if (book.getAvailableCopies() < book.getTotalCopies()) {
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            bookRepository.save(book);
        }
    }

    @Transactional(readOnly = true)
    public List<Book> findAll() {
        return bookRepository.findAll(Sort.by("title").ascending());
    }

    @Transactional(readOnly = true)
    public List<Book> recent() { return bookRepository.findTop5ByOrderByCreatedAtDesc(); }

    @Transactional(readOnly = true)
    public long count() { return bookRepository.count(); }

    @Transactional(readOnly = true)
    public long totalCopies() { return bookRepository.totalCopies(); }

    @Transactional(readOnly = true)
    public long availableCopies() { return bookRepository.availableCopies(); }
}