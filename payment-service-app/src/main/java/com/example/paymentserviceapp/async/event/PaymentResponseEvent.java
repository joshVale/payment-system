package com.example.paymentserviceapp.async.event;

import com.example.paymentserviceapp.async.XPaymentAdapterResponseMessage;
import org.springframework.context.ApplicationEvent;

public class PaymentResponseEvent extends ApplicationEvent {

    private final XPaymentAdapterResponseMessage responseMessage;

    public PaymentResponseEvent(Object source, XPaymentAdapterResponseMessage responseMessage) {
        super(source);
        this.responseMessage = responseMessage;
    }

    public XPaymentAdapterResponseMessage getResponseMessage() {
        return responseMessage;
    }
}

