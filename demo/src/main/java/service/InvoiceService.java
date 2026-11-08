package service;

import domain.Invoice;
import domain.InvoiceStatus;
import domain.POStatus;
import domain.PurchaseOrder;
import dto.CreateInvoiceRequest;
import dto.DecisionRequest;
import dto.InvoiceResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import repository.InvoiceRepository;

@Service
public class InvoiceService {

    private final InvoiceRepository invoices;
    private final PurchaseOrderService orders;
    private final CurrentUserService currentUser;

    public InvoiceService(InvoiceRepository invoices, PurchaseOrderService orders, CurrentUserService currentUser) {
        this.invoices = invoices;
        this.orders = orders;
        this.currentUser = currentUser;
    }

    @Transactional
    public InvoiceResponse create(CreateInvoiceRequest request) {
        if (request.purchaseOrderId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Purchase order is required");
        }
        PurchaseOrder order = orders.requireOrder(request.purchaseOrderId());
        if (order.getStatus() != POStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invoices can only be submitted for approved purchase orders");
        }
        if (request.amount() != null && request.amount().compareTo(order.getAmount()) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invoice amount cannot be greater than the purchase order");
        }

        Invoice invoice = new Invoice();
        invoice.setPurchaseOrder(order);
        invoice.setSubmittedBy(currentUser.requireCurrentUser());
        invoice.setAmount(Checks.positive(request.amount(), "Amount"));
        invoice.setStatus(InvoiceStatus.SUBMITTED);
        Invoice saved = invoices.save(invoice);
        saved.setInvoiceNumber("INV-" + saved.getId());
        return InvoiceResponse.from(invoices.save(saved));
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> list() {
        return invoices.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(InvoiceResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public InvoiceResponse get(Long id) {
        return InvoiceResponse.from(requireInvoice(id));
    }

    @Transactional
    public InvoiceResponse decision(Long id, DecisionRequest request) {
        Checks.requireReasonWhenRejecting(request.approve(), request.reason());
        Invoice invoice = requireInvoice(id);
        if (invoice.getStatus() != InvoiceStatus.SUBMITTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only submitted invoices can be approved or rejected");
        }
        if (request.approve()) {
            invoice.setStatus(InvoiceStatus.APPROVED);
            invoice.setRejectionReason(null);
        } else {
            invoice.setStatus(InvoiceStatus.REJECTED);
            invoice.setRejectionReason(request.reason().trim());
        }
        return InvoiceResponse.from(invoices.save(invoice));
    }

    @Transactional
    public InvoiceResponse pay(Long id) {
        Invoice invoice = requireInvoice(id);
        if (invoice.getStatus() != InvoiceStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only approved invoices can be paid");
        }
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());
        return InvoiceResponse.from(invoices.save(invoice));
    }

    private Invoice requireInvoice(Long id) {
        return invoices.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
    }
}
