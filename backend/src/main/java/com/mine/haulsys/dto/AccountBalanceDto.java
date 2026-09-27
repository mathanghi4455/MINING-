package com.mine.haulsys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountBalanceDto {
    private String accountCode;
    private String accountName;
    private BigDecimal balance;
}
