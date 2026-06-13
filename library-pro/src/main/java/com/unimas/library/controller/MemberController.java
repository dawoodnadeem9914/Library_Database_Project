package com.unimas.library.controller;

import com.unimas.library.entity.Member;
import com.unimas.library.service.FileStorageService;
import com.unimas.library.service.LoanService;
import com.unimas.library.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Member module - full CRUD with paginated search and profile view. */
@Controller
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final LoanService loanService;
    private final FileStorageService fileStorageService;

    public MemberController(MemberService memberService, LoanService loanService,
                            FileStorageService fileStorageService) {
        this.memberService = memberService;
        this.loanService = loanService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("members", memberService.page(keyword, page));
        model.addAttribute("keyword", keyword);
        return "members/list";
    }

    @GetMapping("/{id:\\d+}")
    public String view(@PathVariable Long id, Model model) {
        Member member = memberService.findById(id);
        model.addAttribute("member", member);
        model.addAttribute("activeLoanCount", loanService.countActiveForMember(id));
        model.addAttribute("totalFines", loanService.totalFines(member.getLoans()));
        return "members/view";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("member", new Member());
        model.addAttribute("formAction", "/members");
        model.addAttribute("pageTitle", "Register New Member");
        return "members/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("member") Member member, BindingResult result,
                         @org.springframework.web.bind.annotation.RequestParam(value = "photoFile", required = false)
                         org.springframework.web.multipart.MultipartFile photoFile,
                         Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/members");
            model.addAttribute("pageTitle", "Register New Member");
            return "members/form";
        }
        try {
            String uploaded = fileStorageService.store(photoFile, "members");
            if (uploaded != null) member.setPhotoUrl(uploaded);
            memberService.create(member);
            ra.addFlashAttribute("success", "Member " + member.getFullName() + " registered successfully.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/members";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("member", memberService.findById(id));
        model.addAttribute("formAction", "/members/" + id);
        model.addAttribute("pageTitle", "Edit Member");
        return "members/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("member") Member member,
                         BindingResult result,
                         @org.springframework.web.bind.annotation.RequestParam(value = "photoFile", required = false)
                         org.springframework.web.multipart.MultipartFile photoFile,
                         Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/members/" + id);
            model.addAttribute("pageTitle", "Edit Member");
            return "members/form";
        }
        try {
            String uploaded = fileStorageService.store(photoFile, "members");
            if (uploaded != null) member.setPhotoUrl(uploaded);
            memberService.update(id, member);
            ra.addFlashAttribute("success", "Member updated successfully.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/members";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            memberService.delete(id);
            ra.addFlashAttribute("success", "Member deleted successfully.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/members";
    }
}
