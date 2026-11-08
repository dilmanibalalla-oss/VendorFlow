package dto;

import java.math.BigDecimal;

public record CreatePurchaseOrderRequest(Long vendorId, String description, BigDecimal amount) {
}
