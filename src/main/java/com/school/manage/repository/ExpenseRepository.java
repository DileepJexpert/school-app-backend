package com.school.manage.repository;

import com.school.manage.model.Expense;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends MongoRepository<Expense, String> {

    /** All expenses, newest first. */
    List<Expense> findAllByOrderByDateDesc();

    /** Expenses within an inclusive date range, newest first. */
    List<Expense> findByDateBetweenOrderByDateDesc(LocalDate from, LocalDate to);
}
