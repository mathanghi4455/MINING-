package com.mine.haulsys.scheduler;

import com.mine.haulsys.models.Truck;
import com.mine.haulsys.models.enums.TruckStatus;
import com.mine.haulsys.repository.TruckRepository;
import com.mine.haulsys.services.DispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatteryMonitorJob {

    private final TruckRepository truckRepository;
    private final DispatchService dispatchService;
    private final Random random = new Random();

    /**
     * Every 60 seconds: check HAULING trucks with SoC < 20% and auto-dispatch to nearest charger.
     */
    @Scheduled(fixedRate = 60000)
    public void checkBatteries() {
        List<Truck> lowBatteryTrucks = truckRepository
            .findByCurrentSoCPercentLessThanAndStatus(20.0, TruckStatus.HAULING);
        
        if (!lowBatteryTrucks.isEmpty()) {
            log.warn("Battery monitor triggered: {} trucks with low SoC", lowBatteryTrucks.size());
        }
        
        for (Truck truck : lowBatteryTrucks) {
            log.warn("AUTO-DISPATCH TRIGGER: Truck={} | SoC={}% | Time={}",
                truck.getTruckCode(), String.format("%.1f", truck.getCurrentSoCPercent()), LocalDateTime.now());
            dispatchService.routeToNearestCharger(truck);
        }
    }

    /**
     * Every 30 seconds: simulate SoC drain for HAULING trucks and SoC gain for CHARGING trucks.
     * This makes the live dashboard feel alive during demo.
     */
    @Scheduled(fixedRate = 30000)
    public void simulateSoCChanges() {
        List<Truck> haulingTrucks = truckRepository.findByStatus(TruckStatus.HAULING);
        for (Truck truck : haulingTrucks) {
            double drain = 0.5 + random.nextDouble() * 1.5; // 0.5 to 2.0% drain
            double newSoC = Math.max(0.0, truck.getCurrentSoCPercent() - drain);
            truck.setCurrentSoCPercent(newSoC);
        }
        if (!haulingTrucks.isEmpty()) {
            truckRepository.saveAll(haulingTrucks);
        }

        List<Truck> chargingTrucks = truckRepository.findByStatus(TruckStatus.CHARGING);
        for (Truck truck : chargingTrucks) {
            double gain = 3.0 + random.nextDouble() * 2.0; // 3 to 5% gain
            double newSoC = Math.min(100.0, truck.getCurrentSoCPercent() + gain);
            truck.setCurrentSoCPercent(newSoC);
            if (newSoC >= 95.0) {
                truck.setStatus(TruckStatus.IDLE);
                log.info("Truck {} fully charged, status -> IDLE", truck.getTruckCode());
            }
        }
        if (!chargingTrucks.isEmpty()) {
            truckRepository.saveAll(chargingTrucks);
        }
    }
}
