package com.mine.haulsys.services;

import com.mine.haulsys.models.Truck;
import com.mine.haulsys.models.enums.TruckStatus;
import com.mine.haulsys.dto.TruckDto;
import com.mine.haulsys.exception.ResourceNotFoundException;
import com.mine.haulsys.repository.TruckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TruckService {

    private final TruckRepository truckRepository;

    public TruckDto create(TruckDto dto) {
        Truck truck = Truck.builder()
            .truckCode(dto.getTruckCode())
            .model(dto.getModel())
            .batteryCapacityKwh(dto.getBatteryCapacityKwh())
            .currentSoCPercent(dto.getCurrentSoCPercent() != null ? dto.getCurrentSoCPercent() : 100.0)
            .status(dto.getStatus() != null ? dto.getStatus() : TruckStatus.IDLE)
            .currentLat(dto.getCurrentLat())
            .currentLng(dto.getCurrentLng())
            .assignedRouteId(dto.getAssignedRouteId())
            .build();
        return toDto(truckRepository.save(truck));
    }

    @Transactional(readOnly = true)
    public List<TruckDto> findAll() {
        return truckRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TruckDto findById(Long id) {
        return toDto(truckRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Truck", id)));
    }

    public TruckDto update(Long id, TruckDto dto) {
        Truck truck = truckRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Truck", id));
        if (dto.getTruckCode() != null) truck.setTruckCode(dto.getTruckCode());
        if (dto.getModel() != null) truck.setModel(dto.getModel());
        if (dto.getBatteryCapacityKwh() != null) truck.setBatteryCapacityKwh(dto.getBatteryCapacityKwh());
        if (dto.getCurrentSoCPercent() != null) truck.setCurrentSoCPercent(dto.getCurrentSoCPercent());
        if (dto.getStatus() != null) truck.setStatus(dto.getStatus());
        if (dto.getCurrentLat() != null) truck.setCurrentLat(dto.getCurrentLat());
        if (dto.getCurrentLng() != null) truck.setCurrentLng(dto.getCurrentLng());
        if (dto.getAssignedRouteId() != null) truck.setAssignedRouteId(dto.getAssignedRouteId());
        return toDto(truckRepository.save(truck));
    }

    public void delete(Long id) {
        if (!truckRepository.existsById(id)) {
            throw new ResourceNotFoundException("Truck", id);
        }
        truckRepository.deleteById(id);
    }

    public TruckDto updateSoC(Long id, Double newSoC) {
        Truck truck = truckRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Truck", id));
        truck.setCurrentSoCPercent(Math.max(0.0, Math.min(100.0, newSoC)));
        return toDto(truckRepository.save(truck));
    }

    public TruckDto toDto(Truck truck) {
        TruckDto dto = new TruckDto();
        dto.setId(truck.getId());
        dto.setTruckCode(truck.getTruckCode());
        dto.setModel(truck.getModel());
        dto.setBatteryCapacityKwh(truck.getBatteryCapacityKwh());
        dto.setCurrentSoCPercent(truck.getCurrentSoCPercent());
        dto.setStatus(truck.getStatus());
        dto.setCurrentLat(truck.getCurrentLat());
        dto.setCurrentLng(truck.getCurrentLng());
        dto.setAssignedRouteId(truck.getAssignedRouteId());
        return dto;
    }
}
