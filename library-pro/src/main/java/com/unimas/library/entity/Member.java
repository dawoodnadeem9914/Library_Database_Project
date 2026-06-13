package com.unimas.library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** MEMBERS table - registered library members (students and staff). */
@Entity
@Table(name = "MEMBERS")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_seq")
    @SequenceGenerator(name = "member_seq", sequenceName = "MEMBER_SEQ", allocationSize = 1)
    @Column(name = "MEMBER_ID")
    private Long id;

    @NotBlank(message = "Name is required") @Size(max = 120)
    @Column(name = "FULL_NAME", nullable = false, length = 120)
    private String fullName;

    @NotBlank(message = "Email is required") @Email(message = "Enter a valid email")
    @Column(name = "EMAIL", nullable = false, unique = true, length = 120)
    private String email;

    @NotBlank(message = "Phone is required") @Size(max = 20)
    @Column(name = "PHONE", nullable = false, length = 20)
    private String phone;

    @NotBlank(message = "Matric / staff number is required") @Size(max = 20)
    @Column(name = "MATRIC_NO", nullable = false, unique = true, length = 20)
    private String matricNo;

    @NotBlank(message = "Member type is required")
    @Column(name = "MEMBER_TYPE", nullable = false, length = 20)
    private String memberType;   // STUDENT / STAFF

    @Size(max = 500)
    @Column(name = "PHOTO_URL", length = 500)
    private String photoUrl;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Loan> loans = new ArrayList<>();

    @Column(name = "CREATED_BY", length = 50, updatable = false)
    private String createdBy;

    @Column(name = "UPDATED_BY", length = 50)
    private String updatedBy;

    public String getCreatedBy() { return createdBy; }
    public String getUpdatedBy() { return updatedBy; }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now(); this.updatedAt = this.createdAt;
        this.createdBy = com.unimas.library.config.AuditorUtil.currentUser();
        this.updatedBy = this.createdBy;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = com.unimas.library.config.AuditorUtil.currentUser();
    }

    public Member() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getMatricNo() { return matricNo; }
    public void setMatricNo(String matricNo) { this.matricNo = matricNo; }
    public String getMemberType() { return memberType; }
    public void setMemberType(String memberType) { this.memberType = memberType; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<Loan> getLoans() { return loans; }
}
