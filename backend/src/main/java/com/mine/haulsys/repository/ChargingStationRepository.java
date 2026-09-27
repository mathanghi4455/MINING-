package com.mine.haulsys.repository;

import com.mine.haulsys.models.ChargingStation;
import com.mine.haulsys.models.enums.StationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChargingStationRepository extends JpaRepository<ChargingStation, Long> {
    List<ChargingStation> findByStatus(StationStatus status);
}
