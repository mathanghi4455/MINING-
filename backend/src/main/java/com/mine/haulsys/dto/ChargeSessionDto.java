package com.mine.haulsys.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ChargeSessionDto {
    private Long id;
    private Long truckId;
    private Long stationId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double startSoC;
    private Double endSoC;
    private Double energyDeliveredKwh;
    private BigDecimal costAmount;
}
