package com.school.manage.controller;

import com.school.manage.model.Expense;
import com.school.manage.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExpenseController {

    private final ExpenseService expenseService;

    /**
     * Retrieve all expenses, optionally filtered by date range.
     *
     * GET /api/expenses
     * GET /api/expenses?from=2024-06-01&to=2024-06-30
     */
    @GetMapping
    public ResponseEntity<List<Expense>> getExpenses(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from != null && to != null) {
            return ResponseEntity.ok(expenseService.getExpensesByDateRange(from, to));
        }
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }

    /**
     * Record a new expense.
     *
     * POST /api/expenses
     */
    @PostMapping
    public ResponseEntity<Expense> addExpense(@RequestBody Expense expense) {
        return new ResponseEntity<>(expenseService.addExpense(expense), HttpStatus.CREATED);
    }

    /**
     * Delete an expense by ID.
     *
     * DELETE /api/expenses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable String id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}
