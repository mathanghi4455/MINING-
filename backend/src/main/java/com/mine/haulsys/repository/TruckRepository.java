package com.mine.haulsys.repository;

import com.mine.haulsys.models.Truck;
import com.mine.haulsys.models.enums.TruckStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TruckRepository extends JpaRepository<Truck, Long> {
    List<Truck> findByStatus(TruckStatus status);
    List<Truck> findByCurrentSoCPercentLessThanAndStatus(Double threshold, TruckStatus status);
}
