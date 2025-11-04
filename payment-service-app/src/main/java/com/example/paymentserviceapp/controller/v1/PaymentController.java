package com.example.paymentserviceapp.controller.v1;

import com.example.paymentserviceapp.dto.PaymentDto;
import com.example.paymentserviceapp.dto.request.PaymentFilterRequest;
import com.example.paymentserviceapp.dto.request.PaymentRequest;
import com.example.paymentserviceapp.dto.response.PaymentResponse;
import com.example.paymentserviceapp.exception.EntityNotFoundException;
import com.example.paymentserviceapp.mapper.PaymentApiMapper;
import com.example.paymentserviceapp.mapper.PaymentFilterMapper;
import com.example.paymentserviceapp.persistency.PaymentFilter;
import com.example.paymentserviceapp.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentService paymentService;
    private final PaymentApiMapper paymentApiMapper;
    private final PaymentFilterMapper paymentFilterMapper;

    private static final String DEFAULT_SORT_FIELD = "createdAt";
    private static final String SORT_DIRECTION_DESC = "desc";
    private static final String DEFAULT_SORT_DIRECTION = SORT_DIRECTION_DESC;
    private static final String DEFAULT_PAGE = "0";
    private static final String DEFAULT_PAGE_SIZE = "20";

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody PaymentRequest request) {
        log.info("Creating payment with inquiryRefId: {}", request.inquiryRefId());
        final PaymentDto dto = paymentApiMapper.toDto(request);
        final PaymentDto created = paymentService.createPayment(dto);
        final PaymentResponse response = paymentApiMapper.toResponse(created);
        log.debug("Payment created successfully. Operation: createPayment, Payment state: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('user','admin')")
    public List<PaymentResponse> getPayments() {
        log.info("Getting all payments. Operation: getAllPayments");
        final List<PaymentDto> dtos = paymentService.getAllPayments();
        final List<PaymentResponse> response = paymentApiMapper.toResponseList(dtos);
        log.debug("All payments retrieved successfully. Operation: getAllPayments, Payment count: {}, Payments state: {}", 
                response.size(), response);
        return response;
    }

    @PutMapping("/{guid}")
    @PreAuthorize("hasAnyRole('user', 'admin')")
    public ResponseEntity<PaymentResponse> update(@PathVariable UUID guid, @Valid @RequestBody PaymentRequest request) {
        log.info("Updating payment. Operation: updatePayment, Payment guid: {}", guid);
        final PaymentDto dto = paymentApiMapper.toDto(request);
        final PaymentDto updated = paymentService.updatePayment(guid, dto);
        final PaymentResponse response = paymentApiMapper.toResponse(updated);
        log.debug("Payment updated successfully. Operation: updatePayment, Payment guid: {}, Payment state: {}", 
                guid, response);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{guid}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Void> delete(@PathVariable UUID guid) {
        log.info("Deleting payment. Operation: deletePayment, Payment guid: {}", guid);
        try {
            paymentService.delete(guid);
            log.debug("Payment deleted successfully. Operation: deletePayment, Payment guid: {}", guid);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            log.debug("Payment not found for deletion. Operation: deletePayment, Payment guid: {}", guid);
            return ResponseEntity.noContent().build();
        }
    }


    @GetMapping("/{guid}")
    @PreAuthorize("hasAnyRole('user', 'admin')")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID guid) {
        log.info("Getting payment by id. Operation: getPaymentById, Payment guid: {}", guid);
        final PaymentDto paymentDto = paymentService.getPaymentById(guid);
        final PaymentResponse response = paymentApiMapper.toResponse(paymentDto);
        log.debug("Payment retrieved successfully. Operation: getPaymentById, Payment guid: {}, Payment state: {}", 
                guid, response);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('user', 'admin')")
    public Page<PaymentResponse> searchPayments(
        @ModelAttribute PaymentFilterRequest filterRequest,
        @RequestParam(defaultValue = DEFAULT_PAGE) int page,
        @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
        @RequestParam(defaultValue = DEFAULT_SORT_FIELD) String sortBy,
        @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) String direction
    ) {
        log.info("Searching payments. Operation: searchPayments, Page: {}, Size: {}, SortBy: {}, Direction: {}", 
                page, size, sortBy, direction);
        final Sort sort = direction.equalsIgnoreCase(SORT_DIRECTION_DESC)
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();

        final Pageable pageable = PageRequest.of(page, size, sort);
        final PaymentFilter serviceFilter = paymentFilterMapper.toServiceFilter(filterRequest);
        final Page<PaymentResponse> response = paymentService.searchPaged(serviceFilter, pageable)
                .map(paymentApiMapper::toResponse);
        log.debug("Payments search completed successfully. Operation: searchPayments, Total elements: {}, " +
                "Total pages: {}, Payments state: {}", response.getTotalElements(), response.getTotalPages(), 
                response.getContent());
        return response;
    }
}