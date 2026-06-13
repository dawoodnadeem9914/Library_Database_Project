package com.unimas.library.controller;

import com.unimas.library.entity.Book;
import com.unimas.library.service.BookService;
import com.unimas.library.service.ExportService;
import com.unimas.library.service.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Book module - CRUD, search by title/author/ISBN/category, sorting,
 * pagination, cover image upload and PDF/Excel export.
 */
@Controller
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final FileStorageService fileStorageService;
    private final ExportService exportService;

    public BookController(BookService bookService, FileStorageService fileStorageService,
                          ExportService exportService) {
        this.bookService = bookService;
        this.exportService = exportService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String category,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "title") String sort,
                       @RequestParam(defaultValue = "asc") String dir,
                       Model model) {
        Page<Book> books = bookService.page(keyword, category, page, sort, dir);
        System.out.println("TOTAL BOOKS FOUND = " + books.getTotalElements());
        model.addAttribute("books", books);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("categories", bookService.categories());
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("revDir", "asc".equalsIgnoreCase(dir) ? "desc" : "asc");
        return "books/list";
    }

    @GetMapping("/{id:\\d+}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("book", bookService.findById(id));
        return "books/view";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("formAction", "/books");
        model.addAttribute("pageTitle", "Add New Book");
        return "books/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("book") Book book, BindingResult result,
                         @RequestParam(value = "coverFile", required = false) MultipartFile coverFile,
                         Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/books");
            model.addAttribute("pageTitle", "Add New Book");
            return "books/form";
        }
        try {
            String uploaded = fileStorageService.store(coverFile, "covers");
            if (uploaded != null) book.setImageUrl(uploaded);
            bookService.create(book);
            ra.addFlashAttribute("success", "Book \"" + book.getTitle() + "\" added successfully.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/books";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("book", bookService.findById(id));
        model.addAttribute("formAction", "/books/" + id);
        model.addAttribute("pageTitle", "Edit Book");
        return "books/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("book") Book book,
                         BindingResult result,
                         @RequestParam(value = "coverFile", required = false) MultipartFile coverFile,
                         Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/books/" + id);
            model.addAttribute("pageTitle", "Edit Book");
            return "books/form";
        }
        try {
            String uploaded = fileStorageService.store(coverFile, "covers");
            if (uploaded != null) book.setImageUrl(uploaded);
            bookService.update(id, book);
            ra.addFlashAttribute("success", "Book updated successfully.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/books";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            bookService.delete(id);
            ra.addFlashAttribute("success", "Book deleted successfully.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/books";
    }

    /** Export the (filtered) catalogue as a PDF report. */
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String category,
                                            @ModelAttribute("appSettings") com.unimas.library.entity.Setting settings) {
        byte[] pdf = exportService.booksPdf(bookService.searchAll(keyword, category),
                settings.getLibraryName());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=books.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /** Export the (filtered) catalogue as CSV. */
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv(@RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String category) {
        byte[] csv = exportService.booksCsv(bookService.searchAll(keyword, category));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=books.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    /** Export the (filtered) catalogue as an Excel workbook. */
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel(@RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) String category) {
        byte[] xlsx = exportService.booksExcel(bookService.searchAll(keyword, category));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=books.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(xlsx);
    }
}
