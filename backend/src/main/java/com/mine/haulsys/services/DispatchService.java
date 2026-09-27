package com.mine.haulsys.services;

import com.mine.haulsys.models.*;
import com.mine.haulsys.models.enums.*;
import com.mine.haulsys.dto.*;
import com.mine.haulsys.exception.ResourceNotFoundException;
import com.mine.haulsys.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DispatchService {

    private final TruckRepository truckRepository;
    private final ChargingStationRepository stationRepository;
    private final HaulDispatchRepository dispatchRepository;
    private final ChargeSessionRepository chargeSessionRepository;
    private final HaulRouteRepository routeRepository;
    private final AccountingService accountingService;

    public HaulDispatchDto createDispatch(HaulDispatchDto dto) {
        Truck truck = truckRepository.findById(dto.getTruckId())
            .orElseThrow(() -> new ResourceNotFoundException("Truck", dto.getTruckId()));

        HaulDispatch dispatch = HaulDispatch.builder()
            .truckId(dto.getTruckId())
            .routeId(dto.getRouteId())
            .originPit(dto.getOriginPit())
            .destinationDump(dto.getDestinationDump())
            .tonnageMoved(dto.getTonnageMoved())
            .startTime(LocalDateTime.now())
            .status(DispatchStatus.IN_PROGRESS)
            .distanceKm(dto.getDistanceKm())
            .build();

        if (dto.getRouteId() != null) {
            routeRepository.findById(dto.getRouteId()).ifPresent(route -> {
                dispatch.setOriginPit(route.getOriginPit());
                dispatch.setDestinationDump(route.getDestinationDump());
                dispatch.setDistanceKm(route.getDistanceKm());
                truck.setAssignedRouteId(route.getId());
            });
        }

        truck.setStatus(TruckStatus.HAULING);
        truckRepository.save(truck);

        return toDto(dispatchRepository.save(dispatch));
    }

    public HaulDispatchDto completeDispatch(Long dispatchId) {
        HaulDispatch dispatch = dispatchRepository.findById(dispatchId)
            .orElseThrow(() -> new ResourceNotFoundException("HaulDispatch", dispatchId));
        dispatch.setStatus(DispatchStatus.COMPLETED);
        dispatch.setEndTime(LocalDateTime.now());

        Truck truck = truckRepository.findById(dispatch.getTruckId())
            .orElseThrow(() -> new ResourceNotFoundException("Truck", dispatch.getTruckId()));
        if (truck.getStatus() == TruckStatus.HAULING) {
            truck.setStatus(TruckStatus.IDLE);
            truck.setAssignedRouteId(null);
            truckRepository.save(truck);
        }
        return toDto(dispatchRepository.save(dispatch));
    }

    public void routeToNearestCharger(Truck truck) {
        List<ChargingStation> available = stationRepository.findByStatus(StationStatus.AVAILABLE);
        if (available.isEmpty()) {
            log.warn("No available charging stations for truck {}", truck.getTruckCode());
            return;
        }

        ChargingStation nearest = available.stream()
            .min(Comparator.comparingDouble(s -> euclideanDistance(
                truck.getCurrentLat(), truck.getCurrentLng(),
                s.getLatitude(), s.getLongitude()
            ))).orElseThrow();

        // Mark station occupied
        nearest.setStatus(StationStatus.OCCUPIED);
        stationRepository.save(nearest);

        // Create charge session
        ChargeSession session = ChargeSession.builder()
            .truckId(truck.getId())
            .stationId(nearest.getId())
            .startTime(LocalDateTime.now())
            .startSoC(truck.getCurrentSoCPercent())
            .build();
        chargeSessionRepository.save(session);

        // Update truck status
        truck.setStatus(TruckStatus.EN_ROUTE_TO_CHARGE);
        truckRepository.save(truck);

        log.info("AUTO-DISPATCH: Truck {} (SoC={}%) -> Station {} at {}", 
            truck.getTruckCode(), truck.getCurrentSoCPercent(), nearest.getStationCode(), LocalDateTime.now());
    }

    public ChargeSessionDto completeChargeSession(Long sessionId, Double endSoC, Double energyKwh) {
        ChargeSession session = chargeSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("ChargeSession", sessionId));

        session.setEndTime(LocalDateTime.now());
        session.setEndSoC(endSoC);
        session.setEnergyDeliveredKwh(energyKwh);

        // Calculate cost: rate = $0.18/kWh default
        BigDecimal cost = BigDecimal.valueOf(energyKwh != null ? energyKwh * 0.18 : 0);
        session.setCostAmount(cost);

        // Free the station
        stationRepository.findById(session.getStationId()).ifPresent(station -> {
            station.setStatus(StationStatus.AVAILABLE);
            stationRepository.save(station);
        });

        // Update truck to IDLE
        truckRepository.findById(session.getTruckId()).ifPresent(truck -> {
            truck.setCurrentSoCPercent(endSoC);
            truck.setStatus(TruckStatus.IDLE);
            truckRepository.save(truck);
        });

        ChargeSession saved = chargeSessionRepository.save(session);

        // Post accounting journal entry
        try {
            accountingService.postChargingCostEntry(saved);
        } catch (Exception e) {
            log.warn("Could not post charge journal entry: {}", e.getMessage());
        }

        return toSessionDto(saved);
    }

    @Transactional(readOnly = true)
    public List<HaulDispatchDto> findAllDispatches() {
        return dispatchRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HaulDispatchDto findDispatchById(Long id) {
        return toDto(dispatchRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("HaulDispatch", id)));
    }

    @Transactional(readOnly = true)
    public List<ChargeSessionDto> findAllChargeSessions() {
        return chargeSessionRepository.findAll().stream().map(this::toSessionDto).collect(Collectors.toList());
    }

    // Station CRUD
    public ChargingStationDto createStation(ChargingStationDto dto) {
        ChargingStation station = ChargingStation.builder()
            .stationCode(dto.getStationCode())
            .name(dto.getName())
            .maxPowerKw(dto.getMaxPowerKw())
            .latitude(dto.getLatitude())
            .longitude(dto.getLongitude())
            .status(dto.getStatus() != null ? dto.getStatus() : StationStatus.AVAILABLE)
            .build();
        return toStationDto(stationRepository.save(station));
    }

    @Transactional(readOnly = true)
    public List<ChargingStationDto> findAllStations() {
        return stationRepository.findAll().stream().map(this::toStationDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChargingStationDto findStationById(Long id) {
        return toStationDto(stationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ChargingStation", id)));
    }

    public ChargingStationDto updateStation(Long id, ChargingStationDto dto) {
        ChargingStation station = stationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ChargingStation", id));
        if (dto.getName() != null) station.setName(dto.getName());
        if (dto.getMaxPowerKw() != null) station.setMaxPowerKw(dto.getMaxPowerKw());
        if (dto.getStatus() != null) station.setStatus(dto.getStatus());
        return toStationDto(stationRepository.save(station));
    }

    // Routes
    public HaulRouteDto createRoute(HaulRouteDto dto) {
        HaulRoute route = HaulRoute.builder()
            .routeName(dto.getRouteName())
            .originPit(dto.getOriginPit())
            .destinationDump(dto.getDestinationDump())
            .distanceKm(dto.getDistanceKm())
            .analyticAccountId(dto.getAnalyticAccountId())
            .build();
        return toRouteDto(routeRepository.save(route));
    }

    @Transactional(readOnly = true)
    public List<HaulRouteDto> findAllRoutes() {
        return routeRepository.findAll().stream().map(this::toRouteDto).collect(Collectors.toList());
    }

    private double euclideanDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) return Double.MAX_VALUE;
        double dLat = lat2 - lat1;
        double dLng = lng2 - lng1;
        return Math.sqrt(dLat * dLat + dLng * dLng);
    }

    private HaulDispatchDto toDto(HaulDispatch d) {
        HaulDispatchDto dto = new HaulDispatchDto();
        dto.setId(d.getId());
        dto.setTruckId(d.getTruckId());
        dto.setRouteId(d.getRouteId());
        dto.setOriginPit(d.getOriginPit());
        dto.setDestinationDump(d.getDestinationDump());
        dto.setTonnageMoved(d.getTonnageMoved());
        dto.setStartTime(d.getStartTime());
        dto.setEndTime(d.getEndTime());
        dto.setStatus(d.getStatus());
        dto.setDistanceKm(d.getDistanceKm());
        return dto;
    }

    public ChargeSessionDto toSessionDto(ChargeSession s) {
        ChargeSessionDto dto = new ChargeSessionDto();
        dto.setId(s.getId());
        dto.setTruckId(s.getTruckId());
        dto.setStationId(s.getStationId());
        dto.setStartTime(s.getStartTime());
        dto.setEndTime(s.getEndTime());
        dto.setStartSoC(s.getStartSoC());
        dto.setEndSoC(s.getEndSoC());
        dto.setEnergyDeliveredKwh(s.getEnergyDeliveredKwh());
        dto.setCostAmount(s.getCostAmount());
        return dto;
    }

    private ChargingStationDto toStationDto(ChargingStation s) {
        ChargingStationDto dto = new ChargingStationDto();
        dto.setId(s.getId());
        dto.setStationCode(s.getStationCode());
        dto.setName(s.getName());
        dto.setMaxPowerKw(s.getMaxPowerKw());
        dto.setLatitude(s.getLatitude());
        dto.setLongitude(s.getLongitude());
        dto.setStatus(s.getStatus());
        return dto;
    }

    private HaulRouteDto toRouteDto(HaulRoute r) {
        HaulRouteDto dto = new HaulRouteDto();
        dto.setId(r.getId());
        dto.setRouteName(r.getRouteName());
        dto.setOriginPit(r.getOriginPit());
        dto.setDestinationDump(r.getDestinationDump());
        dto.setDistanceKm(r.getDistanceKm());
        dto.setAnalyticAccountId(r.getAnalyticAccountId());
        return dto;
    }
}
