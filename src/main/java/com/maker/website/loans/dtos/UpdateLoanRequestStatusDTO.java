package com.maker.website.loans.dtos;

import com.maker.website.loans.LoanRequestStatusENUM;

public record UpdateLoanRequestStatusDTO(
        LoanRequestStatusENUM status,
        String adminNotes
) {
}
