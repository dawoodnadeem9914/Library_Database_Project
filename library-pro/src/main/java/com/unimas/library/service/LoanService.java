package com.unimas.library.service;

import com.unimas.library.entity.Book;
import com.unimas.library.entity.Loan;
import com.unimas.library.entity.Setting;
import com.unimas.library.repository.LoanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Loan module: borrow / return / history.
 * Enforces the SETTINGS business rules:
 *  - due date = loan date + maxLoanDays
 *  - a member may hold at most maxBooksPerMember active loans
 *  - fine = daysOverdue * finePerDay
 * All copy-count updates happen inside one transaction.
 */
@Service
@Transactional
public class LoanService {

    public static final int PAGE_SIZE = 8;
    private static final List<String> ACTIVE = List.of(Loan.BORROWED, Loan.OVERDUE);

    private final LoanRepository loanRepository;
    private final BookService bookService;
    private final SettingsService settingsService;

    public LoanService(LoanRepository loanRepository, BookService bookService,
                       SettingsService settingsService) {
        this.loanRepository = loanRepository;
        this.bookService = bookService;
        this.settingsService = settingsService;
    }

    @Transactional(readOnly = true)
    public Page<Loan> page(String keyword, int page) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by("loanDate").descending());
        if (keyword == null || keyword.isBlank()) return loanRepository.findAll(pageable);
        return loanRepository.search(keyword.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public Loan findById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found: " + id));
    }

    /** Borrow a book - applies settings rules and decrements available copies. */
    public Loan create(Loan loan) {
        if (loan.getMember() == null || loan.getMember().getId() == null
                || loan.getBook() == null || loan.getBook().getId() == null) {
            throw new IllegalStateException("Please select both a member and a book.");
        }
        Setting s = settingsService.get();

        long activeForMember = loanRepository
                .countByMemberIdAndStatusIn(loan.getMember().getId(), ACTIVE);
        if (activeForMember >= s.getMaxBooksPerMember()) {
            throw new IllegalStateException("This member already holds the maximum of "
                    + s.getMaxBooksPerMember() + " active loans.");
        }

        Book book = bookService.findById(loan.getBook().getId());
        bookService.decrementAvailable(book);
        loan.setBook(book);

        if (loan.getLoanDate() == null) loan.setLoanDate(LocalDate.now());
        if (loan.getDueDate() == null) loan.setDueDate(loan.getLoanDate().plusDays(s.getMaxLoanDays()));
        loan.setStatus(Loan.BORROWED);
        Loan saved = loanRepository.save(loan);
        return saved;
    }

    public Loan update(Long id, Loan form) {
        Loan existing = findById(id);
        if (!existing.getBook().getId().equals(form.getBook().getId())
                && !Loan.RETURNED.equals(existing.getStatus())) {
            bookService.incrementAvailable(existing.getBook());
            Book newBook = bookService.findById(form.getBook().getId());
            bookService.decrementAvailable(newBook);
            existing.setBook(newBook);
        }
        existing.setMember(form.getMember());
        existing.setLoanDate(form.getLoanDate());
        existing.setDueDate(form.getDueDate() != null
                ? form.getDueDate()
                : form.getLoanDate().plusDays(settingsService.get().getMaxLoanDays()));
        return loanRepository.save(existing);
    }

    /** Return a book - increments available copies and records the date. */
    public Loan returnBook(Long id) {
        Loan loan = findById(id);
        if (Loan.RETURNED.equals(loan.getStatus())) return loan;
        loan.setStatus(Loan.RETURNED);
        loan.setReturnDate(LocalDate.now());
        bookService.incrementAvailable(loan.getBook());
        Loan saved = loanRepository.save(loan);
        return saved;
    }

    public void delete(Long id) {
        Loan loan = findById(id);
        if (!Loan.RETURNED.equals(loan.getStatus())) {
            bookService.incrementAvailable(loan.getBook());
        }
        loanRepository.delete(loan);
    }

    /** Fine owed for one loan based on the configured fine per day. */
    @Transactional(readOnly = true)
    public BigDecimal fineFor(Loan loan) {
        return settingsService.get().getFinePerDay()
                .multiply(BigDecimal.valueOf(loan.getDaysOverdue()));
    }

    /** Runs at startup-ish intervals: flips BORROWED loans past due date to OVERDUE. */
    @Scheduled(fixedDelay = 60 * 60 * 1000, initialDelay = 10 * 1000)
    public void refreshOverdueStatuses() {
        loanRepository.markOverdue(LocalDate.now());
    }

    /** Total outstanding fines for a set of loans (overdue and not yet settled). */
    @Transactional(readOnly = true)
    public BigDecimal totalFines(List<Loan> loans) {
        BigDecimal perDay = settingsService.get().getFinePerDay();
        return loans.stream()
                .map(l -> perDay.multiply(BigDecimal.valueOf(l.getDaysOverdue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public long countActiveForMember(Long memberId) {
        return loanRepository.countByMemberIdAndStatusIn(memberId, ACTIVE);
    }

    /** Loan counts per month for the last n months (oldest first) for the dashboard chart. */
    @Transactional(readOnly = true)
    public java.util.LinkedHashMap<String, Long> monthlyLoanCounts(int months) {
        java.time.YearMonth now = java.time.YearMonth.now();
        java.time.LocalDate from = now.minusMonths(months - 1).atDay(1);
        List<LocalDate> dates = loanRepository.loanDatesSince(from);
        java.util.LinkedHashMap<String, Long> result = new java.util.LinkedHashMap<>();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("MMM yyyy");
        for (int i = months - 1; i >= 0; i--) {
            java.time.YearMonth ym = now.minusMonths(i);
            long count = dates.stream().filter(d -> java.time.YearMonth.from(d).equals(ym)).count();
            result.put(ym.atDay(1).format(fmt), count);
        }
        return result;
    }

    /** Instant overdue refresh - called before dashboards/lists render. */
    public void refreshOverdueNow() {
        loanRepository.markOverdue(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public long borrowedToday() { return loanRepository.countByLoanDate(LocalDate.now()); }

    @Transactional(readOnly = true)
    public long returnedToday() { return loanRepository.countByReturnDate(LocalDate.now()); }

    @Transactional(readOnly = true)
    public long countActive() { return loanRepository.countByStatusIn(ACTIVE); }
    @Transactional(readOnly = true)
    public long countReturned() { return loanRepository.countByStatus(Loan.RETURNED); }
    @Transactional(readOnly = true)
    public long countOverdue() { return loanRepository.countByStatus(Loan.OVERDUE); }
    @Transactional(readOnly = true)
    public long count() { return loanRepository.count(); }
    @Transactional(readOnly = true)
    public List<Loan> recent() { return loanRepository.findTop5ByOrderByLoanDateDesc(); }
}
