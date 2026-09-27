package com.mine.haulsys.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "haul_routes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HaulRoute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String routeName;

    private String originPit;
    private String destinationDump;
    private Double distanceKm;
    private Long analyticAccountId;
}
