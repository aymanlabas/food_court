package com.foodcourt.commandes.mapper;

import com.foodcourt.commandes.dto.OrderItemResponse;
import com.foodcourt.commandes.dto.OrderResponse;
import com.foodcourt.commandes.entity.MealOrder;
import com.foodcourt.commandes.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "userEmail", ignore = true)
    OrderResponse toResponse(MealOrder order);

    List<OrderResponse> toResponseList(List<MealOrder> orders);

    @Mapping(target = "dishNom", ignore = true)
    @Mapping(target = "sousTotal", expression = "java(calculateSubTotal(item))")
    OrderItemResponse toItemResponse(OrderItem item);

    List<OrderItemResponse> toItemResponseList(List<OrderItem> items);

    default BigDecimal calculateSubTotal(OrderItem item) {
        if (item == null || item.getPrixUnitaire() == null || item.getQuantite() == null) {
            return BigDecimal.ZERO;
        }
        return item.getPrixUnitaire().multiply(BigDecimal.valueOf(item.getQuantite()));
    }
}

