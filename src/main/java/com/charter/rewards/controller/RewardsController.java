package com.charter.rewards.controller;

import com.charter.rewards.DTO.CustomerRewardsReportDTO;
import com.charter.rewards.service.RewardsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RewardsController {
    /**
     * REST endpoints for the rewards program.
     *
     * GET /api/rewards              → report for ALL customers
     * GET /api/rewards/{customerId} → report for ONE customer
     * GET /api/rewards/points?amount=120 → ad-hoc points preview
     */

        private final RewardsService rewardsService;

        /**
         * Returns the 3-month rewards report for every customer.
         */
        @GetMapping
        public ResponseEntity<List<CustomerRewardsReportDTO>> getAllReports() {
            return ResponseEntity.ok(rewardsService.getAllCustomersReport());
        }

        /**
         * Returns the 3-month rewards report for a single customer.
         */
        @GetMapping("/{customerId}")
        public ResponseEntity<Object> getCustomerReport(
                @PathVariable Long customerId) {
            try {
                return ResponseEntity.ok(rewardsService.getCustomerReport(customerId));
            } catch (NoSuchElementException e) {
                return ResponseEntity.notFound().build();
            }
        }

        /**
         * Quick calculator – how many points would an amount earn?
         * e.g. GET /api/rewards/points?amount=120  →  {"amount":120,"points":90}
         */
        @GetMapping("/points")
        public ResponseEntity<?> previewPoints(@RequestParam double amount) {
            int points = rewardsService.calculatePoints(amount);
            return ResponseEntity.ok(
                    java.util.Map.of("amount", amount, "points", points)
            );
        }
    }


