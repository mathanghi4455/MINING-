package com.mine.haulsys.models;

import com.mine.haulsys.models.enums.PaymentDirection;
import com.mine.haulsys.models.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PaymentDirection direction;

    private Long relatedInvoiceId;
    private Long relatedBillId;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    private BigDecimal amount;

    private LocalDate paymentDate;

    private String notes;
}
