package com.mine.haulsys.controllers;

import com.mine.haulsys.models.CustomerInvoice;
import com.mine.haulsys.models.Payment;
import com.mine.haulsys.models.VendorBill;
import com.mine.haulsys.models.enums.OrderStatus;
import com.mine.haulsys.models.enums.PaymentDirection;
import com.mine.haulsys.models.enums.PaymentMethod;
import com.mine.haulsys.dto.*;
import com.mine.haulsys.exception.ResourceNotFoundException;
import com.mine.haulsys.repository.CustomerInvoiceRepository;
import com.mine.haulsys.repository.PaymentRepository;
import com.mine.haulsys.repository.VendorBillRepository;
import com.mine.haulsys.services.ErpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasAnyRole('ADMIN','INVOICING')")
@RequiredArgsConstructor
public class TransactionController {

    private final ErpService erpService;
    private final VendorBillRepository vendorBillRepository;
    private final CustomerInvoiceRepository customerInvoiceRepository;
    private final PaymentRepository paymentRepository;

    // ===== PURCHASE ORDERS =====
    @PostMapping("/purchase-orders")
    public ResponseEntity<PurchaseOrderDto> createPurchaseOrder(@Valid @RequestBody PurchaseOrderDto dto) {
        return ResponseEntity.ok(erpService.createPurchaseOrder(dto));
    }

    @GetMapping("/purchase-orders")
    public ResponseEntity<List<PurchaseOrderDto>> findAllPurchaseOrders() {
        return ResponseEntity.ok(erpService.findAllPurchaseOrders());
    }

    @GetMapping("/purchase-orders/{id}")
    public ResponseEntity<PurchaseOrderDto> findPOById(@PathVariable Long id) {
        return ResponseEntity.ok(erpService.findPOById(id));
    }

    @PostMapping("/purchase-orders/{id}/confirm")
    public ResponseEntity<PurchaseOrderDto> confirmPurchaseOrder(@PathVariable Long id) {
        return ResponseEntity.ok(erpService.confirmPurchaseOrder(id));
    }

    @PostMapping("/purchase-orders/{id}/convert-to-bill")
    public ResponseEntity<VendorBillDto> convertPOToVendorBill(@PathVariable Long id) {
        return ResponseEntity.ok(erpService.convertPOToVendorBill(id));
    }

    // ===== VENDOR BILLS =====
    @PostMapping("/vendor-bills")
    public ResponseEntity<VendorBillDto> createVendorBill(@Valid @RequestBody VendorBillDto dto) {
        VendorBill bill = new VendorBill();
        bill.setVendorId(dto.getVendorId());
        bill.setPoId(dto.getPoId());
        bill.setAmount(dto.getAmount());
        bill.setDueDate(dto.getDueDate() != null ? dto.getDueDate() : LocalDate.now().plusDays(30));
        bill.setStatus(OrderStatus.OPEN);
        bill.setCreatedAt(LocalDateTime.now());
        bill = vendorBillRepository.save(bill);
        VendorBillDto result = new VendorBillDto();
        result.setId(bill.getId());
        result.setVendorId(bill.getVendorId());
        result.setAmount(bill.getAmount());
        result.setDueDate(bill.getDueDate());
        result.setStatus(bill.getStatus());
        result.setCreatedAt(bill.getCreatedAt());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/vendor-bills")
    public ResponseEntity<List<VendorBillDto>> findAllVendorBills() {
        return ResponseEntity.ok(erpService.findAllVendorBills());
    }

    @GetMapping("/vendor-bills/{id}")
    public ResponseEntity<VendorBillDto> findBillById(@PathVariable Long id) {
        return ResponseEntity.ok(erpService.findBillById(id));
    }

    @PostMapping("/vendor-bills/{id}/pay")
    public ResponseEntity<PaymentDto> payVendorBill(@PathVariable Long id, @RequestBody PaymentDto dto) {
        return ResponseEntity.ok(erpService.payVendorBill(id, dto));
    }

    // ===== SALES ORDERS =====
    @PostMapping("/sales-orders")
    public ResponseEntity<SalesOrderDto> createSalesOrder(@Valid @RequestBody SalesOrderDto dto) {
        return ResponseEntity.ok(erpService.createSalesOrder(dto));
    }

    @GetMapping("/sales-orders")
    public ResponseEntity<List<SalesOrderDto>> findAllSalesOrders() {
        return ResponseEntity.ok(erpService.findAllSalesOrders());
    }

    @GetMapping("/sales-orders/{id}")
    public ResponseEntity<SalesOrderDto> findSOById(@PathVariable Long id) {
        return ResponseEntity.ok(erpService.findSOById(id));
    }

    @PostMapping("/sales-orders/{id}/confirm")
    public ResponseEntity<SalesOrderDto> confirmSalesOrder(@PathVariable Long id) {
        return ResponseEntity.ok(erpService.confirmSalesOrder(id));
    }

    @PostMapping("/sales-orders/{id}/convert-to-invoice")
    public ResponseEntity<CustomerInvoiceDto> convertSOToCustomerInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(erpService.convertSOToCustomerInvoice(id));
    }

    // ===== CUSTOMER INVOICES =====
    @PostMapping("/customer-invoices")
    public ResponseEntity<CustomerInvoiceDto> createCustomerInvoice(@Valid @RequestBody CustomerInvoiceDto dto) {
        CustomerInvoice invoice = new CustomerInvoice();
        invoice.setCustomerId(dto.getCustomerId());
        invoice.setSoId(dto.getSoId());
        invoice.setAmount(dto.getAmount());
        invoice.setTonnageBilled(dto.getTonnageBilled());
        invoice.setDueDate(dto.getDueDate() != null ? dto.getDueDate() : LocalDate.now().plusDays(30));
        invoice.setStatus(OrderStatus.OPEN);
        invoice.setCreatedAt(LocalDateTime.now());
        invoice = customerInvoiceRepository.save(invoice);
        CustomerInvoiceDto result = new CustomerInvoiceDto();
        result.setId(invoice.getId());
        result.setCustomerId(invoice.getCustomerId());
        result.setAmount(invoice.getAmount());
        result.setDueDate(invoice.getDueDate());
        result.setStatus(invoice.getStatus());
        result.setCreatedAt(invoice.getCreatedAt());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/customer-invoices")
    public ResponseEntity<List<CustomerInvoiceDto>> findAllCustomerInvoices() {
        return ResponseEntity.ok(erpService.findAllCustomerInvoices());
    }

    @GetMapping("/customer-invoices/{id}")
    public ResponseEntity<CustomerInvoiceDto> findInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(erpService.findInvoiceById(id));
    }

    @PostMapping("/customer-invoices/{id}/collect-payment")
    public ResponseEntity<PaymentDto> collectInvoicePayment(@PathVariable Long id, @RequestBody PaymentDto dto) {
        return ResponseEntity.ok(erpService.collectInvoicePayment(id, dto));
    }

    // ===== PAYMENTS =====
    @PostMapping("/payments")
    public ResponseEntity<PaymentDto> createPayment(@RequestBody PaymentDto dto) {
        Payment payment = new Payment();
        payment.setAmount(dto.getAmount());
        payment.setPaymentDate(dto.getPaymentDate() != null ? dto.getPaymentDate() : LocalDate.now());
        payment.setDirection(dto.getDirection() != null ? dto.getDirection() : PaymentDirection.OUT);
        payment.setMethod(dto.getMethod() != null ? dto.getMethod() : PaymentMethod.BANK);
        payment.setNotes(dto.getNotes());
        payment.setRelatedInvoiceId(dto.getRelatedInvoiceId());
        payment.setRelatedBillId(dto.getRelatedBillId());
        payment = paymentRepository.save(payment);
        PaymentDto result = new PaymentDto();
        result.setId(payment.getId());
        result.setAmount(payment.getAmount());
        result.setPaymentDate(payment.getPaymentDate());
        result.setDirection(payment.getDirection());
        result.setMethod(payment.getMethod());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/payments")
    public ResponseEntity<List<PaymentDto>> findAllPayments() {
        return ResponseEntity.ok(erpService.findAllPayments());
    }
}
