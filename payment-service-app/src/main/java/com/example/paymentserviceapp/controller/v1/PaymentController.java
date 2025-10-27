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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

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
    public ResponseEntity<PaymentResponse> create(@RequestBody PaymentRequest request) {
        final PaymentDto dto = paymentApiMapper.toDto(request);
        final PaymentDto created = paymentService.createPayment(dto);
        final PaymentResponse response = paymentApiMapper.toResponse(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('user','admin')")
    public List<PaymentResponse> getPayments() {
        final List<PaymentDto> dtos = paymentService.getAllPayments();
        return paymentApiMapper.toResponseList(dtos);
    }

    @PutMapping("/{guid}")
    @PreAuthorize("hasAnyRole('user', 'admin')")
    public ResponseEntity<PaymentResponse> update(@PathVariable UUID guid, @RequestBody PaymentRequest request) {
        final PaymentDto dto = paymentApiMapper.toDto(request);
        final PaymentDto updated = paymentService.updatePayment(guid, dto);
        final PaymentResponse response = paymentApiMapper.toResponse(updated);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{guid}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Void> delete(@PathVariable UUID guid) {
        try {
            paymentService.delete(guid);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.noContent().build();
        }
    }


    @GetMapping("/{guid}")
    @PreAuthorize("hasAnyRole('user', 'admin')")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID guid) {
        final PaymentDto paymentDto = paymentService.getPaymentById(guid);
        return ResponseEntity.ok(paymentApiMapper.toResponse(paymentDto));
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
        final Sort sort = direction.equalsIgnoreCase(SORT_DIRECTION_DESC)
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();

        final Pageable pageable = PageRequest.of(page, size, sort);
        final PaymentFilter serviceFilter = paymentFilterMapper.toServiceFilter(filterRequest);
        return paymentService.searchPaged(serviceFilter, pageable)
                .map(paymentApiMapper::toResponse);
    }
}