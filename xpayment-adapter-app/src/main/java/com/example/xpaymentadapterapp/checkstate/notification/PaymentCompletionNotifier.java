package com.example.xpaymentadapterapp.checkstate.notification;

import com.example.xpaymentadapterapp.api.dto.CreateChargeResponseDto;
import com.example.xpaymentadapterapp.async.AsyncSender;
import com.example.xpaymentadapterapp.async.XPaymentAdapterResponseMessage;
import com.example.xpaymentadapterapp.async.XPaymentAdapterStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Компонент для отправки уведомлений об изменении статуса платежа в payment-service.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCompletionNotifier {

    private final AsyncSender<XPaymentAdapterResponseMessage> asyncSender;

    /**
     * Отправляет уведомление об изменении статуса платежа в payment-service через Kafka.
     *
     * @param chargeResponse ответ от X Payment Provider с информацией о платеже
     */
    public void notifyPaymentCompletion(CreateChargeResponseDto chargeResponse) {
        if (chargeResponse == null) {
            log.warn("Cannot notify payment completion: chargeResponse is null");
            return;
        }

        if (chargeResponse.order() == null) {
            log.warn("Cannot notify payment completion: paymentGuid (order) is null");
            return;
        }

        XPaymentAdapterStatus adapterStatus = mapStatus(chargeResponse.status());
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
        log.info("Payment completion notification sent: paymentGuid={}, status={}, transactionRefId={}",
                responseMessage.paymentGuid(), responseMessage.status(), responseMessage.transactionRefId());
    }

    private XPaymentAdapterStatus mapStatus(String providerStatus) {
        if (providerStatus == null) {
            return XPaymentAdapterStatus.PROCESSING;
        }

        return switch (providerStatus.toLowerCase()) {
            case "succeeded" -> XPaymentAdapterStatus.SUCCEEDED;
            case "canceled", "cancelled" -> XPaymentAdapterStatus.CANCELED;
            default -> XPaymentAdapterStatus.PROCESSING;
        };
    }
}

