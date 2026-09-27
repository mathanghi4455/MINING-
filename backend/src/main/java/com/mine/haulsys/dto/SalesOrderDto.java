package com.mine.haulsys.dto;

import com.mine.haulsys.models.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SalesOrderDto {
    private Long id;
    @NotNull
    private Long customerId;
    @NotNull
    private Long productId;
    @NotNull
    private Double qty;
    @NotNull
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private String period;
    private OrderStatus status;
    private LocalDateTime createdAt;
}
