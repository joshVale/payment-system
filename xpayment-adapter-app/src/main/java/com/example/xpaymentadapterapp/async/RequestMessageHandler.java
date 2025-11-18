package com.example.xpaymentadapterapp.async;

import com.example.xpaymentadapterapp.api.XPaymentProviderGateway;
import com.example.xpaymentadapterapp.api.dto.CreateChargeRequestDto;
import com.example.xpaymentadapterapp.api.dto.CreateChargeResponseDto;
import com.example.xpaymentadapterapp.async.kafka.DltSender;
import com.example.xpaymentadapterapp.async.validation.PaymentMessageValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestMessageHandler implements MessageHandler<XPaymentAdapterRequestMessage> {

    private final AsyncSender<XPaymentAdapterResponseMessage> sender;
    private final PaymentMessageValidator validator;
    private final DltSender dltSender;
    private final XPaymentProviderGateway xPaymentProviderGateway;

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

        log.debug("Message validation passed. Preparing create charge request for paymentGuid={}", message.paymentGuid());
        CreateChargeRequestDto requestDto = mapToCreateChargeRequest(message);

        try {
            CreateChargeResponseDto providerResponse = xPaymentProviderGateway.createCharge(requestDto);
            XPaymentAdapterResponseMessage responseMessage = mapToAdapterResponse(message, providerResponse);
            sender.send(responseMessage);
            log.info("Payment response sent: paymentGuid={}, status={}, transactionRefId={}",
                    responseMessage.paymentGuid(), responseMessage.status(), responseMessage.transactionRefId());
        } catch (RestClientException ex) {
            log.error("Error calling X Payment Provider: paymentGuid={}", message.paymentGuid(), ex);
            dltSender.sendToDlt(message, ex.getMessage());
            throw ex;
        }
    }

    private CreateChargeRequestDto mapToCreateChargeRequest(XPaymentAdapterRequestMessage message) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("paymentGuid", message.paymentGuid().toString());
        metadata.put("messageId", message.messageId().toString());
        metadata.put("requestOccurredAt", message.occurredAt().toString());
        metadata.put("amount", message.amount());

        return new CreateChargeRequestDto(
                message.amount(),
                message.currency(),
                buildCustomerName(message.paymentGuid()),
                message.paymentGuid(),
                buildReceiptEmail(message.paymentGuid()),
                metadata
        );
    }

    private XPaymentAdapterResponseMessage mapToAdapterResponse(
            XPaymentAdapterRequestMessage original,
            CreateChargeResponseDto providerResponse
    ) {
        BigDecimal amount = providerResponse.amount() != null ? providerResponse.amount() : original.amount();
        String currency = providerResponse.currency() != null ? providerResponse.currency() : original.currency();
        UUID transactionRefId = providerResponse.id() != null ? providerResponse.id() : original.paymentGuid();
        Instant occurredAt = resolveOccurredAt(providerResponse, original);

        return new XPaymentAdapterResponseMessage(
                original.paymentGuid(),
                original.messageId(),
                amount,
                currency,
                transactionRefId,
                mapStatus(providerResponse.status()),
                occurredAt
        );
    }

    private Instant resolveOccurredAt(CreateChargeResponseDto providerResponse, XPaymentAdapterRequestMessage original) {
        if (providerResponse.chargedAt() != null) {
            return providerResponse.chargedAt();
        }
        if (providerResponse.createdAt() != null) {
            return providerResponse.createdAt();
        }
        if (original.occurredAt() != null) {
            return original.occurredAt();
        }
        return Instant.now();
    }

    private XPaymentAdapterStatus mapStatus(String providerStatus) {
        if (providerStatus == null) {
            return XPaymentAdapterStatus.PROCESSING;
        }

        return switch (providerStatus.toLowerCase(Locale.ROOT)) {
            case "succeeded" -> XPaymentAdapterStatus.SUCCEEDED;
            case "canceled", "cancelled" -> XPaymentAdapterStatus.CANCELED;
            default -> XPaymentAdapterStatus.PROCESSING;
        };
    }

    private String buildCustomerName(UUID paymentGuid) {
        return "Payment-" + paymentGuid;
    }

    private String buildReceiptEmail(UUID paymentGuid) {
        return paymentGuid + "@xpayment-adapter.local";
    }
}

