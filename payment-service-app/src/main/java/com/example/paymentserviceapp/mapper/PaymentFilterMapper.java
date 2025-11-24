package com.example.paymentserviceapp.mapper;

import com.example.paymentserviceapp.dto.request.PaymentFilterRequest;
import com.example.paymentserviceapp.persistency.PaymentFilter;
import org.mapstruct.Mapper;

import static org.mapstruct.ReportingPolicy.ERROR;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ERROR)
public interface PaymentFilterMapper {

    PaymentFilter toServiceFilter(PaymentFilterRequest  request);
}
