package com.mine.haulsys.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompleteChargeRequest {
    @NotNull
    private Double endSoC;
    private Double energyDeliveredKwh;
}
