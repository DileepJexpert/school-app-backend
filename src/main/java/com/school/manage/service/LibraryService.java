package com.school.manage.service;

import com.school.manage.model.Book;
import com.school.manage.model.BookIssue;
import com.school.manage.repository.BookIssueRepository;
import com.school.manage.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LibraryService {

    private final BookRepository bookRepository;
    private final BookIssueRepository bookIssueRepository;

    private static final int DEFAULT_DUE_DAYS = 14;
    private static final double FINE_PER_DAY = 2.0;
    private static final int MAX_BOOKS_PER_STUDENT = 3;

    // -----------------------------------------------------------------------
    //  Book catalog management
    // -----------------------------------------------------------------------

    /**
     * Adds a new book to the catalog.
     */
    public Book addBook(Book book) {
        book.setCreatedAt(LocalDateTime.now());
        book.setActive(true);
        if (book.getAvailableCopies() == 0 && book.getTotalCopies() > 0) {
            book.setAvailableCopies(book.getTotalCopies());
        }
        return bookRepository.save(book);
    }

    /**
     * Updates an existing book's details. Preserves createdAt.
     */
    public Book updateBook(String id, Book book) {
        Book existing = getBookById(id);
        book.setId(id);
        book.setCreatedAt(existing.getCreatedAt());
        book.setUpdatedAt(LocalDateTime.now());
        book.setActive(true);
        return bookRepository.save(book);
    }

    /**
     * Soft-deletes a book by setting active = false.
     */
    public void deleteBook(String id) {
        Book book = getBookById(id);
        book.setActive(false);
        bookRepository.save(book);
    }

    /**
     * Returns all active books ordered by title.
     */
    public List<Book> getAllBooks() {
        return bookRepository.findByActiveTrueOrderByTitleAsc();
    }

    /**
     * Returns a single book by ID.
     */
    public Book getBookById(String id) {
        return bookRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Book not found: " + id));
    }

    /**
     * Searches books by title (case-insensitive partial match).
     */
    public List<Book> searchBooks(String query) {
        return bookRepository.findByTitleContainingIgnoreCaseAndActiveTrue(query);
    }

    /**
     * Returns books filtered by category.
     */
    public List<Book> getBooksByCategory(String category) {
        return bookRepository.findByCategoryAndActiveTrue(category);
    }

    // -----------------------------------------------------------------------
    //  Book issue / return
    // -----------------------------------------------------------------------

    /**
     * Issues a book to a student.
     * Validates: available copies > 0, student has fewer than 3 active issues.
     */
    public BookIssue issueBook(BookIssue issue) {
        // Validate book exists and has available copies
        Book book = getBookById(issue.getBookId());
        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("No available copies for book: " + book.getTitle());
        }

        // Validate student doesn't exceed max active issues
        long activeIssues = bookIssueRepository.countByStudentIdAndStatus(issue.getStudentId(), "ISSUED");
        if (activeIssues >= MAX_BOOKS_PER_STUDENT) {
            throw new RuntimeException("Student already has " + MAX_BOOKS_PER_STUDENT +
                    " books issued. Please return a book before issuing a new one.");
        }

        // Set issue details
        issue.setBookTitle(book.getTitle());
        issue.setIssueDate(LocalDate.now());
        issue.setDueDate(LocalDate.now().plusDays(DEFAULT_DUE_DAYS));
        issue.setStatus("ISSUED");
        issue.setFineAmount(0);
        issue.setCreatedAt(LocalDateTime.now());

        // Decrement available copies
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        return bookIssueRepository.save(issue);
    }

    /**
     * Returns a book. Calculates fine if overdue (Rs 2 per day).
     */
    public BookIssue returnBook(String issueId) {
        BookIssue issue = bookIssueRepository.findById(issueId).orElseThrow(() ->
                new RuntimeException("Book issue not found: " + issueId));

        if ("RETURNED".equals(issue.getStatus())) {
            throw new RuntimeException("Book has already been returned.");
        }

        issue.setReturnDate(LocalDate.now());
        issue.setStatus("RETURNED");

        // Calculate fine if overdue
        if (LocalDate.now().isAfter(issue.getDueDate())) {
            long overdueDays = ChronoUnit.DAYS.between(issue.getDueDate(), LocalDate.now());
            issue.setFineAmount(overdueDays * FINE_PER_DAY);
        }

        // Increment available copies
        Book book = getBookById(issue.getBookId());
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        return bookIssueRepository.save(issue);
    }

    /**
     * Returns all issues for a given student.
     */
    public List<BookIssue> getStudentIssues(String studentId) {
        return bookIssueRepository.findByStudentId(studentId);
    }

    /**
     * Returns all overdue book issues (past due date and still ISSUED).
     */
    public List<BookIssue> getOverdueBooks() {
        return bookIssueRepository.findByDueDateBeforeAndStatus(LocalDate.now(), "ISSUED");
    }

    /**
     * Returns library dashboard statistics.
     */
    public Map<String, Object> getLibraryStats() {
        Map<String, Object> stats = new HashMap<>();

        List<Book> allBooks = bookRepository.findByActiveTrueOrderByTitleAsc();
        int totalBooks = allBooks.stream().mapToInt(Book::getTotalCopies).sum();

        List<BookIssue> issuedBooks = bookIssueRepository.findByStatus("ISSUED");
        List<BookIssue> overdueBooks = bookIssueRepository.findByDueDateBeforeAndStatus(LocalDate.now(), "ISSUED");

        // Count distinct students with active issues
        long totalStudentsWithBooks = issuedBooks.stream()
                .map(BookIssue::getStudentId)
                .distinct()
                .count();

        stats.put("totalBooks", totalBooks);
        stats.put("totalTitles", allBooks.size());
        stats.put("totalIssued", issuedBooks.size());
        stats.put("totalOverdue", overdueBooks.size());
        stats.put("totalStudentsWithBooks", totalStudentsWithBooks);

        return stats;
    }
}
