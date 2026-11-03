package com.maker.website.loans.dtos;

import com.maker.website.loans.LoanRequestEntity;
import com.maker.website.loans.LoanRequestStatusENUM;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record LoanRequestResponseDTO(
        Long id,
        String requesterName,
        String requesterEmail,
        List<LoanRequestItemResponseDTO> items,
        LocalDate pickupDate,
        LocalDate returnDate,
        String reason,
        LoanRequestStatusENUM status,
        String adminNotes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public LoanRequestResponseDTO(LoanRequestEntity request) {
        this(
                request.getId(),
                request.getRequester().getName(),
                request.getRequester().getEmail(),
                request.getItems().stream().map(LoanRequestItemResponseDTO::new).toList(),
                request.getPickupDate(),
                request.getReturnDate(),
                request.getReason(),
                request.getStatus(),
                request.getAdminNotes(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }
}
