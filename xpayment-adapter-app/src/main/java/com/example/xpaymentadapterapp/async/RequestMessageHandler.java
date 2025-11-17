package com.example.xpaymentadapterapp.async;

import com.example.xpaymentadapterapp.async.kafka.DltSender;
import com.example.xpaymentadapterapp.async.validation.PaymentMessageValidator;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestMessageHandler implements MessageHandler<XPaymentAdapterRequestMessage> {

    private final AsyncSender<XPaymentAdapterResponseMessage> sender;
    private final PaymentMessageValidator validator;
    private final DltSender dltSender;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void handle(XPaymentAdapterRequestMessage message) {
        log.info("Processing payment request: paymentGuid={}, amount={}, currency={}",
                message.paymentGuid(), message.amount(), message.currency());

        // Validate the message first
        PaymentMessageValidator.ValidationResult validationResult = validator.validate(message);
        
        if (!validationResult.isValid()) {
            log.warn("Invalid message received: paymentGuid={}, error={}",
                    message.paymentGuid(), validationResult.getErrorMessage());
            // Send to DLT immediately without processing
            dltSender.sendToDlt(message, validationResult.getErrorMessage());
            return;
        }

        // Schedule delayed processing (30 seconds) for valid messages
        scheduler.schedule(() -> {
            try {
                processPayment(message);
            } catch (Exception e) {
                log.error("Error processing payment: paymentGuid={}", message.paymentGuid(), e);
            }
        }, 30, TimeUnit.SECONDS);

        log.info("Payment processing scheduled for 30 seconds: paymentGuid={}", message.paymentGuid());
    }

    /**
     * Processes the payment and sends a response.
     *
     * @param message the payment request message
     */
    private void processPayment(XPaymentAdapterRequestMessage message) {
        log.info("Processing payment after delay: paymentGuid={}, amount={}, currency={}",
                message.paymentGuid(), message.amount(), message.currency());

        BigDecimal amount = message.amount();
        XPaymentAdapterStatus status;
        
        BigDecimal remainder = amount.remainder(BigDecimal.valueOf(2));
        if (remainder.compareTo(BigDecimal.ZERO) == 0) {
            status = XPaymentAdapterStatus.SUCCEEDED;
            log.info("Payment succeeded (amount is divisible by 2): paymentGuid={}", message.paymentGuid());
        } else {
            status = XPaymentAdapterStatus.CANCELED;
            log.info("Payment canceled (amount is not divisible by 2): paymentGuid={}", message.paymentGuid());
        }

        UUID transactionRefId = UUID.randomUUID();

        XPaymentAdapterResponseMessage responseMessage = new XPaymentAdapterResponseMessage(
                message.paymentGuid(),
                message.messageId(),
                message.amount(),
                message.currency(),
                transactionRefId,
                status,
                Instant.now()
        );

        // Send response
        sender.send(responseMessage);
        log.info("Payment response sent: paymentGuid={}, status={}, transactionRefId={}",
                message.paymentGuid(), status, transactionRefId);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down RequestMessageHandler scheduler");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

