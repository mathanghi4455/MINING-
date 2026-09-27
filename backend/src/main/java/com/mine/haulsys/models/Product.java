package com.mine.haulsys.models;

import com.mine.haulsys.models.enums.ProductType;
import com.mine.haulsys.models.enums.UnitOfMeasure;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private ProductType type;

    @Column(unique = true)
    private String sku;

    @Enumerated(EnumType.STRING)
    private UnitOfMeasure unitOfMeasure;

    private BigDecimal unitPrice;

    private Long linkedAccountId;
}
