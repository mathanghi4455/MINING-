package com.mine.haulsys.services;

import com.mine.haulsys.models.*;
import com.mine.haulsys.models.enums.AccountType;
import com.mine.haulsys.dto.JournalLineDto;
import com.mine.haulsys.exception.ResourceNotFoundException;
import com.mine.haulsys.exception.UnbalancedJournalException;
import com.mine.haulsys.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AccountingService {

    private final JournalRepository journalRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final JournalLineRepository journalLineRepository;
    private final AccountRepository accountRepository;

    /**
     * Post a balanced journal entry. Validates sum(debit) == sum(credit).
     */
    public JournalEntry postJournalEntry(Long journalId, LocalDate date, String reference,
                                          String sourceDocType, Long sourceDocId,
                                          List<JournalLineDto> lines) {
        BigDecimal totalDebit = lines.stream()
            .map(l -> l.getDebit() != null ? l.getDebit() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = lines.stream()
            .map(l -> l.getCredit() != null ? l.getCredit() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new UnbalancedJournalException(
                String.format("Journal entry is not balanced: debit=%s, credit=%s", totalDebit, totalCredit)
            );
        }

        JournalEntry entry = JournalEntry.builder()
            .journalId(journalId)
            .entryDate(date)
            .reference(reference)
            .sourceDocType(sourceDocType)
            .sourceDocId(sourceDocId)
            .build();
        entry = journalEntryRepository.save(entry);

        for (JournalLineDto lineDto : lines) {
            JournalLine line = JournalLine.builder()
                .entryId(entry.getId())
                .accountId(lineDto.getAccountId())
                .debit(lineDto.getDebit() != null ? lineDto.getDebit() : BigDecimal.ZERO)
                .credit(lineDto.getCredit() != null ? lineDto.getCredit() : BigDecimal.ZERO)
                .analyticAccountId(lineDto.getAnalyticAccountId())
                .description(lineDto.getDescription())
                .build();
            journalLineRepository.save(line);
        }

        log.info("Posted journal entry #{} | ref={} | debit/credit={}", entry.getId(), reference, totalDebit);
        return entry;
    }

    /**
     * Post charging cost: Debit "Fleet Charging Power Costs", Credit "Power Utility Creditors"
     */
    public void postChargingCostEntry(ChargeSession session) {
        if (session.getCostAmount() == null || session.getCostAmount().compareTo(BigDecimal.ZERO) == 0) return;

        Account expenseAccount = findAccountByCode("5000"); // Fleet Charging Power Costs
        Account creditorAccount = findAccountByCode("2100"); // Power Utility Creditors
        Long journalId = getJournalIdByType("PURCHASE");

        List<JournalLineDto> lines = new ArrayList<>();
        JournalLineDto debitLine = new JournalLineDto();
        debitLine.setAccountId(expenseAccount.getId());
        debitLine.setDebit(session.getCostAmount());
        debitLine.setCredit(BigDecimal.ZERO);
        debitLine.setDescription("Charging cost for session #" + session.getId());
        lines.add(debitLine);

        JournalLineDto creditLine = new JournalLineDto();
        creditLine.setAccountId(creditorAccount.getId());
        creditLine.setDebit(BigDecimal.ZERO);
        creditLine.setCredit(session.getCostAmount());
        creditLine.setDescription("Power Utility payable for session #" + session.getId());
        lines.add(creditLine);

        postJournalEntry(journalId, LocalDate.now(),
            "CHG-" + session.getId(), "ChargeSession", session.getId(), lines);
    }

    /**
     * Post sales invoice: Debit "Accounts Receivable", Credit "Haulage Transport Revenue"
     */
    public void postSalesEntry(CustomerInvoice invoice) {
        Account arAccount = findAccountByCode("1300"); // Accounts Receivable
        Account revenueAccount = findAccountByCode("4000"); // Haulage Transport Revenue
        Long journalId = getJournalIdByType("SALES");

        List<JournalLineDto> lines = new ArrayList<>();
        JournalLineDto debitLine = new JournalLineDto();
        debitLine.setAccountId(arAccount.getId());
        debitLine.setDebit(invoice.getAmount());
        debitLine.setCredit(BigDecimal.ZERO);
        debitLine.setDescription("Invoice #" + invoice.getId() + " - Customer " + invoice.getCustomerId());
        lines.add(debitLine);

        JournalLineDto creditLine = new JournalLineDto();
        creditLine.setAccountId(revenueAccount.getId());
        creditLine.setDebit(BigDecimal.ZERO);
        creditLine.setCredit(invoice.getAmount());
        creditLine.setDescription("Haulage revenue for invoice #" + invoice.getId());
        lines.add(creditLine);

        postJournalEntry(journalId, LocalDate.now(),
            "INV-" + invoice.getId(), "CustomerInvoice", invoice.getId(), lines);
    }

    /**
     * Post payment received: Debit "Cash/Bank", Credit "Accounts Receivable"
     */
    public void postPaymentReceivedEntry(Payment payment) {
        Account bankAccount = findAccountByCode("1200"); // Cash/Bank
        Account arAccount = findAccountByCode("1300");   // Accounts Receivable
        Long journalId = getJournalIdByType("BANK");

        List<JournalLineDto> lines = new ArrayList<>();
        JournalLineDto debitLine = new JournalLineDto();
        debitLine.setAccountId(bankAccount.getId());
        debitLine.setDebit(payment.getAmount());
        debitLine.setCredit(BigDecimal.ZERO);
        debitLine.setDescription("Payment received for invoice #" + payment.getRelatedInvoiceId());
        lines.add(debitLine);

        JournalLineDto creditLine = new JournalLineDto();
        creditLine.setAccountId(arAccount.getId());
        creditLine.setDebit(BigDecimal.ZERO);
        creditLine.setCredit(payment.getAmount());
        creditLine.setDescription("AR cleared for invoice #" + payment.getRelatedInvoiceId());
        lines.add(creditLine);

        postJournalEntry(journalId, payment.getPaymentDate(),
            "PAY-IN-" + payment.getId(), "Payment", payment.getId(), lines);
    }

    /** Overload: accepts CustomerInvoice for context (ignored, Payment already has relatedInvoiceId) */
    public void postPaymentReceivedEntry(Payment payment, CustomerInvoice invoice) {
        postPaymentReceivedEntry(payment);
    }

    /**
     * Post vendor bill: Debit "Heavy Tire & Motor Replacement Expenses", Credit "Vehicle Supplier Payables"
     */
    public void postVendorBillEntry(VendorBill bill) {
        Account expenseAccount = findAccountByCode("5100"); // Replacement Expenses
        Account payablesAccount = findAccountByCode("2000"); // Vehicle Supplier Payables
        Long journalId = getJournalIdByType("PURCHASE");

        List<JournalLineDto> lines = new ArrayList<>();
        JournalLineDto debitLine = new JournalLineDto();
        debitLine.setAccountId(expenseAccount.getId());
        debitLine.setDebit(bill.getAmount());
        debitLine.setCredit(BigDecimal.ZERO);
        debitLine.setDescription("Vendor bill #" + bill.getId());
        lines.add(debitLine);

        JournalLineDto creditLine = new JournalLineDto();
        creditLine.setAccountId(payablesAccount.getId());
        creditLine.setDebit(BigDecimal.ZERO);
        creditLine.setCredit(bill.getAmount());
        creditLine.setDescription("Vendor payable for bill #" + bill.getId());
        lines.add(creditLine);

        postJournalEntry(journalId, LocalDate.now(),
            "BILL-" + bill.getId(), "VendorBill", bill.getId(), lines);
    }

    /**
     * Post vendor payment: Debit "Vehicle Supplier Payables", Credit "Cash/Bank"
     */
    public void postVendorPaymentEntry(Payment payment) {
        Account payablesAccount = findAccountByCode("2000"); // Payables
        Account bankAccount = findAccountByCode("1200");     // Cash/Bank
        Long journalId = getJournalIdByType("BANK");

        List<JournalLineDto> lines = new ArrayList<>();
        JournalLineDto debitLine = new JournalLineDto();
        debitLine.setAccountId(payablesAccount.getId());
        debitLine.setDebit(payment.getAmount());
        debitLine.setCredit(BigDecimal.ZERO);
        debitLine.setDescription("Vendor payment for bill #" + payment.getRelatedBillId());
        lines.add(debitLine);

        JournalLineDto creditLine = new JournalLineDto();
        creditLine.setAccountId(bankAccount.getId());
        creditLine.setDebit(BigDecimal.ZERO);
        creditLine.setCredit(payment.getAmount());
        creditLine.setDescription("Cash/bank outflow for bill #" + payment.getRelatedBillId());
        lines.add(creditLine);

        postJournalEntry(journalId, payment.getPaymentDate(),
            "PAY-OUT-" + payment.getId(), "Payment", payment.getId(), lines);
    }

    /** Overload: accepts VendorBill for context (ignored, Payment already has relatedBillId) */
    public void postVendorPaymentEntry(Payment payment, VendorBill bill) {
        postVendorPaymentEntry(payment);
    }

    public Account findAccountByCode(String code) {
        return accountRepository.findByCode(code)
            .orElseThrow(() -> new ResourceNotFoundException("Account with code " + code + " not found"));
    }

    /**
     * Get account balance: for ASSET accounts debit-heavy = positive; for LIABILITY/EQUITY/INCOME credit-heavy = positive
     */
    public BigDecimal getAccountBalance(Account account) {
        List<JournalLine> lines = journalLineRepository.findByAccountId(account.getId());
        BigDecimal totalDebit = lines.stream().map(JournalLine::getDebit).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = lines.stream().map(JournalLine::getCredit).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (account.getType() == AccountType.ASSET || account.getType() == AccountType.EXPENSE) {
            return totalDebit.subtract(totalCredit);
        } else {
            return totalCredit.subtract(totalDebit);
        }
    }

    private Long getJournalIdByType(String type) {
        return journalRepository.findAll().stream()
            .filter(j -> j.getType().name().equals(type))
            .findFirst()
            .map(j -> j.getId())
            .orElse(1L);
    }
}
