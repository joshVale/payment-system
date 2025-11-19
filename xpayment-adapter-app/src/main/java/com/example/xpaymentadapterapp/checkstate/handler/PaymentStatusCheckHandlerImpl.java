package com.example.xpaymentadapterapp.checkstate.handler;

import com.example.xpaymentadapterapp.api.XPaymentProviderGateway;
import com.example.xpaymentadapterapp.api.dto.CreateChargeResponseDto;
import com.example.xpaymentadapterapp.async.AsyncSender;
import com.example.xpaymentadapterapp.async.XPaymentAdapterResponseMessage;
import com.example.xpaymentadapterapp.async.XPaymentAdapterStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentStatusCheckHandlerImpl implements PaymentStatusCheckHandler {

    private final XPaymentProviderGateway xPaymentProviderGateway;
    private final AsyncSender<XPaymentAdapterResponseMessage> asyncSender;

    @Override
    public boolean handle(UUID chargeGuid) {
        try {
            CreateChargeResponseDto chargeResponse = xPaymentProviderGateway.retrieveCharge(chargeGuid);
            
            if (chargeResponse == null) {
                log.warn("Charge response is null for chargeGuid: {}", chargeGuid);
                return false;
            }

            String status = chargeResponse.status();
            if (status == null) {
                log.debug("Charge status is null for chargeGuid: {}, treating as processing", chargeGuid);
                return false;
            }

            String statusLower = status.toLowerCase(Locale.ROOT);
            boolean isTerminal = "succeeded".equals(statusLower) || "canceled".equals(statusLower) || "cancelled".equals(statusLower);

            if (isTerminal) {
                log.info("Charge {} has terminal status: {}, sending notification", chargeGuid, status);
                
                XPaymentAdapterStatus adapterStatus = mapStatus(status);
                XPaymentAdapterResponseMessage responseMessage = new XPaymentAdapterResponseMessage(
                        chargeResponse.order(),
                        UUID.randomUUID(), // messageId
                        chargeResponse.amount(),
                        chargeResponse.currency(),
                        chargeResponse.id(),
                        adapterStatus,
                        chargeResponse.chargedAt() != null ? chargeResponse.chargedAt() : Instant.now()
                );
                
                asyncSender.send(responseMessage);
                return true;
            }

            log.debug("Charge {} is still processing, status: {}", chargeGuid, status);
            return false;

        } catch (RestClientException ex) {
            log.error("Error retrieving charge status for chargeGuid: {}", chargeGuid, ex);
            return false;
        }
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
}

