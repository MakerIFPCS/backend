package com.maker.website.orders.dtos;

import com.maker.website.orders.ProductionOrderCommentEntity;

import java.time.LocalDateTime;

public record ProductionOrderCommentResponseDTO(
        Long id,
        String authorName,
        String authorRole,
        String message,
        LocalDateTime createdAt
) {
    public ProductionOrderCommentResponseDTO(ProductionOrderCommentEntity comment) {
        this(
                comment.getId(),
                comment.getAuthor().getName(),
                comment.getAuthor().getRole().name(),
                comment.getMessage(),
                comment.getCreatedAt()
        );
    }
}
