package com.school.manage.service;

import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.Expense;
import com.school.manage.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    /** Returns all expense records ordered by date descending. */
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAllByOrderByDateDesc();
    }

    /** Returns expenses whose date falls within [from, to] (inclusive). */
    public List<Expense> getExpensesByDateRange(LocalDate from, LocalDate to) {
        return expenseRepository.findByDateBetweenOrderByDateDesc(from, to);
    }

    /** Persists a new expense record. */
    public Expense addExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    /** Deletes an expense by ID; throws if not found. */
    public void deleteExpense(String id) {
        if (!expenseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Expense not found: " + id);
        }
        expenseRepository.deleteById(id);
    }
}
