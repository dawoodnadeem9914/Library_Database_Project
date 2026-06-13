package com.unimas.library.controller;

import com.unimas.library.repository.BookRepository;
import com.unimas.library.service.BookService;
import com.unimas.library.service.LoanService;
import com.unimas.library.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

/** Reports module - enterprise analytics with Chart.js datasets. */
@Controller
public class ReportController {

    private final ReportService reportService;
    private final BookRepository bookRepository;
    private final BookService bookService;
    private final LoanService loanService;

    public ReportController(ReportService reportService, BookRepository bookRepository,
                            BookService bookService, LoanService loanService) {
        this.reportService = reportService;
        this.bookRepository = bookRepository;
        this.bookService = bookService;
        this.loanService = loanService;
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        loanService.refreshOverdueNow();
        model.addAttribute("stats", reportService.stats());
        model.addAttribute("totalLoans", reportService.totalLoans());
        model.addAttribute("topBooks", reportService.mostBorrowedBooks(7));
        model.addAttribute("topMembers", reportService.mostActiveMembers(7));
        model.addAttribute("monthlyLoans", loanService.monthlyLoanCounts(6));

        // Books by category
        List<String> catLabels = new ArrayList<>();
        List<Long> catCounts = new ArrayList<>();
        for (Object[] row : bookRepository.countByCategory()) {
            catLabels.add((String) row[0]);
            catCounts.add((Long) row[1]);
        }
        model.addAttribute("catLabels", catLabels);
        model.addAttribute("catCounts", catCounts);

        // Availability: available vs on loan
        long available = bookService.availableCopies();
        long total = bookService.totalCopies();
        model.addAttribute("copiesAvailable", available);
        model.addAttribute("copiesOnLoan", Math.max(0, total - available));
        return "reports";
    }
}
