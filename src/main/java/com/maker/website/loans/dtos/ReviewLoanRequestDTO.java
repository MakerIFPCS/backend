package com.maker.website.loans.dtos;

import com.maker.website.loans.LoanRequestStatusENUM;

public record ReviewLoanRequestDTO(
        LoanRequestStatusENUM status,
        String adminNotes
) {
}
