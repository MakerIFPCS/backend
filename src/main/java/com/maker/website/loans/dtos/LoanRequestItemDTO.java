package com.maker.website.loans.dtos;

import com.maker.website.loans.LoanResourceTypeENUM;

public record LoanRequestItemDTO(
        LoanResourceTypeENUM type,
        Long resourceId,
        Integer quantity
) {
}
