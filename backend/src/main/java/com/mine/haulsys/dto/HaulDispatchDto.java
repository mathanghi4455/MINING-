package com.mine.haulsys.dto;

import com.mine.haulsys.models.enums.DispatchStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HaulDispatchDto {
    private Long id;
    @NotNull(message = "Truck ID is required")
    private Long truckId;
    private Long routeId;
    @NotBlank(message = "Origin pit is required")
    private String originPit;
    @NotBlank(message = "Destination is required")
    private String destinationDump;
    @NotNull(message = "Tonnage is required")
    private Double tonnageMoved;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private DispatchStatus status;
    private Double distanceKm;
}
