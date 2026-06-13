package com.unimas.library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** LOANS table - borrowing transactions linking MEMBERS and BOOKS. */
@Entity
@Table(name = "LOANS")
public class Loan {

    public static final String BORROWED = "BORROWED";
    public static final String RETURNED = "RETURNED";
    public static final String OVERDUE  = "OVERDUE";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "loan_seq")
    @SequenceGenerator(name = "loan_seq", sequenceName = "LOAN_SEQ", allocationSize = 1)
    @Column(name = "LOAN_ID")
    private Long id;

    @NotNull(message = "Please select a book")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "BOOK_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_LOAN_BOOK"))
    private Book book;

    @NotNull(message = "Please select a member")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "MEMBER_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_LOAN_MEMBER"))
    private Member member;

    @NotNull(message = "Loan date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "LOAN_DATE", nullable = false)
    private LocalDate loanDate;

    // Optional in the form: LoanService fills it as loanDate + maxLoanDays (SETTINGS)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "DUE_DATE", nullable = false)
    private LocalDate dueDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "RETURN_DATE")
    private LocalDate returnDate;

    @Column(name = "STATUS", nullable = false, length = 15)
    private String status = BORROWED;

    @Column(name = "CREATED_AT", updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private java.time.LocalDateTime updatedAt;

    @Column(name = "CREATED_BY", length = 50, updatable = false)
    private String createdBy;

    @Column(name = "UPDATED_BY", length = 50)
    private String updatedBy;

    public String getCreatedBy() { return createdBy; }
    public String getUpdatedBy() { return updatedBy; }

    @PrePersist
    void onCreate() {
        this.createdAt = java.time.LocalDateTime.now(); this.updatedAt = this.createdAt;
        this.createdBy = com.unimas.library.config.AuditorUtil.currentUser();
        this.updatedBy = this.createdBy;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = java.time.LocalDateTime.now();
        this.updatedBy = com.unimas.library.config.AuditorUtil.currentUser();
    }

    public Loan() { }

    /** Days past the due date (0 if not overdue). */
    public long getDaysOverdue() {
        LocalDate end = (returnDate != null) ? returnDate : LocalDate.now();
        if (dueDate == null || !end.isAfter(dueDate)) return 0;
        return ChronoUnit.DAYS.between(dueDate, end);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }
    public LocalDate getLoanDate() { return loanDate; }
    public void setLoanDate(LocalDate loanDate) { this.loanDate = loanDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
}
