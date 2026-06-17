package com.school.manage.controller;

import com.school.manage.model.Book;
import com.school.manage.model.BookIssue;
import com.school.manage.service.LibraryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LibraryController {

    private final LibraryService libraryService;

    // -----------------------------------------------------------------------
    //  Book catalog endpoints
    // -----------------------------------------------------------------------

    @PostMapping("/books")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        return ResponseEntity.ok(libraryService.addBook(book));
    }

    @GetMapping("/books")
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(libraryService.getAllBooks());
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<Book> getBook(@PathVariable String id) {
        return ResponseEntity.ok(libraryService.getBookById(id));
    }

    @PutMapping("/books/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Book> updateBook(@PathVariable String id, @RequestBody Book book) {
        return ResponseEntity.ok(libraryService.updateBook(id, book));
    }

    @DeleteMapping("/books/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Void> deleteBook(@PathVariable String id) {
        libraryService.deleteBook(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/books/search")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam String q) {
        return ResponseEntity.ok(libraryService.searchBooks(q));
    }

    @GetMapping("/books/category/{category}")
    public ResponseEntity<List<Book>> getBooksByCategory(@PathVariable String category) {
        return ResponseEntity.ok(libraryService.getBooksByCategory(category));
    }

    // -----------------------------------------------------------------------
    //  Book issue / return endpoints
    // -----------------------------------------------------------------------

    @PostMapping("/issues")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<BookIssue> issueBook(@RequestBody BookIssue issue) {
        return ResponseEntity.ok(libraryService.issueBook(issue));
    }

    @PutMapping("/issues/{id}/return")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<BookIssue> returnBook(@PathVariable String id) {
        return ResponseEntity.ok(libraryService.returnBook(id));
    }

    @GetMapping("/issues/student/{studentId}")
    public ResponseEntity<List<BookIssue>> getStudentIssues(@PathVariable String studentId) {
        return ResponseEntity.ok(libraryService.getStudentIssues(studentId));
    }

    @GetMapping("/issues/overdue")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<BookIssue>> getOverdueBooks() {
        return ResponseEntity.ok(libraryService.getOverdueBooks());
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Map<String, Object>> getLibraryStats() {
        return ResponseEntity.ok(libraryService.getLibraryStats());
    }
}
