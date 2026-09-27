package com.mine.haulsys.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class JournalLineDto {
    private Long accountId;
    private BigDecimal debit;
    private BigDecimal credit;
    private Long analyticAccountId;
    private String description;
}
