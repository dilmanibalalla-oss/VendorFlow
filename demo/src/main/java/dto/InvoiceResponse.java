package dto;

import domain.Invoice;
import domain.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvoiceResponse(
        Long id,
        String invoiceNumber,
        Long purchaseOrderId,
        String poNumber,
        BigDecimal amount,
        InvoiceStatus status,
        String rejectionReason,
        String submittedBy,
        LocalDateTime createdAt,
        LocalDateTime paidAt
) {
    public static InvoiceResponse from(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getPurchaseOrder().getId(),
                invoice.getPurchaseOrder().getPoNumber(),
                invoice.getAmount(),
                invoice.getStatus(),
                invoice.getRejectionReason(),
                invoice.getSubmittedBy().getUsername(),
                invoice.getCreatedAt(),
                invoice.getPaidAt());
    }
}
