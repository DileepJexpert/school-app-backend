package com.school.manage.controller;

import com.school.manage.model.Expense;
import com.school.manage.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','ACCOUNTANT')")
    public ResponseEntity<List<Expense>> getExpenses(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        log.debug("[ExpenseController] GET /api/expenses — from='{}', to='{}'", from, to);
        if (from != null && to != null) {
            return ResponseEntity.ok(expenseService.getExpensesByDateRange(from, to));
        }
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','ACCOUNTANT')")
    public ResponseEntity<Expense> addExpense(@RequestBody Expense expense) {
        log.info("[ExpenseController] POST /api/expenses — category='{}', amount='{}'",
                expense.getCategory(), expense.getAmount());
        Expense saved = expenseService.addExpense(expense);
        log.info("[ExpenseController] Expense saved: id='{}'", saved.getId());
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','ACCOUNTANT')")
    public ResponseEntity<Void> deleteExpense(@PathVariable String id) {
        log.info("[ExpenseController] DELETE /api/expenses/{}", id);
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}
