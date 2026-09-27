package com.mine.haulsys.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "charge_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargeSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
