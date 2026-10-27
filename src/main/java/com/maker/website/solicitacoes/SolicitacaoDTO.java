package com.maker.website.solicitacoes;

public record SolicitacaoDTO(
        Long id, String code, String type, String requesterName, String requesterType,
        String email, String registrationNumber, String item, Integer quantity,
        String requestedAt, String returnDate, String purpose, String notes,
        String status, String adminNotes, String rejectionReason, String decidedAt,
        String createdAt
) {
    public SolicitacaoDTO(SolicitacaoEntity entity) {
        this(entity.getId(), entity.getCode(), entity.getType(), entity.getRequesterName(),
                entity.getRequesterType(), entity.getEmail(), entity.getRegistrationNumber(),
                entity.getItem(), entity.getQuantity(), entity.getRequestedAt(), entity.getReturnDate(),
                entity.getPurpose(), entity.getNotes(), entity.getStatus(), entity.getAdminNotes(),
                entity.getRejectionReason(), entity.getDecidedAt(),
                entity.getCreatedAt() == null ? null : entity.getCreatedAt().toString());
    }
}
