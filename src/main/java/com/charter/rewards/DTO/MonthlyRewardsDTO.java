package com.charter.rewards.DTO;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyRewardsDTO {

    private String month;     // e.g. "JAN-2026"

    private int points;
}