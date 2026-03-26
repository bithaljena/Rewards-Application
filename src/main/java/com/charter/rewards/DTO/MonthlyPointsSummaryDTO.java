package com.charter.rewards.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyPointsSummaryDTO {

    private String monthYear;   // e.g. "JAN-2026"

    private int monthlyPoints;

    private List<TransactionPointsDTO> transactions;
}