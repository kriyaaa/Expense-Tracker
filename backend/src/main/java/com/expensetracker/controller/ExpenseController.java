package com.expensetracker.controller;

import com.expensetracker.dto.ApiResponse;
import com.expensetracker.dto.CreateExpenseRequest;
import com.expensetracker.dto.ExpenseResponse;
import com.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
@Slf4j
public class ExpenseController {

    private final ExpenseService expenseService;

    /**
     * POST /expenses
     * Creates a new expense entry.
     *
     * Request body: { amount, category, description, date }
     * Returns 201 Created with the saved expense.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(
            @Valid @RequestBody CreateExpenseRequest request) {

        ExpenseResponse created = expenseService.createExpense(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Expense created successfully", created));
    }

    /**
     * GET /expenses
     * Returns a list of expenses, newest first.
     *
     * Optional query params:
     *   ?category=Food       → filter by category (case-insensitive)
     *   ?sort=date_desc      → default behaviour, included for API compatibility
     *
     * Returns 200 OK with the list (empty array if no results).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getExpenses(
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "date_desc") String sort) {

        List<ExpenseResponse> expenses = expenseService.getExpenses(category);
        return ResponseEntity.ok(ApiResponse.success(expenses));
    }

    /**
     * DELETE /expenses/{id}
     * Deletes an expense by id.
     * Returns 204 No Content on success, 404 if not found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}
