package com.example.paymentserviceapp.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class EntityNotFoundException extends BasePaymentSystemException {

    private final String operation;
    private final UUID entityId;

    public EntityNotFoundException(String message, String operation, UUID entityId) {
        super(message);
        this.operation = operation;
        this.entityId = entityId;
    }
}
