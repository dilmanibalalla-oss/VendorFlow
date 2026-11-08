package service;

import domain.Vendor;
import domain.VendorStatus;
import dto.CreateVendorRequest;
import dto.VendorResponse;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import repository.VendorRepository;

@Service
public class VendorService {

    private final VendorRepository vendors;
    private final CurrentUserService currentUser;

    public VendorService(VendorRepository vendors, CurrentUserService currentUser) {
        this.vendors = vendors;
        this.currentUser = currentUser;
    }

    @Transactional
    public VendorResponse create(CreateVendorRequest request) {
        Vendor vendor = new Vendor();
        vendor.setName(Checks.required(request.name(), "Name"));
        vendor.setEmail(Checks.optional(request.email()));
        vendor.setPhone(Checks.optional(request.phone()));
        vendor.setAddress(Checks.optional(request.address()));
        vendor.setStatus(VendorStatus.PENDING);
        vendor.setCreatedBy(currentUser.requireCurrentUser());
        return VendorResponse.from(vendors.save(vendor));
    }

    @Transactional(readOnly = true)
    public List<VendorResponse> list() {
        return vendors.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(VendorResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public VendorResponse get(Long id) {
        return VendorResponse.from(requireVendor(id));
    }

    @Transactional
    public VendorResponse verify(Long id) {
        Vendor vendor = requireVendor(id);
        if (vendor.getStatus() != VendorStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending vendors can be verified");
        }
        vendor.setStatus(VendorStatus.VERIFIED);
        return VendorResponse.from(vendors.save(vendor));
    }

    @Transactional
    public VendorResponse approve(Long id) {
        Vendor vendor = requireVendor(id);
        if (vendor.getStatus() != VendorStatus.VERIFIED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only verified vendors can be approved");
        }
        vendor.setStatus(VendorStatus.APPROVED);
        return VendorResponse.from(vendors.save(vendor));
    }

    Vendor requireVendor(Long id) {
        return vendors.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor not found"));
    }
}
