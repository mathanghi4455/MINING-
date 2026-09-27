package com.mine.haulsys.repository;

import com.mine.haulsys.models.HaulRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HaulRouteRepository extends JpaRepository<HaulRoute, Long> {
}
