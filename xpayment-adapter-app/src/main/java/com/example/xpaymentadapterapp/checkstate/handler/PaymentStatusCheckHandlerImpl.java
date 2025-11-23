package com.example.xpaymentadapterapp.checkstate.handler;

import com.example.xpaymentadapterapp.api.XPaymentProviderGateway;
import com.example.xpaymentadapterapp.api.dto.ChargeStatus;
import com.example.xpaymentadapterapp.api.dto.CreateChargeResponseDto;
import com.example.xpaymentadapterapp.checkstate.notification.PaymentCompletionNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentStatusCheckHandlerImpl implements PaymentStatusCheckHandler {

    private final XPaymentProviderGateway xPaymentProviderGateway;
    private final PaymentCompletionNotifier paymentCompletionNotifier;

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

            boolean isTerminal = ChargeStatus.isTerminal(status);

            if (isTerminal) {
                log.info("Charge {} has terminal status: {}, notifying payment-service", chargeGuid, status);
                paymentCompletionNotifier.notifyPaymentCompletion(chargeResponse);
                return true;
            }

            log.debug("Charge {} is still processing, status: {}", chargeGuid, status);
            return false;

        } catch (RestClientException ex) {
            log.error("Error retrieving charge status for chargeGuid: {}", chargeGuid, ex);
            return false;
        }
    }
}

