package com.mine.haulsys.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnalyticAccountDto {
    private Long id;
    @NotBlank
    private String name;
    private String description;
}
