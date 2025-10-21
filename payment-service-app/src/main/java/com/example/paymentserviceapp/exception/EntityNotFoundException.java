package com.example.paymentserviceapp.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@Getter
public class EntityNotFoundException extends BasePaymentSystemException {

    private final String operation;
    private final UUID entityId;
    private final HttpStatus status;

    public EntityNotFoundException(String message, String operation, UUID entityId) {
        super(message);
        this.operation = operation;
        this.entityId = entityId;
        this.status = HttpStatus.NOT_FOUND;
    }
}
