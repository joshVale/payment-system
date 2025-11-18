package com.example.xpaymentadapterapp.api;

import com.example.xpaymentadapterapp.api.dto.CreateChargeRequestDto;
import com.example.xpaymentadapterapp.api.dto.CreateChargeResponseDto;
import com.example.xpaymentadapterapp.api.mapper.XPaymentProviderMapper;
import com.iprody.xpayment.app.api.client.DefaultApi;
import com.iprody.xpayment.app.api.model.ChargeResponse;
import com.iprody.xpayment.app.api.model.CreateChargeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
class XPaymentProviderGatewayImpl implements XPaymentProviderGateway {

    private final DefaultApi defaultApi;
    private final XPaymentProviderMapper mapper;

    @Override
    public CreateChargeResponseDto createCharge(CreateChargeRequestDto createChargeRequest) throws RestClientException {
        CreateChargeRequest request = mapper.toCreateChargeRequest(createChargeRequest);
        ChargeResponse response = defaultApi.createCharge(request);
        return mapper.toCreateChargeResponseDto(requireResponseBody(response, "POST /charges"));
    }

    @Override
    public CreateChargeResponseDto retrieveCharge(UUID id) throws RestClientException {
        ChargeResponse response = defaultApi.retrieveCharge(id);
        return mapper.toCreateChargeResponseDto(requireResponseBody(response, "GET /charges/{id}"));
    }

    private ChargeResponse requireResponseBody(ChargeResponse response, String operation) {
        if (response == null) {
            throw new RestClientException(operation + " returned empty body");
        }
        return response;
    }
}