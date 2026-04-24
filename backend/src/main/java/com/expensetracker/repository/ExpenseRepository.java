package com.expensetracker.repository;

import com.expensetracker.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    /**
     * Returns all expenses sorted newest-first.
     */
    List<Expense> findAllByOrderByDateDescCreatedAtDesc();

    /**
     * Returns expenses filtered by category, sorted newest-first.
     * Case-insensitive match so 'food' and 'Food' both work.
     */
    @Query("SELECT e FROM Expense e WHERE LOWER(e.category) = LOWER(:category) ORDER BY e.date DESC, e.createdAt DESC")
    List<Expense> findByCategoryIgnoreCaseOrderByDateDesc(@Param("category") String category);

    /**
     * Check whether a category exists (used for validation messages).
     */
    boolean existsByCategory(String category);
}
