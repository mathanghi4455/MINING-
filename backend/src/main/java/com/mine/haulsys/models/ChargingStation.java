package com.mine.haulsys.models;

import com.mine.haulsys.models.enums.StationStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "charging_stations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargingStation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String stationCode;

    private String name;

    private Double maxPowerKw;
    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    private StationStatus status;
}
