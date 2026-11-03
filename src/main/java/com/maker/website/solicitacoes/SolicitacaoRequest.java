package com.maker.website.solicitacoes;

public record SolicitacaoRequest(
        String code, String type, String requesterName, String requesterType,
        String email, String registrationNumber, String item, Integer quantity,
        String requestedAt, String returnDate, String purpose, String notes,
        String status, String adminNotes, String rejectionReason, String decidedAt
) {}
