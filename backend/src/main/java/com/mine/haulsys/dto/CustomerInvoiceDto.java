package com.mine.haulsys.dto;

import com.mine.haulsys.models.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CustomerInvoiceDto {
    private Long id;
    private Long soId;
    @NotNull
    private Long customerId;
    private Double tonnageBilled;
    @NotNull
    private BigDecimal amount;
    private LocalDate dueDate;
    private OrderStatus status;
    private LocalDateTime createdAt;
}
