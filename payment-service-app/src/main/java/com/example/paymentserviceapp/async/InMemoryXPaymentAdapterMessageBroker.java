package com.example.paymentserviceapp.async;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class InMemoryXPaymentAdapterMessageBroker implements AsyncSender<XPaymentAdapterRequestMessage> {

    private final AsyncListener<XPaymentAdapterResponseMessage> resultListener;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    @Override
    public void send(XPaymentAdapterRequestMessage request) {
        log.info("Received payment request: messageId={}, paymentGuid={}, amount={}, currency={}",
                request.getMessageId(), request.getPaymentGuid(), request.getAmount(), request.getCurrency());

        UUID txId = UUID.randomUUID();
        log.debug("Generated transaction reference ID: {}", txId);

        // Schedule async processing after 30 seconds
        scheduler.schedule(() -> processPayment(request, txId), 30, TimeUnit.SECONDS);

        log.info("Payment request scheduled for processing in 30 seconds: paymentGuid={}, transactionRefId={}",
                request.getPaymentGuid(), txId);
    }

    private void processPayment(XPaymentAdapterRequestMessage request, UUID txId) {
        log.info("Processing payment: paymentGuid={}, transactionRefId={}, amount={}",
                request.getPaymentGuid(), txId, request.getAmount());

        // Determine status based on amount divisibility by 2
        XPaymentAdapterStatus status = isAmountDivisibleByTwo(request.getAmount())
                ? XPaymentAdapterStatus.SUCCEEDED
                : XPaymentAdapterStatus.CANCELED;

        log.info("Payment processing completed: paymentGuid={}, status={}, reason={}",
                request.getPaymentGuid(), status,
                status == XPaymentAdapterStatus.SUCCEEDED ? "Amount is divisible by 2" : "Amount is not divisible by 2");

        emit(request, txId, status);
    }

    private boolean isAmountDivisibleByTwo(BigDecimal amount) {
        return amount.remainder(BigDecimal.valueOf(2)).compareTo(BigDecimal.ZERO) == 0;
    }

    private void emit(XPaymentAdapterRequestMessage request, UUID txId, XPaymentAdapterStatus status) {
        log.debug("Emitting response message: paymentGuid={}, status={}, transactionRefId={}",
                request.getPaymentGuid(), status, txId);

        XPaymentAdapterResponseMessage result = XPaymentAdapterResponseMessage.builder()
                .messageId(UUID.randomUUID())
                .paymentGuid(request.getPaymentGuid())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .transactionRefId(txId)
                .status(status)
                .occurredAt(OffsetDateTime.now())
                .build();

        log.info("Sending response to listener: messageId={}, paymentGuid={}, status={}",
                result.getMessageId(), result.getPaymentGuid(), result.getStatus());

        resultListener.onMessage(result);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down X Payment Adapter message broker scheduler");
        scheduler.shutdownNow();
    }
}

