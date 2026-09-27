package com.mine.haulsys.controllers;

import com.mine.haulsys.dto.*;
import com.mine.haulsys.services.DispatchService;
import com.mine.haulsys.services.TruckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/haul")
@RequiredArgsConstructor
public class HaulTruckController {

    private final TruckService truckService;
    private final DispatchService dispatchService;

    // ===== TRUCKS =====
    @PostMapping("/trucks")
    @PreAuthorize("hasAnyRole('ADMIN','INVOICING')")
    public ResponseEntity<TruckDto> createTruck(@Valid @RequestBody TruckDto truckDto) {
        return ResponseEntity.ok(truckService.create(truckDto));
    }

    @GetMapping("/trucks")
    public ResponseEntity<List<TruckDto>> findAllTrucks() {
        return ResponseEntity.ok(truckService.findAll());
    }

    @GetMapping("/trucks/{id}")
    public ResponseEntity<TruckDto> findTruckById(@PathVariable Long id) {
        return ResponseEntity.ok(truckService.findById(id));
    }

    @PutMapping("/trucks/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TruckDto> updateTruck(@PathVariable Long id, @RequestBody TruckDto truckDto) {
        return ResponseEntity.ok(truckService.update(id, truckDto));
    }

    @DeleteMapping("/trucks/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTruck(@PathVariable Long id) {
        truckService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/trucks/{id}/update-soc")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TruckDto> updateSoC(@PathVariable Long id, @RequestBody UpdateSoCRequest request) {
        return ResponseEntity.ok(truckService.updateSoC(id, request.getSoCPercent()));
    }

    // ===== CHARGERS =====
    @GetMapping("/chargers")
    public ResponseEntity<List<ChargingStationDto>> findAllStations() {
        return ResponseEntity.ok(dispatchService.findAllStations());
    }

    @GetMapping("/chargers/{id}")
    public ResponseEntity<ChargingStationDto> findStationById(@PathVariable Long id) {
        return ResponseEntity.ok(dispatchService.findStationById(id));
    }

    @PostMapping("/chargers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ChargingStationDto> createStation(@Valid @RequestBody ChargingStationDto stationDto) {
        return ResponseEntity.ok(dispatchService.createStation(stationDto));
    }

    @PutMapping("/chargers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ChargingStationDto> updateStation(@PathVariable Long id, @RequestBody ChargingStationDto stationDto) {
        return ResponseEntity.ok(dispatchService.updateStation(id, stationDto));
    }

    // ===== ROUTES =====
    @GetMapping("/routes")
    public ResponseEntity<List<HaulRouteDto>> findAllRoutes() {
        return ResponseEntity.ok(dispatchService.findAllRoutes());
    }

    @PostMapping("/routes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HaulRouteDto> createRoute(@Valid @RequestBody HaulRouteDto routeDto) {
        return ResponseEntity.ok(dispatchService.createRoute(routeDto));
    }

    // ===== DISPATCHES =====
    @PostMapping("/dispatches")
    public ResponseEntity<HaulDispatchDto> createDispatch(@Valid @RequestBody HaulDispatchDto haulDispatchDto) {
        return ResponseEntity.ok(dispatchService.createDispatch(haulDispatchDto));
    }

    @GetMapping("/dispatches")
    public ResponseEntity<List<HaulDispatchDto>> findAllDispatches() {
        return ResponseEntity.ok(dispatchService.findAllDispatches());
    }

    @GetMapping("/dispatches/{id}")
    public ResponseEntity<HaulDispatchDto> findDispatchById(@PathVariable Long id) {
        return ResponseEntity.ok(dispatchService.findDispatchById(id));
    }

    @PostMapping("/dispatches/{id}/complete")
    public ResponseEntity<HaulDispatchDto> completeDispatch(@PathVariable Long id) {
        return ResponseEntity.ok(dispatchService.completeDispatch(id));
    }

    // ===== CHARGE SESSIONS =====
    @GetMapping("/charge-sessions")
    public ResponseEntity<List<ChargeSessionDto>> findAllChargeSessions() {
        return ResponseEntity.ok(dispatchService.findAllChargeSessions());
    }

    @PostMapping("/charge-sessions/{id}/complete")
    public ResponseEntity<ChargeSessionDto> completeChargeSession(
            @PathVariable Long id, @RequestBody CompleteChargeRequest req) {
        return ResponseEntity.ok(dispatchService.completeChargeSession(id, req.getEndSoC(), req.getEnergyDeliveredKwh()));
    }
}
