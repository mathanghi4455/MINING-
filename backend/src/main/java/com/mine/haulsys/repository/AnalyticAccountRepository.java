package com.mine.haulsys.repository;

import com.mine.haulsys.models.AnalyticAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyticAccountRepository extends JpaRepository<AnalyticAccount, Long> {
}
