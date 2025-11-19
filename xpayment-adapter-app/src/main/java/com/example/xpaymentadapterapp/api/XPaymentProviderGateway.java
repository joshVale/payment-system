package com.example.xpaymentadapterapp.api;

import com.example.xpaymentadapterapp.api.dto.CreateChargeRequestDto;
import com.example.xpaymentadapterapp.api.dto.CreateChargeResponseDto;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

public interface XPaymentProviderGateway {

    CreateChargeResponseDto createCharge(CreateChargeRequestDto createChargeRequest) throws RestClientException;

    CreateChargeResponseDto retrieveCharge(UUID id) throws RestClientException;
}
