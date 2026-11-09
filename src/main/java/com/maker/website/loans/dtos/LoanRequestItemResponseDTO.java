package com.maker.website.loans.dtos;

import com.maker.website.loans.LoanRequestItemEntity;
import com.maker.website.loans.LoanResourceTypeENUM;

public record LoanRequestItemResponseDTO(
        LoanResourceTypeENUM type,
        Long resourceId,
        String name,
        Integer quantity
) {
    public LoanRequestItemResponseDTO(LoanRequestItemEntity item) {
        this(
                item.getType(),
                item.getType() == LoanResourceTypeENUM.MACHINE ? item.getMachine().getId() : item.getMaterial().getId(),
                item.getType() == LoanResourceTypeENUM.MACHINE ? item.getMachine().getName() : item.getMaterial().getName(),
                item.getQuantity()
        );
    }
}
