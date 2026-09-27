package com.mine.haulsys.dto;

import lombok.Data;

@Data
public class HaulRouteDto {
    private Long id;
    private String routeName;
    private String originPit;
    private String destinationDump;
    private Double distanceKm;
    private Long analyticAccountId;
}
