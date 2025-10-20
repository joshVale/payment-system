package com.example.paymentserviceapp.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@Getter
public abstract class BasePaymentSystemException extends RuntimeException {

    private final String operation;
    private final UUID entityId;
    private final HttpStatus status;

    protected BasePaymentSystemException(String message, String operation, UUID entityId, HttpStatus status) {
        super(message);
        this.operation = operation;
        this.entityId = entityId;
        this.status = status;
    }
}

