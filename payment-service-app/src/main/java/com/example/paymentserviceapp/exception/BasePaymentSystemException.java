package com.example.paymentserviceapp.exception;

public abstract class BasePaymentSystemException extends RuntimeException {

    public BasePaymentSystemException() {
        super();
    }

    public BasePaymentSystemException(String message) {
        super(message);
    }

    public BasePaymentSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public BasePaymentSystemException(Throwable cause) {
        super(cause);
    }

    public BasePaymentSystemException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

