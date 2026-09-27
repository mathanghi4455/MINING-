package com.mine.haulsys.dto;

import com.mine.haulsys.models.enums.ProductType;
import com.mine.haulsys.models.enums.UnitOfMeasure;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDto {
    private Long id;
    @NotBlank
    private String name;
    private ProductType type;
    private String sku;
    private UnitOfMeasure unitOfMeasure;
    @NotNull
    private BigDecimal unitPrice;
    private Long linkedAccountId;
}
