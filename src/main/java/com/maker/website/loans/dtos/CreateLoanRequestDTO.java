package com.maker.website.loans.dtos;

import java.time.LocalDate;
import java.util.List;

public record CreateLoanRequestDTO(
        List<LoanRequestItemDTO> items,
        LocalDate pickupDate,
        LocalDate returnDate,
        String reason
) {
}
