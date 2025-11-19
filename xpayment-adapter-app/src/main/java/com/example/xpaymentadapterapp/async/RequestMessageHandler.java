package com.example.xpaymentadapterapp.async;

import com.example.xpaymentadapterapp.api.XPaymentProviderGateway;
import com.example.xpaymentadapterapp.api.dto.CreateChargeRequestDto;
import com.example.xpaymentadapterapp.api.dto.CreateChargeResponseDto;
import com.example.xpaymentadapterapp.async.kafka.DltSender;
import com.example.xpaymentadapterapp.async.mapper.XPaymentAdapterResponseMapper;
import com.example.xpaymentadapterapp.async.validation.PaymentMessageValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
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
    private final XPaymentAdapterResponseMapper responseMapper;

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
            XPaymentAdapterResponseMessage responseMessage = responseMapper.toResponseMessage(message, providerResponse);
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

    private String buildCustomerName(UUID paymentGuid) {
        return "Payment-" + paymentGuid;
    }

    private String buildReceiptEmail(UUID paymentGuid) {
        return paymentGuid + "@xpayment-adapter.local";
    }
}

