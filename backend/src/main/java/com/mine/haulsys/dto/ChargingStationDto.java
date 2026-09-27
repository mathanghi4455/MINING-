package com.mine.haulsys.dto;

import com.mine.haulsys.models.enums.StationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChargingStationDto {
    private Long id;
    @NotBlank
    private String stationCode;
    @NotBlank
    private String name;
    @NotNull
    private Double maxPowerKw;
    private Double latitude;
    private Double longitude;
    private StationStatus status;
}
