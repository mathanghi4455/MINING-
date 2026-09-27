package com.mine.haulsys.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BudgetVarianceDto {
    private Long budgetId;
    private Long analyticAccountId;
    private String analyticAccountName;
    private Long accountId;
    private String accountCode;
    private String accountName;
    private String period;
    private BigDecimal plannedAmount;
    private BigDecimal actualAmount;
    private BigDecimal variance;
    private Double variancePercent;
}
