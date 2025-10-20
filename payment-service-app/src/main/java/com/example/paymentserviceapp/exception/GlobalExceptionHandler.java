package com.example.paymentserviceapp.exception;

import com.example.paymentserviceapp.dto.ErrorDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BasePaymentSystemException.class)
    public ResponseEntity<ErrorDto> handleBaseException(BasePaymentSystemException ex) {
        ErrorDto errorDto = new ErrorDto(
                ex.getEntityId(),
                ex.getOperation(),
                ex.getMessage()
        );
        return ResponseEntity.status(ex.getStatus()).body(errorDto);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleOtherExceptions(Exception ex) {
        ErrorDto errorDto = new ErrorDto(
                null,
                "unknown-op",
                ex.getMessage()
        );
        return ResponseEntity.internalServerError().body(errorDto);
    }
}
