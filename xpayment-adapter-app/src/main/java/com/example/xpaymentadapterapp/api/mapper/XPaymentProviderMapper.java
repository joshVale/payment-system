package com.example.xpaymentadapterapp.api.mapper;

import com.example.xpaymentadapterapp.api.dto.CreateChargeRequestDto;
import com.example.xpaymentadapterapp.api.dto.CreateChargeResponseDto;
import com.iprody.xpayment.app.api.model.ChargeResponse;
import com.iprody.xpayment.app.api.model.CreateChargeRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface XPaymentProviderMapper {

    @Mapping(target = "metadata", expression = "java(toApiMetadata(dto.metadata()))")
    CreateChargeRequest toCreateChargeRequest(CreateChargeRequestDto dto);

    @Mapping(target = "metadata", expression = "java(toDtoMetadata(response.getMetadata()))")
    @Mapping(target = "createdAt", expression = "java(toInstant(response.getCreatedAt()))")
    @Mapping(target = "chargedAt", expression = "java(toInstant(response.getChargedAt()))")
    CreateChargeResponseDto toCreateChargeResponseDto(ChargeResponse response);

    default Map<String, Object> toApiMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        return new HashMap<>(metadata);
    }

    default Map<String, Object> toDtoMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return Map.of();
        }
        return Map.copyOf(metadata);
    }

    default Instant toInstant(String iso8601) {
        if (iso8601 == null || iso8601.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(iso8601);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
}

