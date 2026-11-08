package service;

import domain.POStatus;
import domain.PurchaseOrder;
import domain.Vendor;
import domain.VendorStatus;
import dto.CreatePurchaseOrderRequest;
import dto.DecisionRequest;
import dto.PurchaseOrderResponse;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import repository.PurchaseOrderRepository;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository orders;
    private final VendorService vendors;
    private final CurrentUserService currentUser;

    public PurchaseOrderService(PurchaseOrderRepository orders, VendorService vendors, CurrentUserService currentUser) {
        this.orders = orders;
        this.vendors = vendors;
        this.currentUser = currentUser;
    }

    @Transactional
    public PurchaseOrderResponse create(CreatePurchaseOrderRequest request) {
        if (request.vendorId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vendor is required");
        }
        Vendor vendor = vendors.requireVendor(request.vendorId());
        if (vendor.getStatus() != VendorStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Purchase orders can only be created for approved vendors");
        }

        PurchaseOrder order = new PurchaseOrder();
        order.setVendor(vendor);
        order.setCreatedBy(currentUser.requireCurrentUser());
        order.setDescription(Checks.required(request.description(), "Description"));
        order.setAmount(Checks.positive(request.amount(), "Amount"));
        order.setStatus(POStatus.PENDING_MANAGER);
        PurchaseOrder saved = orders.save(order);
        saved.setPoNumber("PO-" + saved.getId());
        return PurchaseOrderResponse.from(orders.save(saved));
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> list() {
        return orders.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(PurchaseOrderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse get(Long id) {
        return PurchaseOrderResponse.from(requireOrder(id));
    }

    @Transactional
    public PurchaseOrderResponse managerDecision(Long id, DecisionRequest request) {
        Checks.requireReasonWhenRejecting(request.approve(), request.reason());
        PurchaseOrder order = requireOrder(id);
        if (order.getStatus() != POStatus.PENDING_MANAGER) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This purchase order is not waiting for the manager");
        }
        applyDecision(order, request, POStatus.PENDING_FINANCE);
        return PurchaseOrderResponse.from(orders.save(order));
    }

    @Transactional
    public PurchaseOrderResponse financeDecision(Long id, DecisionRequest request) {
        Checks.requireReasonWhenRejecting(request.approve(), request.reason());
        PurchaseOrder order = requireOrder(id);
        if (order.getStatus() != POStatus.PENDING_FINANCE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This purchase order is not waiting for finance");
        }
        applyDecision(order, request, POStatus.APPROVED);
        return PurchaseOrderResponse.from(orders.save(order));
    }

    PurchaseOrder requireOrder(Long id) {
        return orders.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase order not found"));
    }

    private void applyDecision(PurchaseOrder order, DecisionRequest request, POStatus approvedStatus) {
        if (request.approve()) {
            order.setStatus(approvedStatus);
            order.setRejectionReason(null);
            return;
        }
        order.setStatus(POStatus.REJECTED);
        order.setRejectionReason(request.reason().trim());
    }
}
