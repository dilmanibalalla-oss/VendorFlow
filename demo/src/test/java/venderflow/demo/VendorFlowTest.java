package venderflow.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class VendorFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void vendorIsApprovedThenOrderedInvoicedAndPaid() throws Exception {
        MvcResult createdVendor = mockMvc.perform(post("/api/vendors")
                        .with(httpBasic("procurement", "procurement123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Acme Supplies","email":"acme@example.com","phone":"0112345678","address":"Colombo"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();
        int vendorId = JsonPath.read(createdVendor.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(post("/api/vendors/{id}/verify", vendorId)
                        .with(httpBasic("procurement", "procurement123")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/vendors/{id}/approve", vendorId)
                        .with(httpBasic("manager", "manager123")))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/vendors/{id}/verify", vendorId)
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VERIFIED"));

        mockMvc.perform(post("/api/vendors/{id}/approve", vendorId)
                        .with(httpBasic("manager", "manager123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        MvcResult createdOrder = mockMvc.perform(post("/api/purchase-orders")
                        .with(httpBasic("procurement", "procurement123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vendorId":%d,"description":"Office chairs","amount":1500.00}
                                """.formatted(vendorId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_MANAGER"))
                .andReturn();
        int orderId = JsonPath.read(createdOrder.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(post("/api/purchase-orders/{id}/manager-decision", orderId)
                        .with(httpBasic("manager", "manager123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"approve":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_FINANCE"));

        mockMvc.perform(post("/api/purchase-orders/{id}/finance-decision", orderId)
                        .with(httpBasic("finance", "finance123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"approve":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        MvcResult createdInvoice = mockMvc.perform(post("/api/invoices")
                        .with(httpBasic("procurement", "procurement123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"purchaseOrderId":%d,"amount":1500.00}
                                """.formatted(orderId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andReturn();
        int invoiceId = JsonPath.read(createdInvoice.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(post("/api/invoices/{id}/decision", invoiceId)
                        .with(httpBasic("finance", "finance123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"approve":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(post("/api/invoices/{id}/pay", invoiceId)
                        .with(httpBasic("finance", "finance123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        mockMvc.perform(get("/api/me").with(httpBasic("finance", "finance123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("FINANCE"));
    }
}
