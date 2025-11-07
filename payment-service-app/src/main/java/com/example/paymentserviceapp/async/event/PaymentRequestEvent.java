package com.example.paymentserviceapp.async.event;

import com.example.paymentserviceapp.async.XPaymentAdapterRequestMessage;
import org.springframework.context.ApplicationEvent;

public class PaymentRequestEvent extends ApplicationEvent {

    private final XPaymentAdapterRequestMessage requestMessage;

    public PaymentRequestEvent(Object source, XPaymentAdapterRequestMessage requestMessage) {
        super(source);
        this.requestMessage = requestMessage;
    }

    public XPaymentAdapterRequestMessage getRequestMessage() {
        return requestMessage;
    }
}

