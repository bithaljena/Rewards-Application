package com.charter.rewards.DTO;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRewardsReportDTO{

    private Long customerId;

    private String customerName;

    private String customerEmail;

    private List<MonthlyPointsSummaryDTO> monthlyBreakdown;

    private int totalPoints;
}
