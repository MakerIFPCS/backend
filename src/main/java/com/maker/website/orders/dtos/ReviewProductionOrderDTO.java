package com.maker.website.orders.dtos;

import com.maker.website.orders.ProductionOrderStatusENUM;

public record ReviewProductionOrderDTO(
        ProductionOrderStatusENUM status,
        String adminNotes
) {
}
