package com.maker.website.loans.dtos;

import com.maker.website.loans.LoanRequestItemEntity;
import com.maker.website.loans.LoanResourceTypeENUM;

public record LoanRequestItemResponseDTO(
        LoanResourceTypeENUM type,
        Long resourceId,
        Integer quantity,
        String name
) {
    public LoanRequestItemResponseDTO(LoanRequestItemEntity item) {
        this(item.getType(), item.getResourceId(), item.getQuantity(), item.getName());
    }
}
