package dto;

import domain.POStatus;
import domain.PurchaseOrder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchaseOrderResponse(
        Long id,
        String poNumber,
        Long vendorId,
        String vendorName,
        String description,
        BigDecimal amount,
        POStatus status,
        String rejectionReason,
        String createdBy,
        LocalDateTime createdAt
) {
    public static PurchaseOrderResponse from(PurchaseOrder order) {
        return new PurchaseOrderResponse(
                order.getId(),
                order.getPoNumber(),
                order.getVendor().getId(),
                order.getVendor().getName(),
                order.getDescription(),
                order.getAmount(),
                order.getStatus(),
                order.getRejectionReason(),
                order.getCreatedBy().getUsername(),
                order.getCreatedAt());
    }
}
