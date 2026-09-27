package com.mine.haulsys.services;

import com.mine.haulsys.models.*;
import com.mine.haulsys.models.enums.AccountType;
import com.mine.haulsys.dto.*;
import com.mine.haulsys.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final JournalEntryRepository journalEntryRepository;
    private final JournalLineRepository journalLineRepository;
    private final AccountRepository accountRepository;
    private final BudgetRepository budgetRepository;

    // ===== BALANCE SHEET =====
    public BalanceSheetDto getBalanceSheet(LocalDate asOf) {
        // get all entries up to asOf
        List<JournalEntry> entries = journalEntryRepository.findByEntryDateLessThanEqual(asOf);
        Set<Long> entryIds = entries.stream().map(JournalEntry::getId).collect(Collectors.toSet());

        // load lines for those entries
        List<JournalLine> allLines = entryIds.isEmpty()
            ? Collections.emptyList()
            : journalLineRepository.findAll().stream()
                .filter(l -> entryIds.contains(l.getEntryId()))
                .collect(Collectors.toList());

        List<AccountBalanceDto> assets = new ArrayList<>();
        List<AccountBalanceDto> liabilities = new ArrayList<>();
        List<AccountBalanceDto> equities = new ArrayList<>();
        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        BigDecimal totalEquity = BigDecimal.ZERO;

        for (Account account : accountRepository.findAll()) {
            AccountType atype = account.getType();
            if (atype == AccountType.INCOME || atype == AccountType.EXPENSE) continue;

            BigDecimal balance = computeBalance(account, allLines);
            if (balance.compareTo(BigDecimal.ZERO) == 0) continue;

            AccountBalanceDto abd = new AccountBalanceDto();
            abd.setAccountCode(account.getCode());
            abd.setAccountName(account.getName());
            abd.setBalance(balance);

            if (atype == AccountType.ASSET) {
                assets.add(abd);
                totalAssets = totalAssets.add(balance);
            } else if (atype == AccountType.LIABILITY) {
                liabilities.add(abd);
                totalLiabilities = totalLiabilities.add(balance);
            } else {
                equities.add(abd);
                totalEquity = totalEquity.add(balance);
            }
        }

        BalanceSheetDto dto = new BalanceSheetDto();
        dto.setAssets(assets);
        dto.setLiabilities(liabilities);
        dto.setEquity(equities);
        dto.setTotalAssets(totalAssets);
        dto.setTotalLiabilities(totalLiabilities);
        dto.setTotalEquity(totalEquity);
        return dto;
    }

    // ===== PROFIT AND LOSS =====
    public ProfitLossDto getProfitAndLoss(LocalDate from, LocalDate to) {
        List<JournalEntry> entries = journalEntryRepository.findByEntryDateBetween(from, to);
        Set<Long> entryIds = entries.stream().map(JournalEntry::getId).collect(Collectors.toSet());

        List<JournalLine> allLines = entryIds.isEmpty()
            ? Collections.emptyList()
            : journalLineRepository.findAll().stream()
                .filter(l -> entryIds.contains(l.getEntryId()))
                .collect(Collectors.toList());

        List<AccountBalanceDto> income = new ArrayList<>();
        List<AccountBalanceDto> expenses = new ArrayList<>();
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (Account account : accountRepository.findAll()) {
            AccountType atype = account.getType();
            if (atype != AccountType.INCOME && atype != AccountType.EXPENSE) continue;

            BigDecimal balance = computeBalance(account, allLines);
            if (balance.compareTo(BigDecimal.ZERO) == 0) continue;

            AccountBalanceDto abd = new AccountBalanceDto();
            abd.setAccountCode(account.getCode());
            abd.setAccountName(account.getName());
            abd.setBalance(balance);

            if (atype == AccountType.INCOME) {
                income.add(abd);
                totalIncome = totalIncome.add(balance);
            } else {
                expenses.add(abd);
                totalExpenses = totalExpenses.add(balance);
            }
        }

        ProfitLossDto dto = new ProfitLossDto();
        dto.setIncome(income);
        dto.setExpenses(expenses);
        dto.setTotalIncome(totalIncome);
        dto.setTotalExpenses(totalExpenses);
        dto.setNetProfit(totalIncome.subtract(totalExpenses));
        return dto;
    }

    // ===== BUDGET VARIANCE =====
    public List<BudgetVarianceDto> getBudgetReport(Long analyticAccountId, String period) {
        List<Budget> budgets = budgetRepository.findByAnalyticAccountIdAndPeriod(analyticAccountId, period);
        List<BudgetVarianceDto> result = new ArrayList<>();

        for (Budget budget : budgets) {
            Account account = accountRepository.findById(budget.getAccountId()).orElse(null);
            if (account == null) continue;

            List<JournalLine> lines = journalLineRepository
                .findByAccountIdAndAnalyticAccountId(budget.getAccountId(), analyticAccountId);

            BigDecimal actual = computeBalance(account, lines);
            BigDecimal planned = budget.getPlannedAmount() != null ? budget.getPlannedAmount() : BigDecimal.ZERO;
            BigDecimal variance = planned.subtract(actual);
            double variancePct = planned.compareTo(BigDecimal.ZERO) != 0
                ? variance.divide(planned, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;

            BudgetVarianceDto dto = new BudgetVarianceDto();
            dto.setAccountCode(account.getCode());
            dto.setAccountName(account.getName());
            dto.setPlannedAmount(planned);
            dto.setActualAmount(actual);
            dto.setVariance(variance);
            dto.setVariancePercent(variancePct);
            result.add(dto);
        }
        return result;
    }

    // ===== HELPER =====
    private BigDecimal computeBalance(Account account, List<JournalLine> lines) {
        List<JournalLine> acctLines = lines.stream()
            .filter(l -> l.getAccountId().equals(account.getId()))
            .collect(Collectors.toList());

        BigDecimal debit = acctLines.stream()
            .map(l -> l.getDebit() != null ? l.getDebit() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal credit = acctLines.stream()
            .map(l -> l.getCredit() != null ? l.getCredit() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        AccountType atype = account.getType();
        if (atype == AccountType.ASSET || atype == AccountType.EXPENSE) {
            return debit.subtract(credit);
        } else {
            return credit.subtract(debit);
        }
    }
}
