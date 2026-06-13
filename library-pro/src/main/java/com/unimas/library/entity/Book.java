package com.unimas.library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** BOOKS table - library catalogue with cover image and audit timestamps. */
@Entity
@Table(name = "BOOKS")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "book_seq")
    @SequenceGenerator(name = "book_seq", sequenceName = "BOOK_SEQ", allocationSize = 1)
    @Column(name = "BOOK_ID")
    private Long id;

    @NotBlank(message = "Title is required") @Size(max = 200)
    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @NotBlank(message = "Author is required") @Size(max = 120)
    @Column(name = "AUTHOR", nullable = false, length = 120)
    private String author;

    @NotBlank(message = "ISBN is required")
    @Pattern(regexp = "^(?:\\d{9}[\\dXx]|\\d{13})$",
             message = "ISBN must be 10 or 13 digits (ISBN-10 may end in X)")
    @Column(name = "ISBN", nullable = false, unique = true, length = 20)
    private String isbn;

    @NotBlank(message = "Category is required") @Size(max = 60)
    @Column(name = "CATEGORY", nullable = false, length = 60)
    private String category;

    @Size(max = 1000)
    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Size(max = 120)
    @Column(name = "PUBLISHER", length = 120)
    private String publisher;

    @Size(max = 30)
    @Column(name = "LANGUAGE_CODE", length = 30)
    private String language;

    @Size(max = 30)
    @Column(name = "EDITION", length = 30)
    private String edition;

    @Size(max = 30)
    @Column(name = "SHELF_LOCATION", length = 30)
    private String shelfLocation;

    @Min(1900) @Max(2100)
    @Column(name = "PUBLICATION_YEAR")
    private Integer publicationYear;

    @NotNull(message = "Total copies is required")
    @Min(value = 1, message = "There must be at least 1 copy")
    @Column(name = "TOTAL_COPIES", nullable = false)
    private Integer totalCopies;

    @Column(name = "AVAILABLE_COPIES", nullable = false)
    private Integer availableCopies;

    @Size(max = 500)
    @Column(name = "IMAGE_URL", length = 500)
    private String imageUrl;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "CREATED_BY", length = 50, updatable = false)
    private String createdBy;

    @Column(name = "UPDATED_BY", length = 50)
    private String updatedBy;

    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private List<Loan> loans = new ArrayList<>();

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.createdBy = com.unimas.library.config.AuditorUtil.currentUser();
        this.updatedBy = this.createdBy;
        if (this.availableCopies == null) this.availableCopies = this.totalCopies;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = com.unimas.library.config.AuditorUtil.currentUser();
    }

    public Book() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getEdition() { return edition; }
    public void setEdition(String edition) { this.edition = edition; }
    public String getShelfLocation() { return shelfLocation; }
    public void setShelfLocation(String shelfLocation) { this.shelfLocation = shelfLocation; }
    public String getCreatedBy() { return createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public Integer getPublicationYear() { return publicationYear; }
    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }
    public Integer getTotalCopies() { return totalCopies; }
    public void setTotalCopies(Integer totalCopies) { this.totalCopies = totalCopies; }
    public Integer getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(Integer availableCopies) { this.availableCopies = availableCopies; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<Loan> getLoans() { return loans; }
}
