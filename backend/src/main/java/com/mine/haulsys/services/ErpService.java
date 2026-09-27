package com.mine.haulsys.services;

import com.mine.haulsys.models.*;
import com.mine.haulsys.models.enums.*;
import com.mine.haulsys.dto.*;
import com.mine.haulsys.exception.ResourceNotFoundException;
import com.mine.haulsys.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ErpService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final VendorBillRepository vendorBillRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final CustomerInvoiceRepository customerInvoiceRepository;
    private final PaymentRepository paymentRepository;
    private final AccountingService accountingService;

    // ===========================
    // PURCHASE ORDERS
    // ===========================

    public PurchaseOrderDto createPurchaseOrder(PurchaseOrderDto dto) {
        BigDecimal total = dto.getUnitPrice().multiply(BigDecimal.valueOf(dto.getQty()));
        PurchaseOrder po = PurchaseOrder.builder()
            .vendorId(dto.getVendorId())
            .productId(dto.getProductId())
            .qty(dto.getQty())
            .unitPrice(dto.getUnitPrice())
            .totalAmount(total)
            .status(OrderStatus.DRAFT)
            .createdAt(LocalDateTime.now())
            .build();
        return toPODto(purchaseOrderRepository.save(po));
    }

    public PurchaseOrderDto confirmPurchaseOrder(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));
        po.setStatus(OrderStatus.CONFIRMED);
        return toPODto(purchaseOrderRepository.save(po));
    }

    public VendorBillDto convertPOToVendorBill(Long poId) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
            .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", poId));
        VendorBill bill = VendorBill.builder()
            .poId(po.getId())
            .vendorId(po.getVendorId())
            .amount(po.getTotalAmount())
            .dueDate(LocalDate.now().plusDays(30))
            .status(OrderStatus.OPEN)
            .createdAt(LocalDateTime.now())
            .build();
        VendorBill saved = vendorBillRepository.save(bill);
        accountingService.postVendorBillEntry(saved);
        po.setStatus(OrderStatus.BILLED);
        purchaseOrderRepository.save(po);
        return toBillDto(saved);
    }

    public PaymentDto payVendorBill(Long billId, PaymentDto dto) {
        VendorBill bill = vendorBillRepository.findById(billId)
            .orElseThrow(() -> new ResourceNotFoundException("VendorBill", billId));
        Payment payment = Payment.builder()
            .direction(PaymentDirection.OUT)
            .relatedBillId(bill.getId())
            .method(dto.getMethod() != null ? dto.getMethod() : PaymentMethod.BANK)
            .amount(dto.getAmount() != null ? dto.getAmount() : bill.getAmount())
            .paymentDate(LocalDate.now())
            .notes(dto.getNotes())
            .build();
        Payment saved = paymentRepository.save(payment);
        accountingService.postVendorPaymentEntry(saved, bill);
        bill.setStatus(OrderStatus.PAID);
        vendorBillRepository.save(bill);
        return toPaymentDto(saved);
    }

    // ===========================
    // SALES ORDERS
    // ===========================

    public SalesOrderDto createSalesOrder(SalesOrderDto dto) {
        BigDecimal total = dto.getUnitPrice().multiply(BigDecimal.valueOf(dto.getQty()));
        SalesOrder so = SalesOrder.builder()
            .customerId(dto.getCustomerId())
            .productId(dto.getProductId())
            .qty(dto.getQty())
            .unitPrice(dto.getUnitPrice())
            .totalAmount(total)
            .period(dto.getPeriod())
            .status(OrderStatus.DRAFT)
            .createdAt(LocalDateTime.now())
            .build();
        return toSODto(salesOrderRepository.save(so));
    }

    public SalesOrderDto confirmSalesOrder(Long id) {
        SalesOrder so = salesOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", id));
        so.setStatus(OrderStatus.CONFIRMED);
        return toSODto(salesOrderRepository.save(so));
    }

    public CustomerInvoiceDto convertSOToCustomerInvoice(Long soId) {
        SalesOrder so = salesOrderRepository.findById(soId)
            .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", soId));
        CustomerInvoice invoice = CustomerInvoice.builder()
            .soId(so.getId())
            .customerId(so.getCustomerId())
            .amount(so.getTotalAmount())
            .dueDate(LocalDate.now().plusDays(30))
            .status(OrderStatus.OPEN)
            .createdAt(LocalDateTime.now())
            .build();
        CustomerInvoice saved = customerInvoiceRepository.save(invoice);
        accountingService.postSalesEntry(saved);
        so.setStatus(OrderStatus.BILLED);
        salesOrderRepository.save(so);
        return toInvoiceDto(saved);
    }

    public PaymentDto collectInvoicePayment(Long invoiceId, PaymentDto dto) {
        CustomerInvoice invoice = customerInvoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new ResourceNotFoundException("CustomerInvoice", invoiceId));
        Payment payment = Payment.builder()
            .direction(PaymentDirection.IN)
            .relatedInvoiceId(invoice.getId())
            .method(dto.getMethod() != null ? dto.getMethod() : PaymentMethod.BANK)
            .amount(dto.getAmount() != null ? dto.getAmount() : invoice.getAmount())
            .paymentDate(LocalDate.now())
            .notes(dto.getNotes())
            .build();
        Payment saved = paymentRepository.save(payment);
        accountingService.postPaymentReceivedEntry(saved, invoice);
        invoice.setStatus(OrderStatus.PAID);
        customerInvoiceRepository.save(invoice);
        return toPaymentDto(saved);
    }

    // ===========================
    // FINDERS
    // ===========================

    public List<PurchaseOrderDto> findAllPurchaseOrders() {
        return purchaseOrderRepository.findAll().stream().map(this::toPODto).collect(Collectors.toList());
    }

    public PurchaseOrderDto findPOById(Long id) {
        return toPODto(purchaseOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id)));
    }

    public List<VendorBillDto> findAllVendorBills() {
        return vendorBillRepository.findAll().stream().map(this::toBillDto).collect(Collectors.toList());
    }

    public VendorBillDto findBillById(Long id) {
        return toBillDto(vendorBillRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("VendorBill", id)));
    }

    public List<SalesOrderDto> findAllSalesOrders() {
        return salesOrderRepository.findAll().stream().map(this::toSODto).collect(Collectors.toList());
    }

    public SalesOrderDto findSOById(Long id) {
        return toSODto(salesOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", id)));
    }

    public List<CustomerInvoiceDto> findAllCustomerInvoices() {
        return customerInvoiceRepository.findAll().stream().map(this::toInvoiceDto).collect(Collectors.toList());
    }

    public CustomerInvoiceDto findInvoiceById(Long id) {
        return toInvoiceDto(customerInvoiceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("CustomerInvoice", id)));
    }

    public List<PaymentDto> findAllPayments() {
        return paymentRepository.findAll().stream().map(this::toPaymentDto).collect(Collectors.toList());
    }

    // ===========================
    // MAPPERS
    // ===========================

    private PurchaseOrderDto toPODto(PurchaseOrder po) {
        PurchaseOrderDto d = new PurchaseOrderDto();
        d.setId(po.getId());
        d.setVendorId(po.getVendorId());
        d.setProductId(po.getProductId());
        d.setQty(po.getQty());
        d.setUnitPrice(po.getUnitPrice());
        d.setTotalAmount(po.getTotalAmount());
        d.setStatus(po.getStatus());
        d.setCreatedAt(po.getCreatedAt());
        return d;
    }

    private VendorBillDto toBillDto(VendorBill b) {
        VendorBillDto d = new VendorBillDto();
        d.setId(b.getId());
        d.setPoId(b.getPoId());
        d.setVendorId(b.getVendorId());
        d.setAmount(b.getAmount());
        d.setDueDate(b.getDueDate());
        d.setStatus(b.getStatus());
        d.setCreatedAt(b.getCreatedAt());
        return d;
    }

    private SalesOrderDto toSODto(SalesOrder so) {
        SalesOrderDto d = new SalesOrderDto();
        d.setId(so.getId());
        d.setCustomerId(so.getCustomerId());
        d.setProductId(so.getProductId());
        d.setQty(so.getQty());
        d.setUnitPrice(so.getUnitPrice());
        d.setTotalAmount(so.getTotalAmount());
        d.setPeriod(so.getPeriod());
        d.setStatus(so.getStatus());
        d.setCreatedAt(so.getCreatedAt());
        return d;
    }

    private CustomerInvoiceDto toInvoiceDto(CustomerInvoice inv) {
        CustomerInvoiceDto d = new CustomerInvoiceDto();
        d.setId(inv.getId());
        d.setSoId(inv.getSoId());
        d.setCustomerId(inv.getCustomerId());
        d.setTonnageBilled(inv.getTonnageBilled());
        d.setAmount(inv.getAmount());
        d.setDueDate(inv.getDueDate());
        d.setStatus(inv.getStatus());
        d.setCreatedAt(inv.getCreatedAt());
        return d;
    }

    private PaymentDto toPaymentDto(Payment p) {
        PaymentDto d = new PaymentDto();
        d.setId(p.getId());
        d.setDirection(p.getDirection());
        d.setRelatedInvoiceId(p.getRelatedInvoiceId());
        d.setRelatedBillId(p.getRelatedBillId());
        d.setMethod(p.getMethod());
        d.setAmount(p.getAmount());
        d.setPaymentDate(p.getPaymentDate());
        d.setNotes(p.getNotes());
        return d;
    }
}
