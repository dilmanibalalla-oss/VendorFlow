package dto;

import domain.Vendor;
import domain.VendorStatus;
import java.time.LocalDateTime;

public record VendorResponse(
        Long id,
        String name,
        String email,
        String phone,
        String address,
        VendorStatus status,
        String createdBy,
        LocalDateTime createdAt
) {
    public static VendorResponse from(Vendor vendor) {
        return new VendorResponse(
                vendor.getId(),
                vendor.getName(),
                vendor.getEmail(),
                vendor.getPhone(),
                vendor.getAddress(),
                vendor.getStatus(),
                vendor.getCreatedBy().getUsername(),
                vendor.getCreatedAt());
    }
}
