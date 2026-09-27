package com.mine.haulsys.controllers;

import com.mine.haulsys.models.AnalyticAccount;
import com.mine.haulsys.models.Budget;
import com.mine.haulsys.dto.AnalyticAccountDto;
import com.mine.haulsys.dto.BudgetDto;
import com.mine.haulsys.dto.BudgetVarianceDto;
import com.mine.haulsys.exception.ResourceNotFoundException;
import com.mine.haulsys.repository.AnalyticAccountRepository;
import com.mine.haulsys.repository.BudgetRepository;
import com.mine.haulsys.services.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BudgetController {

    private final AnalyticAccountRepository analyticAccountRepository;
    private final BudgetRepository budgetRepository;
    private final ReportService reportService;

    @PostMapping("/analytic-accounts")
    public ResponseEntity<AnalyticAccountDto> createAnalyticAccount(@Valid @RequestBody AnalyticAccountDto dto) {
        AnalyticAccount account = new AnalyticAccount();
        account.setName(dto.getName());
        account.setDescription(dto.getDescription());
        account = analyticAccountRepository.save(account);
        AnalyticAccountDto result = new AnalyticAccountDto();
        result.setId(account.getId());
        result.setName(account.getName());
        result.setDescription(account.getDescription());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/analytic-accounts")
    public ResponseEntity<List<AnalyticAccountDto>> findAllAnalyticAccounts() {
        List<AnalyticAccountDto> accounts = analyticAccountRepository.findAll().stream().map(a -> {
            AnalyticAccountDto mapped = new AnalyticAccountDto();
            mapped.setId(a.getId());
            mapped.setName(a.getName());
            mapped.setDescription(a.getDescription());
            return mapped;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(accounts);
    }

    @PostMapping("/budgets")
    public ResponseEntity<BudgetDto> createBudget(@Valid @RequestBody BudgetDto dto) {
        Budget budget = new Budget();
        budget.setAnalyticAccountId(dto.getAnalyticAccountId());
        budget.setAccountId(dto.getAccountId());
        budget.setPeriod(dto.getPeriod());
        budget.setPlannedAmount(dto.getPlannedAmount());
        budget = budgetRepository.save(budget);
        BudgetDto result = new BudgetDto();
        result.setId(budget.getId());
        result.setAnalyticAccountId(budget.getAnalyticAccountId());
        result.setAccountId(budget.getAccountId());
        result.setPeriod(budget.getPeriod());
        result.setPlannedAmount(budget.getPlannedAmount());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/budgets")
    public ResponseEntity<List<BudgetDto>> findAllBudgets() {
        List<BudgetDto> budgets = budgetRepository.findAll().stream().map(b -> {
            BudgetDto mapped = new BudgetDto();
            mapped.setId(b.getId());
            mapped.setAnalyticAccountId(b.getAnalyticAccountId());
            mapped.setAccountId(b.getAccountId());
            mapped.setPeriod(b.getPeriod());
            mapped.setPlannedAmount(b.getPlannedAmount());
            return mapped;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/budgets/{id}/variance")
    public ResponseEntity<List<BudgetVarianceDto>> getBudgetVariance(@PathVariable Long id) {
        Budget budget = budgetRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Budget", id));
        return ResponseEntity.ok(reportService.getBudgetReport(budget.getAnalyticAccountId(), budget.getPeriod()));
    }
}
