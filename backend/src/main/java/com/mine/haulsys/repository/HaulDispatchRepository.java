package com.mine.haulsys.repository;

import com.mine.haulsys.models.HaulDispatch;
import com.mine.haulsys.models.enums.DispatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HaulDispatchRepository extends JpaRepository<HaulDispatch, Long> {
    List<HaulDispatch> findByTruckId(Long truckId);
    List<HaulDispatch> findByStatus(DispatchStatus status);
}
