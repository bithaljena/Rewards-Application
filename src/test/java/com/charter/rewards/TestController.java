package com.charter.rewards;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.charter.rewards.entity.Customer;
import com.charter.rewards.entity.Transaction;
import com.charter.rewards.repository.CustomerRepository;
import com.charter.rewards.repository.TransactionRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RewardsControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired CustomerRepository customerRepository;
    @Autowired TransactionRepository transactionRepository;
    @Autowired ObjectMapper objectMapper;

    private static Long customerId;

    @BeforeEach
    void seed() {
        transactionRepository.deleteAll();
        customerRepository.deleteAll();

        Customer c = customerRepository.save(
                Customer.builder()
                        .name("Test User")
                        .email("test@example.com")
                        .build());

        customerId = c.getId();

        LocalDateTime now = LocalDateTime.now();

        // Month -2 → 120 → 90 pts
        transactionRepository.save(Transaction.builder()
                .customer(c)
                .amount(120.0)
                .transactionDate(now.minusMonths(2))
                .description("Electronics")
                .build());

        // Month -1 → 200 → 250 pts
        transactionRepository.save(Transaction.builder()
                .customer(c)
                .amount(200.0)
                .transactionDate(now.minusMonths(1))
                .description("Clothing")
                .build());

        // Current → 75 → 25 pts
        transactionRepository.save(Transaction.builder()
                .customer(c)
                .amount(75.0)
                .transactionDate(now)
                .description("Groceries")
                .build());
    }

    @Test
    @Order(1)
    void getAllReports_returns200() throws Exception {
        mockMvc.perform(get("/api/rewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    @Order(2)
    void getAllReports_correctTotalPoints() throws Exception {
        mockMvc.perform(get("/api/rewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$[?(@.customerName=='Test User')].totalPoints",
                        contains(365)));
    }

    @Test
    @Order(3)
    void getCustomerReport_returns200() throws Exception {
        mockMvc.perform(get("/api/rewards/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(customerId));
    }

    @Test
    @Order(4)
    void getCustomerReport_notFound() throws Exception {
        mockMvc.perform(get("/api/rewards/{id}", 9999))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(5)
    void previewPoints_120_returns90() throws Exception {
        mockMvc.perform(get("/api/rewards/points").param("amount", "120"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.points").value(90));
    }
}