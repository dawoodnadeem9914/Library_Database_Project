package com.unimas.library.service;

import com.unimas.library.dto.DashboardStats;
import com.unimas.library.dto.RankedItem;
import com.unimas.library.repository.BookRepository;
import com.unimas.library.repository.MemberRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Aggregation queries powering the dashboard and the reports page. */
@Service
@Transactional(readOnly = true)
public class ReportService {

    private final BookService bookService;
    private final MemberService memberService;
    private final LoanService loanService;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    public ReportService(BookService bookService, MemberService memberService,
                         LoanService loanService, BookRepository bookRepository,
                         MemberRepository memberRepository) {
        this.bookService = bookService;
        this.memberService = memberService;
        this.loanService = loanService;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    public DashboardStats stats() {
        return new DashboardStats(
                bookService.count(),
                bookService.totalCopies(),
                bookService.availableCopies(),
                memberService.count(),
                loanService.countActive(),
                loanService.countReturned(),
                loanService.countOverdue());
    }

    public List<RankedItem> mostBorrowedBooks(int limit) {
        return bookRepository.mostBorrowed(PageRequest.of(0, limit)).stream()
                .map(r -> new RankedItem((String) r[0], (String) r[1], (Long) r[2]))
                .toList();
    }

    public List<RankedItem> mostActiveMembers(int limit) {
        return memberRepository.mostActive(PageRequest.of(0, limit)).stream()
                .map(r -> new RankedItem((String) r[0], (String) r[1], (Long) r[2]))
                .toList();
    }

    public long totalLoans() { return loanService.count(); }
}
