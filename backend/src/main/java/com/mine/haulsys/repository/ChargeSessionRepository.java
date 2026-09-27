package com.mine.haulsys.repository;

import com.mine.haulsys.models.ChargeSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChargeSessionRepository extends JpaRepository<ChargeSession, Long> {
    List<ChargeSession> findByTruckId(Long truckId);
}
