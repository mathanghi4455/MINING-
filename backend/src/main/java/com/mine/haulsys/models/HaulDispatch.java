package com.mine.haulsys.models;

import com.mine.haulsys.models.enums.DispatchStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "haul_dispatches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HaulDispatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long truckId;

    private Long routeId;
    private String originPit;
    private String destinationDump;
    private Double tonnageMoved;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private DispatchStatus status;

    private Double distanceKm;
}
