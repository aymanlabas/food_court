package com.foodcourt.usagers.mapper;

import com.foodcourt.usagers.dto.PaymentResponse;
import com.foodcourt.usagers.entity.Payment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentResponse toResponse(Payment payment);

    List<PaymentResponse> toResponseList(List<Payment> payments);
}

