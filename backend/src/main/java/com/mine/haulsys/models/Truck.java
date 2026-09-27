package com.mine.haulsys.models;

import com.mine.haulsys.models.enums.TruckStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "trucks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Truck {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String truckCode;

    private String model;

    private Double batteryCapacityKwh;

    private Double currentSoCPercent;

    @Enumerated(EnumType.STRING)
    private TruckStatus status;

    private Double currentLat;
    private Double currentLng;
    private Long assignedRouteId;
}
