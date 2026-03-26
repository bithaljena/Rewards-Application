package com.charter.rewards.service;

import com.charter.rewards.DTO.CustomerRewardsReportDTO;
import com.charter.rewards.DTO.MonthlyPointsSummaryDTO;
import com.charter.rewards.DTO.TransactionPointsDTO;
import com.charter.rewards.entity.Customer;
import com.charter.rewards.entity.Transaction;
import com.charter.rewards.repository.CustomerRepository;
import com.charter.rewards.repository.TransactionRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RewardsService {
        @Autowired
        private final CustomerRepository customerRepository;
        @Autowired
        private final TransactionRepository transactionRepository;

        // ─── Points Calculation Core Logic ────────────────────────────────────────

        /**
         * Calculate reward points for a single transaction amount.
         *
         * Rules:
         *  - $0   – $49.99  → 0 points
         *  - $50  – $99.99  → 1 point per dollar spent in this range
         *  - $100+          → 1 point per dollar in $50–$100 range  (= 50 pts)
         *                   + 2 points per dollar over $100
         *
         * Example: $120 → (50 * 1) + (20 * 2) = 50 + 40 = 90 points
         */
        public int calculatePoints(double amount) {
            int points = 0;

            if (amount > 100) {
                // 2 pts for every dollar over $100
                points += (int) Math.floor((amount - 100) * 2);
                // 1 pt for every dollar between $50 and $100
                points += 50; // the $50–$100 band is always 50 pts when amount > 100
            } else if (amount > 50) {
                // 1 pt for every dollar over $50 (up to $100)
                points += (int) Math.floor(amount - 50);
            }
            // below $50 → 0 points

            return points;
        }

        /**
         * Build a TransactionPointsDTO with the detailed tier breakdown.
         */
        public TransactionPointsDTO buildTransactionPoints(Transaction t) {
            double amount = t.getAmount();

            int tier1 = 0; // $50–$100 band
            int tier2 = 0; // >$100 band

            if (amount > 100) {
                tier1 = 50;
                tier2 = (int) Math.floor((amount - 100) * 2);
            } else if (amount > 50) {
                tier1 = (int) Math.floor(amount - 50);
            }

            return TransactionPointsDTO.builder()
                    .transactionId(t.getId())
                    .date(t.getTransactionDate())
                    .amount(amount)
                    .pointsForTier1(tier1)
                    .pointsForTier2(tier2)
                    .totalPoints(tier1 + tier2)
                    .build();
        }

        // ─── Report Generation ────────────────────────────────────────────────────

        /**
         * Generate full rewards report for ALL customers over the last 3 months.
         */
        public List<CustomerRewardsReportDTO> getAllCustomersReport() {
            List<Customer> customers = customerRepository.findAll();
            return customers.stream()
                    .map(this::buildReportForCustomer)
                    .collect(Collectors.toList());
        }

        /**
         * Generate rewards report for a single customer over the last 3 months.
         */
        public CustomerRewardsReportDTO getCustomerReport(Long customerId) {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new NoSuchElementException("Customer not found: " + customerId));
            return buildReportForCustomer(customer);
        }

        // ─── Private Helpers ──────────────────────────────────────────────────────

        private CustomerRewardsReportDTO buildReportForCustomer(Customer customer) {
            // Determine the 3-month window (last 3 full calendar months from today)
            LocalDate today = LocalDate.now();
            LocalDate threeMonthsAgo = today.minusMonths(3).withDayOfMonth(1);

            List<Transaction> transactions =
                    transactionRepository.findByCustomerIdAndTransactionDateBetween(
                            customer.getId(), threeMonthsAgo, today);

            // Group transactions by "MONTH YEAR" key
            Map<String, List<Transaction>> byMonth = transactions.stream()
                    .collect(Collectors.groupingBy(t -> monthKey(LocalDate.from(t.getTransactionDate()))));

            // Build sorted monthly summaries (oldest → newest)
            List<MonthlyPointsSummaryDTO> monthly = byMonth.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> {
                        List<TransactionPointsDTO> txPoints = entry.getValue().stream()
                                .map(this::buildTransactionPoints)
                                .collect(Collectors.toList());

                        int monthTotal = txPoints.stream()
                                .mapToInt(TransactionPointsDTO::getTotalPoints)
                                .sum();

                        // Readable label: e.g. "JANUARY 2024"
                        String label = entry.getValue().get(0).getTransactionDate()
                                .getMonth()
                                .getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                                .toUpperCase()
                                + " "
                                + entry.getValue().get(0).getTransactionDate().getYear();

                        return MonthlyPointsSummaryDTO.builder()
                                .monthYear(label)
                                .monthlyPoints(monthTotal)
                                .transactions(txPoints)
                                .build();
                    })
                    .collect(Collectors.toList());

            int grandTotal = monthly.stream()
                    .mapToInt(MonthlyPointsSummaryDTO::getMonthlyPoints)
                    .sum();

            return CustomerRewardsReportDTO.builder()
                    .customerId(customer.getId())
                    .customerName(customer.getName())
                    .customerEmail(customer.getEmail())
                    .monthlyBreakdown(monthly)
                    .totalPoints(grandTotal)
                    .build();
        }

        /**
         * Sortable key: "YYYY-MM" so natural string sort = chronological sort.
         */
        private String monthKey(LocalDate date) {
            return date.getYear() + "-" + String.format("%02d", date.getMonthValue());
        }
    }



