package com.mine.haulsys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BalanceSheetDto {
    private List<AccountBalanceDto> assets;
    private List<AccountBalanceDto> liabilities;
    private List<AccountBalanceDto> equity;
    private BigDecimal totalAssets;
    private BigDecimal totalLiabilities;
    private BigDecimal totalEquity;
}
