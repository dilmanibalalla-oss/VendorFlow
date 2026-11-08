package controller;

import dto.CreateVendorRequest;
import dto.VendorResponse;
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
import service.VendorService;

@RestController
@RequestMapping("/api/vendors")
public class VendorController {

    private final VendorService vendors;

    public VendorController(VendorService vendors) {
        this.vendors = vendors;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('PROCRUEMENT', 'ADMIN')")
    public VendorResponse create(@RequestBody CreateVendorRequest request) {
        return vendors.create(request);
    }

    @GetMapping
    public List<VendorResponse> list() {
        return vendors.list();
    }

    @GetMapping("/{id}")
    public VendorResponse get(@PathVariable Long id) {
        return vendors.get(id);
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public VendorResponse verify(@PathVariable Long id) {
        return vendors.verify(id);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public VendorResponse approve(@PathVariable Long id) {
        return vendors.approve(id);
    }
}
