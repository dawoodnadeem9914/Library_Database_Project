package com.unimas.library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** SETTINGS table - single-row system configuration. */
@Entity
@Table(name = "SETTINGS")
public class Setting {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "setting_seq")
    @SequenceGenerator(name = "setting_seq", sequenceName = "SETTING_SEQ", allocationSize = 1)
    @Column(name = "SETTING_ID")
    private Long id;

    @NotBlank @Size(max = 120)
    @Column(name = "LIBRARY_NAME", nullable = false, length = 120)
    private String libraryName = "UNIMAS Library";

    @NotBlank @Size(max = 120)
    @Column(name = "ADMIN_NAME", nullable = false, length = 120)
    private String adminName = "System Administrator";

    @NotBlank @Email
    @Column(name = "ADMIN_EMAIL", nullable = false, length = 120)
    private String adminEmail = "admin@library.unimas.my";

    @Size(max = 250)
    @Column(name = "LIBRARY_ADDRESS", length = 250)
    private String libraryAddress = "Universiti Malaysia Sarawak, 94300 Kota Samarahan, Sarawak";

    @Size(max = 25)
    @Column(name = "CONTACT_NUMBER", length = 25)
    private String contactNumber = "+60 82-581 000";

    @Email @Size(max = 120)
    @Column(name = "EMAIL_FROM", length = 120)
    private String emailFrom = "noreply@library.unimas.my";

    @Column(name = "EMAIL_NOTIFICATIONS", nullable = false)
    private Boolean emailNotifications = Boolean.FALSE;

    @NotNull @Min(1) @Max(90)
    @Column(name = "MAX_LOAN_DAYS", nullable = false)
    private Integer maxLoanDays = 14;

    @NotNull @Min(1) @Max(20)
    @Column(name = "MAX_BOOKS_PER_MEMBER", nullable = false)
    private Integer maxBooksPerMember = 3;

    @NotNull @DecimalMin("0.00")
    @Column(name = "FINE_PER_DAY", nullable = false, precision = 8, scale = 2)
    private BigDecimal finePerDay = new BigDecimal("0.50");

    @NotBlank
    @Column(name = "THEME_MODE", nullable = false, length = 10)
    private String themeMode = "dark";   // light / dark / auto

    public Setting() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLibraryName() { return libraryName; }
    public void setLibraryName(String libraryName) { this.libraryName = libraryName; }
    public String getAdminName() { return adminName; }
    public void setAdminName(String adminName) { this.adminName = adminName; }
    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }
    public Integer getMaxLoanDays() { return maxLoanDays; }
    public void setMaxLoanDays(Integer maxLoanDays) { this.maxLoanDays = maxLoanDays; }
    public Integer getMaxBooksPerMember() { return maxBooksPerMember; }
    public void setMaxBooksPerMember(Integer maxBooksPerMember) { this.maxBooksPerMember = maxBooksPerMember; }
    public BigDecimal getFinePerDay() { return finePerDay; }
    public void setFinePerDay(BigDecimal finePerDay) { this.finePerDay = finePerDay; }
    public String getLibraryAddress() { return libraryAddress; }
    public void setLibraryAddress(String libraryAddress) { this.libraryAddress = libraryAddress; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public String getEmailFrom() { return emailFrom; }
    public void setEmailFrom(String emailFrom) { this.emailFrom = emailFrom; }
    public Boolean getEmailNotifications() { return emailNotifications; }
    public void setEmailNotifications(Boolean emailNotifications) { this.emailNotifications = emailNotifications; }
    public String getThemeMode() { return themeMode; }
    public void setThemeMode(String themeMode) { this.themeMode = themeMode; }
}
