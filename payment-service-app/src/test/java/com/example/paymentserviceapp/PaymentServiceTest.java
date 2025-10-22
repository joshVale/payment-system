package com.example.paymentserviceapp;


import com.example.paymentserviceapp.dto.PaymentDto;
import com.example.paymentserviceapp.exception.EntityNotFoundException;
import com.example.paymentserviceapp.mapper.PaymentMapper;
import com.example.paymentserviceapp.persistence.entity.Payment;
import com.example.paymentserviceapp.persistence.entity.PaymentStatus;
import com.example.paymentserviceapp.persistency.PaymentFilter;
import com.example.paymentserviceapp.persistency.PaymentRepository;
import com.example.paymentserviceapp.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;


@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Unit Tests")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private UUID testGuid;
    private Payment testPayment;
    private PaymentDto testPaymentDto;

    @BeforeEach
    void setUp() {
        testGuid = UUID.randomUUID();

        testPayment = new Payment();
        testPayment.setGuid(testGuid);
        testPayment.setInquiryRefId(UUID.randomUUID());
        testPayment.setAmount(new BigDecimal("1000.00"));
        testPayment.setCurrency("USD");
        testPayment.setTransactionRefId(UUID.randomUUID());
        testPayment.setStatus(PaymentStatus.CREATED);
        testPayment.setNote("Test payment");
        testPayment.setCreatedAt(OffsetDateTime.now());
        testPayment.setUpdatedAt(OffsetDateTime.now());

        testPaymentDto = new PaymentDto(
                testGuid,
                testPayment.getInquiryRefId(),
                testPayment.getAmount(),
                testPayment.getCurrency(),
                testPayment.getTransactionRefId(),
                testPayment.getStatus(),
                testPayment.getNote(),
                testPayment.getCreatedAt(),
                testPayment.getUpdatedAt()
        );
    }

    // ========== a. Поиск по идентификатору ==========

    @Test
    @DisplayName("Должен найти платеж по идентификатору")
    void shouldFindPaymentById() {
        when(paymentRepository.findById(testGuid)).thenReturn(Optional.of(testPayment));
        when(paymentMapper.toPaymentDto(testPayment)).thenReturn(testPaymentDto);

        PaymentDto result = paymentService.getPaymentById(testGuid);

        assertNotNull(result);
        assertEquals(testGuid, result.guid());
        assertEquals(testPayment.getAmount(), result.amount());
        assertEquals(testPayment.getCurrency(), result.currency());

        verify(paymentRepository).findById(testGuid);
        verify(paymentMapper).toPaymentDto(testPayment);
    }

    @Test
    @DisplayName("Должен выбросить EntityNotFoundException если платеж не найден")
    void shouldThrowEntityNotFoundExceptionWhenPaymentNotFound() {
        when(paymentRepository.findById(testGuid)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> paymentService.getPaymentById(testGuid)
        );

        assertTrue(exception.getMessage().contains("не найден"));
        verify(paymentRepository).findById(testGuid);
        verify(paymentMapper, never()).toPaymentDto(any());
    }

    @Test
    @DisplayName("Должен получить все платежи")
    void shouldGetAllPayments() {
        List<Payment> payments = Arrays.asList(testPayment, createPayment(), createPayment());
        when(paymentRepository.findAll()).thenReturn(payments);
        when(paymentMapper.toPaymentDto(any(Payment.class))).thenReturn(testPaymentDto);

        List<PaymentDto> result = paymentService.getAllPayments();

        assertNotNull(result);
        assertEquals(3, result.size());
        verify(paymentRepository).findAll();
        verify(paymentMapper, times(3)).toPaymentDto(any(Payment.class));
    }

    // ========== b. Фильтрация по различным критериям ==========

    @ParameterizedTest(name = "Фильтрация по валюте: {0}")
    @MethodSource("currencyFilterProvider")
    @DisplayName("Должен фильтровать платежи по валюте")
    void shouldFilterPaymentsByCurrency(String currency, int expectedCount) {
        PaymentFilter filter = new PaymentFilter(currency, null, null, null, null, null);
        List<Payment> payments = createPaymentsWithCurrency(currency, expectedCount);
        Page<Payment> paymentPage = new PageImpl<>(payments);

        when(paymentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(paymentPage);
        when(paymentMapper.toPaymentDto(any(Payment.class))).thenReturn(testPaymentDto);

        Page<PaymentDto> result = paymentService.searchPaged(filter, PageRequest.of(0, 25));

        assertNotNull(result);
        assertEquals(expectedCount, result.getContent().size());
        verify(paymentRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    static Stream<Arguments> currencyFilterProvider() {
        return Stream.of(
                Arguments.of("USD", 3),
                Arguments.of("EUR", 2),
                Arguments.of("GBP", 1)
        );
    }

    @ParameterizedTest(name = "Фильтрация по минимальной сумме: {0}")
    @MethodSource("minAmountFilterProvider")
    @DisplayName("Должен фильтровать платежи по минимальной сумме")
    void shouldFilterPaymentsByMinAmount(BigDecimal minAmount, int expectedCount) {
        PaymentFilter filter = new PaymentFilter(null, minAmount, null, null, null, null);
        List<Payment> payments = createPaymentsWithAmount(expectedCount);
        Page<Payment> paymentPage = new PageImpl<>(payments);

        when(paymentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(paymentPage);
        when(paymentMapper.toPaymentDto(any(Payment.class))).thenReturn(testPaymentDto);

        Page<PaymentDto> result = paymentService.searchPaged(filter, PageRequest.of(0, 25));

        assertNotNull(result);
        assertEquals(expectedCount, result.getContent().size());
    }

    static Stream<Arguments> minAmountFilterProvider() {
        return Stream.of(
                Arguments.of(new BigDecimal("100.00"), 5),
                Arguments.of(new BigDecimal("500.00"), 3),
                Arguments.of(new BigDecimal("1000.00"), 2)
        );
    }

    @ParameterizedTest(name = "Фильтрация по максимальной сумме: {0}")
    @MethodSource("maxAmountFilterProvider")
    @DisplayName("Должен фильтровать платежи по максимальной сумме")
    void shouldFilterPaymentsByMaxAmount(BigDecimal maxAmount, int expectedCount) {
        PaymentFilter filter = new PaymentFilter(null, null, maxAmount, null, null, null);
        List<Payment> payments = createPaymentsWithAmount(expectedCount);
        Page<Payment> paymentPage = new PageImpl<>(payments);

        when(paymentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(paymentPage);
        when(paymentMapper.toPaymentDto(any(Payment.class))).thenReturn(testPaymentDto);

        Page<PaymentDto> result = paymentService.searchPaged(filter, PageRequest.of(0, 25));

        assertNotNull(result);
        assertEquals(expectedCount, result.getContent().size());
    }

    static Stream<Arguments> maxAmountFilterProvider() {
        return Stream.of(
                Arguments.of(new BigDecimal("500.00"), 2),
                Arguments.of(new BigDecimal("1000.00"), 4),
                Arguments.of(new BigDecimal("5000.00"), 5)
        );
    }

    @ParameterizedTest(name = "Фильтрация по диапазону сумм: {0} - {1}")
    @MethodSource("amountRangeFilterProvider")
    @DisplayName("Должен фильтровать платежи по диапазону сумм")
    void shouldFilterPaymentsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount, int expectedCount) {
        PaymentFilter filter = new PaymentFilter(null, minAmount, maxAmount, null, null, null);
        List<Payment> payments = createPaymentsWithAmount(expectedCount);
        Page<Payment> paymentPage = new PageImpl<>(payments);

        when(paymentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(paymentPage);
        when(paymentMapper.toPaymentDto(any(Payment.class))).thenReturn(testPaymentDto);

        Page<PaymentDto> result = paymentService.searchPaged(filter, PageRequest.of(0, 25));

        assertNotNull(result);
        assertEquals(expectedCount, result.getContent().size());
    }

    static Stream<Arguments> amountRangeFilterProvider() {
        return Stream.of(
                Arguments.of(new BigDecimal("100.00"), new BigDecimal("500.00"), 2),
                Arguments.of(new BigDecimal("500.00"), new BigDecimal("1500.00"), 3),
                Arguments.of(new BigDecimal("1000.00"), new BigDecimal("10000.00"), 4)
        );
    }

    @Test
    @DisplayName("Должен фильтровать платежи созданные после указанной даты")
    void shouldFilterPaymentsByCreatedAfter() {
        Instant createdAfter = Instant.now().minusSeconds(86400);
        PaymentFilter filter = new PaymentFilter(null, null, null, createdAfter, null, null);
        List<Payment> payments = createPaymentsWithAmount(3);
        Page<Payment> paymentPage = new PageImpl<>(payments);

        when(paymentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(paymentPage);
        when(paymentMapper.toPaymentDto(any(Payment.class))).thenReturn(testPaymentDto);

        Page<PaymentDto> result = paymentService.searchPaged(filter, PageRequest.of(0, 25));

        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        verify(paymentRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Должен фильтровать платежи созданные до указанной даты")
    void shouldFilterPaymentsByCreatedBefore() {
        Instant createdBefore = Instant.now();
        PaymentFilter filter = new PaymentFilter(null, null, null, null, createdBefore, null);
        List<Payment> payments = createPaymentsWithAmount(2);
        Page<Payment> paymentPage = new PageImpl<>(payments);

        when(paymentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(paymentPage);
        when(paymentMapper.toPaymentDto(any(Payment.class))).thenReturn(testPaymentDto);

        Page<PaymentDto> result = paymentService.searchPaged(filter, PageRequest.of(0, 25));

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
    }

    @ParameterizedTest(name = "Фильтрация по диапазону дат создания")
    @MethodSource("dateRangeFilterProvider")
    @DisplayName("Должен фильтровать платежи по диапазону дат создания")
    void shouldFilterPaymentsByDateRange(int daysAfter, int daysBefore, int expectedCount) {
        Instant createdAfter = Instant.now().minusSeconds(daysAfter * 86400L);
        Instant createdBefore = Instant.now().minusSeconds(daysBefore * 86400L);
        PaymentFilter filter = new PaymentFilter(null, null, null, createdAfter, createdBefore, null);
        List<Payment> payments = createPaymentsWithAmount(expectedCount);
        Page<Payment> paymentPage = new PageImpl<>(payments);

        when(paymentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(paymentPage);
        when(paymentMapper.toPaymentDto(any(Payment.class))).thenReturn(testPaymentDto);

        Page<PaymentDto> result = paymentService.searchPaged(filter, PageRequest.of(0, 25));

        assertNotNull(result);
        assertEquals(expectedCount, result.getContent().size());
    }

    static Stream<Arguments> dateRangeFilterProvider() {
        return Stream.of(
                Arguments.of(7, 1, 3),
                Arguments.of(30, 7, 5),
                Arguments.of(90, 30, 2)
        );
    }

    @ParameterizedTest(name = "Фильтрация по статусу: {0}")
    @EnumSource(PaymentStatus.class)
    @DisplayName("Должен фильтровать платежи по статусу")
    void shouldFilterPaymentsByStatus(PaymentStatus status) {
        PaymentFilter filter = new PaymentFilter(null, null, null, null, null, status);
        List<Payment> payments = createPaymentsWithStatus(status, 3);
        Page<Payment> paymentPage = new PageImpl<>(payments);

        when(paymentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(paymentPage);
        when(paymentMapper.toPaymentDto(any(Payment.class))).thenReturn(testPaymentDto);

        Page<PaymentDto> result = paymentService.searchPaged(filter, PageRequest.of(0, 25));

        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        verify(paymentRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    // ========== c. Сортировка ==========

    @ParameterizedTest(name = "Сортировка: {0} {1}")
    @MethodSource("sortingProvider")
    @DisplayName("Должен сортировать платежи по различным критериям")
    void shouldSortPaymentsByDifferentCriteria(String sortField, Sort.Direction direction, int expectedCount) {
        PaymentFilter filter = new PaymentFilter(null, null, null, null, null, null);
        Sort sort = Sort.by(direction, sortField);
        Pageable pageable = PageRequest.of(0, 25, sort);

        List<Payment> payments = createSortedPayments(expectedCount);
        Page<Payment> paymentPage = new PageImpl<>(payments, pageable, payments.size());

        when(paymentRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(paymentPage);
        when(paymentMapper.toPaymentDto(any(Payment.class))).thenReturn(testPaymentDto);

        Page<PaymentDto> result = paymentService.searchPaged(filter, pageable);

        assertNotNull(result);
        assertEquals(expectedCount, result.getContent().size());
        verify(paymentRepository).findAll(any(Specification.class), eq(pageable));
    }

    static Stream<Arguments> sortingProvider() {
        return Stream.of(
                Arguments.of("amount", Sort.Direction.ASC, 5),
                Arguments.of("amount", Sort.Direction.DESC, 5),
                Arguments.of("createdAt", Sort.Direction.ASC, 4),
                Arguments.of("createdAt", Sort.Direction.DESC, 4)
        );
    }

    @Test
    @DisplayName("Должен сортировать по сумме от меньшего к большему")
    void shouldSortByAmountAscending() {
        PaymentFilter filter = new PaymentFilter(null, null, null, null, null, null);
        Sort sort = Sort.by(Sort.Direction.ASC, "amount");
        Pageable pageable = PageRequest.of(0, 25, sort);

        List<Payment> payments = createSortedPayments(3);
        Page<Payment> paymentPage = new PageImpl<>(payments, pageable, payments.size());

        when(paymentRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(paymentPage);
        when(paymentMapper.toPaymentDto(any(Payment.class))).thenReturn(testPaymentDto);

        Page<PaymentDto> result = paymentService.searchPaged(filter, pageable);

        assertNotNull(result);
        assertEquals(3, result.getContent().size());
    }

    @Test
    @DisplayName("Должен сортировать по дате создания от большего к меньшему")
    void shouldSortByCreatedAtDescending() {
        PaymentFilter filter = new PaymentFilter(null, null, null, null, null, null);
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(0, 25, sort);

        List<Payment> payments = createSortedPayments(3);
        Page<Payment> paymentPage = new PageImpl<>(payments, pageable, payments.size());

        when(paymentRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(paymentPage);
        when(paymentMapper.toPaymentDto(any(Payment.class))).thenReturn(testPaymentDto);

        Page<PaymentDto> result = paymentService.searchPaged(filter, pageable);

        assertNotNull(result);
        assertEquals(3, result.getContent().size());
    }

    // ========== d. Пагинация ==========

    @Test
    @DisplayName("Должен использовать первую страницу по умолчанию")
    void shouldUseDefaultFirstPage() {
        PaymentFilter filter = new PaymentFilter(null, null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 25);

        List<Payment> payments = createPaymentsWithAmount(25);
        Page<Payment> paymentPage = new PageImpl<>(payments, pageable, 100);

        when(paymentRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(paymentPage);
        when(paymentMapper.toPaymentDto(any(Payment.class))).thenReturn(testPaymentDto);

        Page<PaymentDto> result = paymentService.searchPaged(filter, pageable);

        assertNotNull(result);
        assertEquals(0, result.getNumber());
        assertEquals(25, result.getContent().size());
        assertEquals(100, result.getTotalElements());
    }

    @Test
    @DisplayName("Должен использовать 25 элементов на странице по умолчанию")
    void shouldUseDefault25ElementsPerPage() {
        PaymentFilter filter = new PaymentFilter(null, null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 25);

        List<Payment> payments = createPaymentsWithAmount(25);
        Page<Payment> paymentPage = new PageImpl<>(payments, pageable, 100);

        when(paymentRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(paymentPage);
        when(paymentMapper.toPaymentDto(any(Payment.class))).thenReturn(testPaymentDto);

        Page<PaymentDto> result = paymentService.searchPaged(filter, pageable);

        assertNotNull(result);
        assertEquals(25, result.getSize());
        assertEquals(25, result.getContent().size());
    }

    @ParameterizedTest(name = "Пагинация: страница {0}, размер {1}")
    @MethodSource("paginationProvider")
    @DisplayName("Должен корректно обрабатывать различные параметры пагинации")
    void shouldHandleDifferentPaginationParameters(int page, int size, int expectedSize) {
        PaymentFilter filter = new PaymentFilter(null, null, null, null, null, null);
        Pageable pageable = PageRequest.of(page, size);

        List<Payment> payments = createPaymentsWithAmount(expectedSize);
        Page<Payment> paymentPage = new PageImpl<>(payments, pageable, 100);

        when(paymentRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(paymentPage);
        when(paymentMapper.toPaymentDto(any(Payment.class))).thenReturn(testPaymentDto);

        Page<PaymentDto> result = paymentService.searchPaged(filter, pageable);

        assertNotNull(result);
        assertEquals(page, result.getNumber());
        assertEquals(size, result.getSize());
        assertEquals(expectedSize, result.getContent().size());
    }

    static Stream<Arguments> paginationProvider() {
        return Stream.of(
                Arguments.of(0, 25, 25),
                Arguments.of(1, 25, 25),
                Arguments.of(0, 10, 10),
                Arguments.of(2, 50, 50),
                Arguments.of(0, 100, 100)
        );
    }

    @Test
    @DisplayName("Должен вернуть пустую страницу если нет данных")
    void shouldReturnEmptyPageWhenNoData() {
        PaymentFilter filter = new PaymentFilter(null, null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 25);
        Page<Payment> emptyPage = Page.empty(pageable);

        when(paymentRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(emptyPage);

        Page<PaymentDto> result = paymentService.searchPaged(filter, pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    // ========== Дополнительные тесты для CRUD операций ==========

    @Test
    @DisplayName("Должен создать новый платеж")
    void shouldCreatePayment() {
        when(paymentMapper.toPaymentEntity(testPaymentDto)).thenReturn(testPayment);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(paymentMapper.toPaymentDto(testPayment)).thenReturn(testPaymentDto);

        PaymentDto result = paymentService.createPayment(testPaymentDto);

        assertNotNull(result);
        assertEquals(testPaymentDto.guid(), result.guid());
        verify(paymentMapper).toPaymentEntity(testPaymentDto);
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentMapper).toPaymentDto(testPayment);
    }

    @Test
    @DisplayName("Должен обновить существующий платеж")
    void shouldUpdatePayment() {
        PaymentDto updateDto = new PaymentDto(
                testGuid,
                testPayment.getInquiryRefId(),
                new BigDecimal("2000.00"),
                "EUR",
                testPayment.getTransactionRefId(),
                PaymentStatus.PENDING,
                "Updated payment",
                testPayment.getCreatedAt(),
                OffsetDateTime.now()
        );

        when(paymentRepository.findByGuidForUpdate(testGuid)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(testPayment)).thenReturn(testPayment);
        when(paymentMapper.toPaymentDto(testPayment)).thenReturn(updateDto);

        PaymentDto result = paymentService.updatePayment(testGuid, updateDto);

        assertNotNull(result);
        verify(paymentRepository).findByGuidForUpdate(testGuid);
        verify(paymentRepository).save(testPayment);
    }

    @Test
    @DisplayName("Должен выбросить исключение при обновлении несуществующего платежа")
    void shouldThrowExceptionWhenUpdatingNonExistentPayment() {
        when(paymentRepository.findByGuidForUpdate(testGuid)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> paymentService.updatePayment(testGuid, testPaymentDto));

        verify(paymentRepository).findByGuidForUpdate(testGuid);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Должен удалить платеж")
    void shouldDeletePayment() {
        when(paymentRepository.existsById(testGuid)).thenReturn(true);
        doNothing().when(paymentRepository).deleteById(testGuid);

        paymentService.delete(testGuid);

        verify(paymentRepository).existsById(testGuid);
        verify(paymentRepository).deleteById(testGuid);
    }

    @Test
    @DisplayName("Должен выбросить исключение при удалении несуществующего платежа")
    void shouldThrowExceptionWhenDeletingNonExistentPayment() {
        when(paymentRepository.existsById(testGuid)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> paymentService.delete(testGuid));

        verify(paymentRepository).existsById(testGuid);
        verify(paymentRepository, never()).deleteById(any());
    }

    // ========== Вспомогательные методы ==========

    private Payment createPayment() {
        Payment payment = new Payment();
        payment.setGuid(UUID.randomUUID());
        payment.setInquiryRefId(UUID.randomUUID());
        payment.setAmount(new BigDecimal("500.00"));
        payment.setCurrency("USD");
        payment.setTransactionRefId(UUID.randomUUID());
        payment.setStatus(PaymentStatus.CREATED);
        payment.setNote("Test");
        payment.setCreatedAt(OffsetDateTime.now());
        payment.setUpdatedAt(OffsetDateTime.now());
        return payment;
    }

    private List<Payment> createPaymentsWithCurrency(String currency, int count) {
        List<Payment> payments = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            Payment payment = createPayment();
            payment.setCurrency(currency);
            payments.add(payment);
        }
        return payments;
    }

    private List<Payment> createPaymentsWithAmount(int count) {
        List<Payment> payments = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            Payment payment = createPayment();
            payment.setAmount(new BigDecimal((i + 1) * 100));
            payments.add(payment);
        }
        return payments;
    }

    private List<Payment> createPaymentsWithStatus(PaymentStatus status, int count) {
        List<Payment> payments = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            Payment payment = createPayment();
            payment.setStatus(status);
            payments.add(payment);
        }
        return payments;
    }

    private List<Payment> createSortedPayments(int count) {
        List<Payment> payments = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            Payment payment = createPayment();
            payment.setAmount(new BigDecimal((i + 1) * 100));
            payment.setCreatedAt(OffsetDateTime.now().minusDays(count - i));
            payments.add(payment);
        }
        return payments;
    }
}
