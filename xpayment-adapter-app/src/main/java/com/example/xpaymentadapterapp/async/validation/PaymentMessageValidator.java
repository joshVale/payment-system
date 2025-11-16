package com.example.xpaymentadapterapp.async.validation;

import com.example.xpaymentadapterapp.async.XPaymentAdapterRequestMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Validator for payment request messages according to business rules.
 * Validates amount, currency, and decimal places according to ISO 4217.
 */
@Slf4j
@Service
public class PaymentMessageValidator {

    /**
     * Map of currency codes to their decimal places according to ISO 4217.
     * Most currencies have 2 decimal places, but there are exceptions.
     */
    private static final Map<String, Integer> CURRENCY_DECIMAL_PLACES = Map.ofEntries(
            // 0 decimal places
            Map.entry("JPY", 0),  // Japanese Yen
            Map.entry("KRW", 0),  // South Korean Won
            Map.entry("CLP", 0),  // Chilean Peso
            Map.entry("VND", 0),  // Vietnamese Dong
            Map.entry("XPF", 0),  // CFP Franc
            Map.entry("XOF", 0),  // West African CFA Franc
            Map.entry("XAF", 0),  // Central African CFA Franc
            // 3 decimal places
            Map.entry("BHD", 3),  // Bahraini Dinar
            Map.entry("IQD", 3),  // Iraqi Dinar
            Map.entry("JOD", 3),  // Jordanian Dinar
            Map.entry("KWD", 3),  // Kuwaiti Dinar
            Map.entry("LYD", 3),  // Libyan Dinar
            Map.entry("OMR", 3),  // Omani Rial
            Map.entry("TND", 3)   // Tunisian Dinar
    );

    /**
     * Default decimal places for currencies not in the map (most currencies use 2).
     */
    private static final int DEFAULT_DECIMAL_PLACES = 2;

    /**
     * Validates a payment request message.
     *
     * @param message the message to validate
     * @return ValidationResult containing validation status and error message if invalid
     */
    public ValidationResult validate(XPaymentAdapterRequestMessage message) {
        if (message == null) {
            return ValidationResult.invalid("Message cannot be null");
        }

        // Check if amount is present
        if (message.amount() == null) {
            return ValidationResult.invalid("Amount is required");
        }

        // Check if currency is present
        if (message.currency() == null || message.currency().isBlank()) {
            return ValidationResult.invalid("Currency is required");
        }

        // Check if amount is non-negative
        if (message.amount().compareTo(BigDecimal.ZERO) < 0) {
            return ValidationResult.invalid("Amount cannot be negative");
        }

        // Check decimal places according to ISO 4217
        String currency = message.currency().toUpperCase().trim();
        int allowedDecimalPlaces = CURRENCY_DECIMAL_PLACES.getOrDefault(currency, DEFAULT_DECIMAL_PLACES);
        
        BigDecimal scaledAmount = message.amount().setScale(allowedDecimalPlaces, RoundingMode.DOWN);
        if (message.amount().compareTo(scaledAmount) != 0) {
            return ValidationResult.invalid(
                    String.format("Amount has too many decimal places for currency %s. Maximum allowed: %d",
                            currency, allowedDecimalPlaces)
            );
        }

        log.debug("Message validation passed for paymentGuid={}, amount={}, currency={}",
                message.paymentGuid(), message.amount(), currency);

        return ValidationResult.valid();
    }

    /**
     * Result of message validation.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}

