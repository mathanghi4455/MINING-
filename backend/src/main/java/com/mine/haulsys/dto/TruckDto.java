package com.mine.haulsys.dto;

import com.mine.haulsys.models.enums.TruckStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TruckDto {
    private Long id;
    @NotBlank(message = "Truck code is required")
    private String truckCode;
    @NotBlank(message = "Model is required")
    private String model;
    @NotNull(message = "Battery capacity is required")
    private Double batteryCapacityKwh;
    private Double currentSoCPercent;
    private TruckStatus status;
    private Double currentLat;
    private Double currentLng;
    private Long assignedRouteId;
}
