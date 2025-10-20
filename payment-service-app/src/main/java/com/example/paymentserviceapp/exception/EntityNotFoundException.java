package com.example.paymentserviceapp.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class EntityNotFoundException extends BasePaymentSystemException {

    public EntityNotFoundException(String message, String operation, UUID entityId) {
        super(message, operation, entityId, HttpStatus.NOT_FOUND);
    }
}
