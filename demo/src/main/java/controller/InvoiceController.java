package controller;

import dto.CreateInvoiceRequest;
import dto.DecisionRequest;
import dto.InvoiceResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import service.InvoiceService;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoices;

    public InvoiceController(InvoiceService invoices) {
        this.invoices = invoices;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('PROCRUEMENT', 'ADMIN')")
    public InvoiceResponse create(@RequestBody CreateInvoiceRequest request) {
        return invoices.create(request);
    }

    @GetMapping
    public List<InvoiceResponse> list() {
        return invoices.list();
    }

    @GetMapping("/{id}")
    public InvoiceResponse get(@PathVariable Long id) {
        return invoices.get(id);
    }

    @PostMapping("/{id}/decision")
    @PreAuthorize("hasRole('FINANCE')")
    public InvoiceResponse decision(@PathVariable Long id, @RequestBody DecisionRequest request) {
        return invoices.decision(id, request);
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasRole('FINANCE')")
    public InvoiceResponse pay(@PathVariable Long id) {
        return invoices.pay(id);
    }
}
