package com.unimas.library.controller;

import com.unimas.library.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Analytics dashboard: stat cards, today's counters, monthly chart, recent activity feed. */
@Controller
public class DashboardController {

    private final ReportService reportService;
    private final LoanService loanService;
    private final MemberService memberService;
    private final BookService bookService;

    public DashboardController(ReportService reportService, LoanService loanService,
                               MemberService memberService, BookService bookService) {
        this.reportService = reportService;
        this.loanService = loanService;
        this.memberService = memberService;
        this.bookService = bookService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        loanService.refreshOverdueNow();   // statuses always current, never stale
        model.addAttribute("stats", reportService.stats());
        model.addAttribute("borrowedToday", loanService.borrowedToday());
        model.addAttribute("returnedToday", loanService.returnedToday());
        model.addAttribute("newMembersThisMonth", memberService.newThisMonth());
        model.addAttribute("recentLoans", loanService.recent());
        model.addAttribute("latestMembers", memberService.latest());
        model.addAttribute("recentBooks", bookService.recent());
        model.addAttribute("topBooks", reportService.mostBorrowedBooks(5));
        model.addAttribute("monthlyLoans", loanService.monthlyLoanCounts(6));
        return "dashboard";
    }
}
