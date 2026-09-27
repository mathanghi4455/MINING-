package com.mine.haulsys.repository;

import com.mine.haulsys.models.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByAnalyticAccountIdAndPeriod(Long analyticAccountId, String period);
}
