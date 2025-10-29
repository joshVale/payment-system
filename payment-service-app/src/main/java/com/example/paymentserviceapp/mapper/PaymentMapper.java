package com.example.paymentserviceapp.mapper;

import com.example.paymentserviceapp.dto.PaymentDto;
import com.example.paymentserviceapp.persistence.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import static org.mapstruct.ReportingPolicy.ERROR;

@Mapper(componentModel = "spring",  unmappedTargetPolicy = ERROR)
public interface PaymentMapper {

    PaymentDto toPaymentDto(Payment payment);

    Payment toPaymentEntity(PaymentDto paymentDto);

    @Mapping(target = "guid", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updatePaymentFromDto(PaymentDto dto, @MappingTarget Payment entity);

}
