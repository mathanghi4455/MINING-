package com.mine.haulsys.dto;

import com.mine.haulsys.models.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchaseOrderDto {
    private Long id;
    @NotNull
    private Long vendorId;
    @NotNull
    private Long productId;
    @NotNull
    private Double qty;
    @NotNull
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
}
