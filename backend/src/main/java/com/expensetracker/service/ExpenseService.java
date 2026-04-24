package com.expensetracker.service;

import com.expensetracker.dto.CreateExpenseRequest;
import com.expensetracker.dto.ExpenseResponse;
import com.expensetracker.exception.ExpenseNotFoundException;
import com.expensetracker.model.Expense;
import com.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    /**
     * Creates a new expense and persists it.
     */
    @Transactional
    public ExpenseResponse createExpense(CreateExpenseRequest request) {
        log.info("Creating expense: category={}, amount={}", request.getCategory(), request.getAmount());

        Expense expense = Expense.builder()
                .amount(request.getAmount())
                .category(request.getCategory().trim())
                .description(request.getDescription().trim())
                .date(request.getDate())
                .build();

        Expense saved = expenseRepository.save(expense);
        log.info("Expense created with id={}", saved.getId());
        return toResponse(saved);
    }

    /**
     * Returns all expenses, optionally filtered by category.
     * Always sorted by date descending (newest first).
     */
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpenses(String category) {
        List<Expense> expenses;

        if (StringUtils.hasText(category)) {
            log.debug("Fetching expenses filtered by category={}", category);
            expenses = expenseRepository.findByCategoryIgnoreCaseOrderByDateDesc(category.trim());
        } else {
            log.debug("Fetching all expenses");
            expenses = expenseRepository.findAllByOrderByDateDescCreatedAtDesc();
        }

        return expenses.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Deletes an expense by id. Throws if not found.
     */
    @Transactional
    public void deleteExpense(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new ExpenseNotFoundException(id);
        }
        expenseRepository.deleteById(id);
        log.info("Expense deleted with id={}", id);
    }

    // ── Mapping ─────────────────────────────────────────────────────────────

    private ExpenseResponse toResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .amount(expense.getAmount())
                .category(expense.getCategory())
                .description(expense.getDescription())
                .date(expense.getDate())
                .createdAt(expense.getCreatedAt())
                .build();
    }
}
