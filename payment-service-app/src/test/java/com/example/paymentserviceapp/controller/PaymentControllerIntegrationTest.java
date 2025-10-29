package com.example.paymentserviceapp.controller;

import com.example.paymentserviceapp.AbstractPostgresIntegrationTest;
import com.example.paymentserviceapp.TestJwtFactory;
import com.example.paymentserviceapp.dto.PaymentDto;
import com.example.paymentserviceapp.dto.request.PaymentRequest;
import com.example.paymentserviceapp.persistency.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class PaymentControllerIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final UUID EXISTING_PAYMENT_GUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID EXISTING_PAYMENT_GUID_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID EXISTING_PAYMENT_GUID_3 = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID NONEXISTENT_GUID = UUID.fromString("99999999-9999-9999-9999-999999999999");

    @BeforeEach
    void setUp() {
        // Очищаем кэш репозитория перед каждым тестом
        paymentRepository.flush();
    }

    // ========== CREATE PAYMENT TESTS ==========

    @Test
    void shouldCreatePayment_WhenValidRequest_AsAdmin() throws Exception {
        PaymentRequest request = new PaymentRequest(
                UUID.randomUUID(),
                BigDecimal.valueOf(123.45),
                "EUR",
                "Test payment"
        );

        String json = objectMapper.writeValueAsString(request);

        String response = mockMvc.perform(post("/api/v1/payments")
                        .with(TestJwtFactory.jwtWithRole("test-admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.amount").value(123.45))
                .andExpect(jsonPath("$.note").value("Test payment"))
                .andExpect(jsonPath("$.guid").exists())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PaymentDto created = objectMapper.readValue(response, PaymentDto.class);
        assertThat(paymentRepository.findById(created.guid())).isPresent();
    }

    @Test
    void shouldReturn403_WhenCreatePayment_AsUser() throws Exception {
        PaymentRequest request = new PaymentRequest(
                UUID.randomUUID(),
                BigDecimal.valueOf(123.45),
                "EUR",
                "Test payment"
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/payments")
                        .with(TestJwtFactory.jwtWithRole("test-user", "user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn401_WhenCreatePayment_WithoutAuth() throws Exception {
        PaymentRequest request = new PaymentRequest(
                UUID.randomUUID(),
                BigDecimal.valueOf(123.45),
                "EUR",
                "Test payment"
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn400_WhenCreatePayment_WithInvalidData() throws Exception {
        PaymentRequest request = new PaymentRequest(
                null, // invalid inquiryRefId
                null, // invalid amount
                "", // invalid currency
                "Test payment"
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/payments")
                        .with(TestJwtFactory.jwtWithRole("test-admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // ========== GET ALL PAYMENTS TESTS ==========

    @Test
    void shouldGetAllPayments_AsUser() throws Exception {
        mockMvc.perform(get("/api/v1/payments")
                        .with(TestJwtFactory.jwtWithRole("test-user", "user"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(7)) // 6 test payments
                .andExpect(jsonPath("$[0].currency").exists())
                .andExpect(jsonPath("$[0].amount").exists())
                .andExpect(jsonPath("$[0].status").exists());
    }

    @Test
    void shouldGetAllPayments_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/payments")
                        .with(TestJwtFactory.jwtWithRole("test-admin", "admin"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(7));
    }

    @Test
    void shouldReturn401_WhenGetAllPayments_WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/payments")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ========== GET PAYMENT BY ID TESTS ==========

    @Test
    void shouldGetPaymentById_WhenExists_AsUser() throws Exception {
        mockMvc.perform(get("/api/v1/payments/" + EXISTING_PAYMENT_GUID)
                        .with(TestJwtFactory.jwtWithRole("test-user", "user"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guid").value(EXISTING_PAYMENT_GUID.toString()))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.amount").value(100.50))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.note").value("Test payment with CREATED status"));
    }

    @Test
    void shouldGetPaymentById_WhenExists_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/payments/" + EXISTING_PAYMENT_GUID_2)
                        .with(TestJwtFactory.jwtWithRole("test-admin", "admin"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guid").value(EXISTING_PAYMENT_GUID_2.toString()))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.amount").value(250.75))
                .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    @Test
    void shouldReturn404_WhenGetPaymentById_NotExists() throws Exception {
        mockMvc.perform(get("/api/v1/payments/" + NONEXISTENT_GUID)
                        .with(TestJwtFactory.jwtWithRole("test-user", "user"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn401_WhenGetPaymentById_WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/payments/" + EXISTING_PAYMENT_GUID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ========== UPDATE PAYMENT TESTS ==========

    @Test
    void shouldUpdatePayment_WhenExists_AsUser() throws Exception {
        PaymentRequest request = new PaymentRequest(
                UUID.randomUUID(),
                BigDecimal.valueOf(999.99),
                "GBP",
                "Updated payment"
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/v1/payments/" + EXISTING_PAYMENT_GUID)
                        .with(TestJwtFactory.jwtWithRole("test-user", "user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guid").value(EXISTING_PAYMENT_GUID.toString()))
                .andExpect(jsonPath("$.currency").value("GBP"))
                .andExpect(jsonPath("$.amount").value(999.99))
                .andExpect(jsonPath("$.note").value("Updated payment"));
    }

    @Test
    void shouldUpdatePayment_WhenExists_AsAdmin() throws Exception {
        PaymentRequest request = new PaymentRequest(
                UUID.randomUUID(),
                BigDecimal.valueOf(1500.00),
                "USD",
                "Admin updated payment"
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/v1/payments/" + EXISTING_PAYMENT_GUID_2)
                        .with(TestJwtFactory.jwtWithRole("test-admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guid").value(EXISTING_PAYMENT_GUID_2.toString()))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.amount").value(1500.00))
                .andExpect(jsonPath("$.note").value("Admin updated payment"));
    }

    @Test
    void shouldReturn404_WhenUpdatePayment_NotExists() throws Exception {
        PaymentRequest request = new PaymentRequest(
                UUID.randomUUID(),
                BigDecimal.valueOf(999.99),
                "GBP",
                "Updated payment"
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/v1/payments/" + NONEXISTENT_GUID)
                        .with(TestJwtFactory.jwtWithRole("test-user", "user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn401_WhenUpdatePayment_WithoutAuth() throws Exception {
        PaymentRequest request = new PaymentRequest(
                UUID.randomUUID(),
                BigDecimal.valueOf(999.99),
                "GBP",
                "Updated payment"
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/v1/payments/" + EXISTING_PAYMENT_GUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn400_WhenUpdatePayment_WithInvalidData() throws Exception {
        PaymentRequest request = new PaymentRequest(
                null, // invalid inquiryRefId
                null, // invalid amount
                "", // invalid currency
                "Updated payment"
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/v1/payments/" + EXISTING_PAYMENT_GUID)
                        .with(TestJwtFactory.jwtWithRole("test-user", "user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // ========== DELETE PAYMENT TESTS ==========

    @Test
    void shouldDeletePayment_WhenExists_AsAdmin() throws Exception {
        mockMvc.perform(delete("/api/v1/payments/" + EXISTING_PAYMENT_GUID_3)
                        .with(TestJwtFactory.jwtWithRole("test-admin", "admin")))
                .andExpect(status().isOk());

        // Verify payment was deleted
        assertThat(paymentRepository.findById(EXISTING_PAYMENT_GUID_3)).isEmpty();
    }

    @Test
    void shouldReturn204_WhenDeletePayment_NotExists_AsAdmin() throws Exception {
        mockMvc.perform(delete("/api/v1/payments/" + NONEXISTENT_GUID)
                        .with(TestJwtFactory.jwtWithRole("test-admin", "admin")))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn403_WhenDeletePayment_AsUser() throws Exception {
        mockMvc.perform(delete("/api/v1/payments/" + EXISTING_PAYMENT_GUID)
                        .with(TestJwtFactory.jwtWithRole("test-user", "user")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn401_WhenDeletePayment_WithoutAuth() throws Exception {
        mockMvc.perform(delete("/api/v1/payments/" + EXISTING_PAYMENT_GUID))
                .andExpect(status().isUnauthorized());
    }

    // ========== SEARCH PAYMENTS TESTS ==========

    @Test
    void shouldSearchPayments_WithDefaultPagination_AsUser() throws Exception {
        mockMvc.perform(get("/api/v1/payments/search")
                        .with(TestJwtFactory.jwtWithRole("test-user", "user"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(7))
                .andExpect(jsonPath("$.totalElements").value(7))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void shouldSearchPayments_WithCustomPagination_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/payments/search")
                        .param("page", "0")
                        .param("size", "3")
                        .param("sortBy", "amount")
                        .param("direction", "desc")
                        .with(TestJwtFactory.jwtWithRole("test-admin", "admin"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(7))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.size").value(3))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void shouldSearchPayments_WithCurrencyFilter_AsUser() throws Exception {
        mockMvc.perform(get("/api/v1/payments/search")
                        .param("currency", "USD")
                        .with(TestJwtFactory.jwtWithRole("test-user", "user"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(4)) // 4 USD payments
                .andExpect(jsonPath("$.totalElements").value(4));
    }

    @Test
    void shouldSearchPayments_WithStatusFilter_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/payments/search")
                        .param("status", "CREATED")
                        .with(TestJwtFactory.jwtWithRole("test-admin", "admin"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1)) // 1 CREATED payment
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].status").value("CREATED"));
    }

    @Test
    void shouldSearchPayments_WithMultipleFilters_AsUser() throws Exception {
        mockMvc.perform(get("/api/v1/payments/search")
                        .param("currency", "EUR")
                        .param("status", "RECEIVED")
                        .with(TestJwtFactory.jwtWithRole("test-user", "user"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1)) // 1 EUR RECEIVED payment
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].currency").value("EUR"))
                .andExpect(jsonPath("$.content[0].status").value("RECEIVED"));
    }

    @Test
    void shouldReturn401_WhenSearchPayments_WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/payments/search")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnEmptyResult_WhenSearchPayments_WithNonExistentFilter() throws Exception {
        mockMvc.perform(get("/api/v1/payments/search")
                        .param("currency", "NONEXISTENT")
                        .with(TestJwtFactory.jwtWithRole("test-user", "user"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}
