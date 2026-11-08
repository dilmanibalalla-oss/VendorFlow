package dto;

import java.math.BigDecimal;

public record CreateInvoiceRequest(Long purchaseOrderId, BigDecimal amount) {
}
