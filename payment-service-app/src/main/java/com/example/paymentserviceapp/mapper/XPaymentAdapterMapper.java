package com.example.paymentserviceapp.mapper;

import com.example.paymentserviceapp.async.XPaymentAdapterRequestMessage;
import com.example.paymentserviceapp.persistence.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.ReportingPolicy.ERROR;

@Mapper(componentModel = "spring", imports = {java.util.UUID.class}, unmappedTargetPolicy = ERROR)
public interface XPaymentAdapterMapper {
    @Mapping(source = "guid", target = "paymentGuid")
    @Mapping(source = "amount", target = "amount")
    @Mapping(source = "currency", target = "currency")
    @Mapping(target = "messageId", expression = "java(UUID.randomUUID())")
    @Mapping(source = "updatedAt", target = "occurredAt")
    XPaymentAdapterRequestMessage toXPaymentAdapterRequestMessage(Payment payment);
}