package com.expensetracker;

import com.expensetracker.dto.CreateExpenseRequest;
import com.expensetracker.repository.ExpenseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExpenseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExpenseRepository expenseRepository;

    @BeforeEach
    void setUp() {
        expenseRepository.deleteAll();
    }

    // ── POST /expenses ───────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /expenses — creates expense and returns 201")
    void createExpense_validRequest_returns201() throws Exception {
        CreateExpenseRequest req = CreateExpenseRequest.builder()
                .amount(new BigDecimal("250.50"))
                .category("Food & Drink")
                .description("Lunch at Swiggy")
                .date(LocalDate.of(2024, 6, 15))
                .build();

        mockMvc.perform(post("/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.amount").value(250.50))
                .andExpect(jsonPath("$.data.category").value("Food & Drink"))
                .andExpect(jsonPath("$.data.description").value("Lunch at Swiggy"))
                .andExpect(jsonPath("$.data.createdAt").isNotEmpty());
    }

    @Test
    @DisplayName("POST /expenses — negative amount returns 400")
    void createExpense_negativeAmount_returns400() throws Exception {
        CreateExpenseRequest req = CreateExpenseRequest.builder()
                .amount(new BigDecimal("-10.00"))
                .category("Transport")
                .description("Bus fare")
                .date(LocalDate.now())
                .build();

        mockMvc.perform(post("/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.amount").isNotEmpty());
    }

    @Test
    @DisplayName("POST /expenses — missing fields returns 400 with per-field errors")
    void createExpense_missingFields_returns400() throws Exception {
        String emptyBody = "{}";

        mockMvc.perform(post("/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.amount").exists())
                .andExpect(jsonPath("$.data.category").exists())
                .andExpect(jsonPath("$.data.description").exists())
                .andExpect(jsonPath("$.data.date").exists());
    }

    // ── GET /expenses ────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /expenses — returns all expenses newest first")
    void getExpenses_returnsListSortedByDateDesc() throws Exception {
        createExpenseViaApi("100.00", "Transport", "Taxi", "2024-06-01");
        createExpenseViaApi("200.00", "Food & Drink", "Dinner", "2024-06-15");

        mockMvc.perform(get("/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].date").value("2024-06-15"))
                .andExpect(jsonPath("$.data[1].date").value("2024-06-01"));
    }

    @Test
    @DisplayName("GET /expenses?category=Transport — filters correctly")
    void getExpenses_withCategoryFilter_returnsFilteredList() throws Exception {
        createExpenseViaApi("100.00", "Transport", "Taxi", "2024-06-01");
        createExpenseViaApi("200.00", "Food & Drink", "Dinner", "2024-06-15");
        createExpenseViaApi("50.00", "Transport", "Bus fare", "2024-06-10");

        mockMvc.perform(get("/expenses").param("category", "Transport"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[*].category", everyItem(equalTo("Transport"))));
    }

    @Test
    @DisplayName("GET /expenses — empty list returns 200 with empty array")
    void getExpenses_noData_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    // ── DELETE /expenses/{id} ────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /expenses/{id} — deletes and returns 204")
    void deleteExpense_exists_returns204() throws Exception {
        Long id = createExpenseViaApi("75.00", "Shopping", "Book", "2024-06-01");

        mockMvc.perform(delete("/expenses/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/expenses"))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @DisplayName("DELETE /expenses/{id} — unknown id returns 404")
    void deleteExpense_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/expenses/{id}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Long createExpenseViaApi(String amount, String category,
                                     String description, String date) throws Exception {
        CreateExpenseRequest req = CreateExpenseRequest.builder()
                .amount(new BigDecimal(amount))
                .category(category)
                .description(description)
                .date(LocalDate.parse(date))
                .build();

        String response = mockMvc.perform(post("/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).path("data").path("id").asLong();
    }
}
