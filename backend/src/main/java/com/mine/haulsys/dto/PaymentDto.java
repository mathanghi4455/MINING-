package com.mine.haulsys.dto;

import com.mine.haulsys.models.enums.PaymentDirection;
import com.mine.haulsys.models.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentDto {
    private Long id;
    @NotNull
    private PaymentDirection direction;
    private Long relatedInvoiceId;
    private Long relatedBillId;
    @NotNull
    private PaymentMethod method;
    @NotNull
    private BigDecimal amount;
    @NotNull
    private LocalDate paymentDate;
    private String notes;
}
