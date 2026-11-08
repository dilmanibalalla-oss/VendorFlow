package controller;

import dto.CreatePurchaseOrderRequest;
import dto.DecisionRequest;
import dto.PurchaseOrderResponse;
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
import service.PurchaseOrderService;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService orders;

    public PurchaseOrderController(PurchaseOrderService orders) {
        this.orders = orders;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('PROCRUEMENT', 'ADMIN')")
    public PurchaseOrderResponse create(@RequestBody CreatePurchaseOrderRequest request) {
        return orders.create(request);
    }

    @GetMapping
    public List<PurchaseOrderResponse> list() {
        return orders.list();
    }

    @GetMapping("/{id}")
    public PurchaseOrderResponse get(@PathVariable Long id) {
        return orders.get(id);
    }

    @PostMapping("/{id}/manager-decision")
    @PreAuthorize("hasRole('MANAGER')")
    public PurchaseOrderResponse managerDecision(@PathVariable Long id, @RequestBody DecisionRequest request) {
        return orders.managerDecision(id, request);
    }

    @PostMapping("/{id}/finance-decision")
    @PreAuthorize("hasRole('FINANCE')")
    public PurchaseOrderResponse financeDecision(@PathVariable Long id, @RequestBody DecisionRequest request) {
        return orders.financeDecision(id, request);
    }
}
