package com.example.paymentserviceapp.mapper;

import com.example.paymentserviceapp.dto.PaymentDto;
import com.example.paymentserviceapp.dto.request.PaymentRequest;
import com.example.paymentserviceapp.dto.response.PaymentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PaymentApiMapper {

    PaymentResponse toResponse(PaymentDto paymentDto);

    List<PaymentResponse> toResponseList(List<PaymentDto> paymentDtos);

    @Mapping(target = "guid", ignore = true)
    @Mapping(target = "transactionRefId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PaymentDto toDto(PaymentRequest request);
}

