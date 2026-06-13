package com.unimas.library.controller;

import com.unimas.library.entity.Loan;
import com.unimas.library.service.BookService;
import com.unimas.library.service.LoanService;
import com.unimas.library.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/** Loan module - borrow, return, history, search; auto-updates book availability. */
@Controller
@RequestMapping("/loans")
public class LoanController {

    private final LoanService loanService;
    private final BookService bookService;
    private final MemberService memberService;

    public LoanController(LoanService loanService, BookService bookService,
                          MemberService memberService) {
        this.loanService = loanService;
        this.bookService = bookService;
        this.memberService = memberService;
    }

    /** Full (non-paginated) lists for the member/book drop-downs on the loan form. */
    private void addFormLists(Model model) {
        model.addAttribute("bookOptions", bookService.findAll());
        model.addAttribute("memberOptions", memberService.findAll());
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "0") int page, Model model) {
        loanService.refreshOverdueNow();
        model.addAttribute("loans", loanService.page(keyword, page));
        model.addAttribute("keyword", keyword);
        return "loans/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        Loan loan = new Loan();
        loan.setLoanDate(LocalDate.now());
        model.addAttribute("loan", loan);
        model.addAttribute("formAction", "/loans");
        model.addAttribute("pageTitle", "Borrow Book");
        addFormLists(model);
        return "loans/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("loan") Loan loan, BindingResult result,
                         Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/loans");
            model.addAttribute("pageTitle", "Borrow Book");
            addFormLists(model);
            return "loans/form";
        }
        try {
            loanService.create(loan);
            ra.addFlashAttribute("success", "Loan recorded successfully.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/loans";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("loan", loanService.findById(id));
        model.addAttribute("formAction", "/loans/" + id);
        model.addAttribute("pageTitle", "Edit Loan");
        addFormLists(model);
        return "loans/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("loan") Loan loan,
                         BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/loans/" + id);
            model.addAttribute("pageTitle", "Edit Loan");
            addFormLists(model);
            return "loans/form";
        }
        loanService.update(id, loan);
        ra.addFlashAttribute("success", "Loan updated successfully.");
        return "redirect:/loans";
    }

    @PostMapping("/{id}/return")
    public String returnBook(@PathVariable Long id, RedirectAttributes ra) {
        loanService.returnBook(id);
        ra.addFlashAttribute("success", "Book returned successfully.");
        return "redirect:/loans";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        loanService.delete(id);
        ra.addFlashAttribute("success", "Loan record deleted.");
        return "redirect:/loans";
    }
}
