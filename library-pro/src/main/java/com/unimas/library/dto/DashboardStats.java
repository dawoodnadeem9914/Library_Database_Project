package com.unimas.library.dto;

/** Aggregated counters shown on the dashboard and reports cards. */
public record DashboardStats(
        long totalBooks,
        long totalCopies,
        long availableBooks,
        long totalMembers,
        long activeLoans,
        long returnedBooks,
        long overdueBooks) { }
