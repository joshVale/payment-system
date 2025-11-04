package com.example.paymentserviceapp.async;

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
@RequiredArgsConstructor
@Slf4j
public class InMemoryXPaymentAdapterMessageBroker implements AsyncSender<XPaymentAdapterRequestMessage> {

    private final AsyncListener<XPaymentAdapterResponseMessage> resultListener;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    @Override
    public void send(XPaymentAdapterRequestMessage request) {
        log.info("Received payment request: messageId={}, paymentGuid={}, amount={}, currency={}",
                request.messageId(), request.paymentGuid(), request.amount(), request.currency());

        UUID txId = UUID.randomUUID();
        log.debug("Generated transaction reference ID: {}", txId);

        log.debug("Payment request sent to adapter: paymentGuid={}, messageId={}",
                request.paymentGuid(), request.messageId());

        // Schedule async processing after 30 seconds
        scheduler.schedule(() -> processPayment(request, txId), 30, TimeUnit.SECONDS);

        log.info("Payment request scheduled for processing in 30 seconds: paymentGuid={}, transactionRefId={}",
                request.paymentGuid(), txId);
    }

    private void processPayment(XPaymentAdapterRequestMessage request, UUID txId) {
        log.info("Processing payment: paymentGuid={}, transactionRefId={}, amount={}",
                request.paymentGuid(), txId, request.amount());

        // Determine status based on amount divisibility by 2
        XPaymentAdapterStatus status = isAmountDivisibleByTwo(request.amount())
                ? XPaymentAdapterStatus.SUCCEEDED
                : XPaymentAdapterStatus.CANCELED;

        log.info("Payment processing completed: paymentGuid={}, status={}, reason={}",
                request.paymentGuid(), status,
                status == XPaymentAdapterStatus.SUCCEEDED ? "Amount is divisible by 2" : "Amount is not divisible by 2");

        emit(request, txId, status);
    }

    private boolean isAmountDivisibleByTwo(BigDecimal amount) {
        return amount.remainder(BigDecimal.valueOf(2)).compareTo(BigDecimal.ZERO) == 0;
    }

    private void emit(XPaymentAdapterRequestMessage request, UUID txId, XPaymentAdapterStatus status) {
        log.debug("Emitting response message: paymentGuid={}, status={}, transactionRefId={}",
                request.paymentGuid(), status, txId);

        XPaymentAdapterResponseMessage result = new XPaymentAdapterResponseMessage(
                request.paymentGuid(),
                UUID.randomUUID(),
                request.amount(),
                request.currency(),
                txId,
                status,
                OffsetDateTime.now()
        );

        log.info("Sending response to listener: messageId={}, paymentGuid={}, status={}",
                result.messageId(), result.paymentGuid(), result.status());

        resultListener.onMessage(result);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down X Payment Adapter message broker scheduler");
        scheduler.shutdownNow();
    }
}

