package com.mine.haulsys.controllers;

import com.mine.haulsys.dto.BalanceSheetDto;
import com.mine.haulsys.dto.BudgetVarianceDto;
import com.mine.haulsys.dto.ProfitLossDto;
import com.mine.haulsys.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/balance-sheet")
    public ResponseEntity<BalanceSheetDto> getBalanceSheet(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf) {
        if (asOf == null) asOf = LocalDate.now();
        return ResponseEntity.ok(reportService.getBalanceSheet(asOf));
    }

    @GetMapping("/profit-and-loss")
    public ResponseEntity<ProfitLossDto> getProfitAndLoss(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.getProfitAndLoss(from, to));
    }

    @GetMapping("/budget")
    public ResponseEntity<List<BudgetVarianceDto>> getBudgetReport(
            @RequestParam Long analyticAccountId,
            @RequestParam String period) {
        return ResponseEntity.ok(reportService.getBudgetReport(analyticAccountId, period));
    }
}
