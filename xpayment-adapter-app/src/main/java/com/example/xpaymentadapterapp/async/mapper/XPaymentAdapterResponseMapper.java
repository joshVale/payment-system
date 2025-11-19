package com.example.xpaymentadapterapp.async.mapper;

import com.example.xpaymentadapterapp.api.dto.CreateChargeResponseDto;
import com.example.xpaymentadapterapp.async.XPaymentAdapterRequestMessage;
import com.example.xpaymentadapterapp.async.XPaymentAdapterResponseMessage;
import com.example.xpaymentadapterapp.async.XPaymentAdapterStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface XPaymentAdapterResponseMapper {

    @Mapping(target = "paymentGuid", source = "original.paymentGuid")
    @Mapping(target = "messageId", source = "original.messageId")
    @Mapping(target = "amount", expression = "java(resolveAmount(providerResponse, original))")
    @Mapping(target = "currency", expression = "java(resolveCurrency(providerResponse, original))")
    @Mapping(target = "transactionRefId", expression = "java(resolveTransactionRefId(providerResponse, original))")
    @Mapping(target = "status", expression = "java(mapStatus(providerResponse.status()))")
    @Mapping(target = "occurredAt", expression = "java(resolveOccurredAt(providerResponse, original))")
    XPaymentAdapterResponseMessage toResponseMessage(
            XPaymentAdapterRequestMessage original,
            CreateChargeResponseDto providerResponse
    );

    default BigDecimal resolveAmount(CreateChargeResponseDto providerResponse, XPaymentAdapterRequestMessage original) {
        return providerResponse.amount() != null ? providerResponse.amount() : original.amount();
    }

    default String resolveCurrency(CreateChargeResponseDto providerResponse, XPaymentAdapterRequestMessage original) {
        return providerResponse.currency() != null ? providerResponse.currency() : original.currency();
    }

    default UUID resolveTransactionRefId(CreateChargeResponseDto providerResponse, XPaymentAdapterRequestMessage original) {
        return providerResponse.id() != null ? providerResponse.id() : original.paymentGuid();
    }

    default Instant resolveOccurredAt(CreateChargeResponseDto providerResponse, XPaymentAdapterRequestMessage original) {
        if (providerResponse.chargedAt() != null) {
            return providerResponse.chargedAt();
        }
        if (providerResponse.createdAt() != null) {
            return providerResponse.createdAt();
        }
        if (original.occurredAt() != null) {
            return original.occurredAt();
        }
        return Instant.now();
    }

    default XPaymentAdapterStatus mapStatus(String providerStatus) {
        if (providerStatus == null) {
            return XPaymentAdapterStatus.PROCESSING;
        }

        return switch (providerStatus.toLowerCase(Locale.ROOT)) {
            case "succeeded" -> XPaymentAdapterStatus.SUCCEEDED;
            case "canceled", "cancelled" -> XPaymentAdapterStatus.CANCELED;
            default -> XPaymentAdapterStatus.PROCESSING;
        };
    }
}

