package com.charter.rewards.DTO;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionPointsDTO {
    private Long transactionId;
    private Long customerId;
    private Double amount;
    private Integer points;
    private LocalDateTime date;
    private int pointsForTier1;
    private int pointsForTier2;
    private int totalPoints;



}
