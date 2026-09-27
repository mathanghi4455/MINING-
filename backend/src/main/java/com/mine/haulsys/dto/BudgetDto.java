package com.mine.haulsys.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class BudgetDto {
    private Long id;
    @NotNull
    private Long analyticAccountId;
    @NotNull
    private Long accountId;
    @NotBlank
    private String period;
    @NotNull
    private BigDecimal plannedAmount;
}
