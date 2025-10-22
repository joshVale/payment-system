package com.example.paymentserviceapp.exception;

public abstract class BasePaymentSystemException extends RuntimeException {

    public BasePaymentSystemException(String message) {
        super(message);
    }

    public BasePaymentSystemException(String message, Throwable cause) {
        super(message, cause);
    }

}

