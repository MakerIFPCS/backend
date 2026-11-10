package com.maker.website.orders.dtos;

import com.maker.website.orders.ProductionOrderEntity;
import com.maker.website.orders.ProductionOrderStatusENUM;
import com.maker.website.orders.ProductionOrderTypeENUM;

import java.time.LocalDateTime;
import java.util.List;

public record ProductionOrderResponseDTO(
        Long id,
        String requesterName,
        String requesterEmail,
        ProductionOrderTypeENUM type,
        Long machineId,
        String machineName,
        String fileName,
        long fileSize,
        String notes,
        String dimensions,
        ProductionOrderStatusENUM status,
        String adminNotes,
        LocalDateTime estimatedCompletionAt,
        List<ProductionOrderCommentResponseDTO> comments,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public ProductionOrderResponseDTO(ProductionOrderEntity order) {
        this(
                order.getId(),
                order.getRequester().getName(),
                order.getRequester().getEmail(),
                order.getType(),
                order.getMachine() == null ? null : order.getMachine().getId(),
                order.getMachine() == null ? null : order.getMachine().getName(),
                order.getFileName(),
                order.getFileSize(),
                order.getNotes(),
                order.getDimensions(),
                order.getStatus(),
                order.getAdminNotes(),
                order.getEstimatedCompletionAt(),
                order.getComments().stream().map(ProductionOrderCommentResponseDTO::new).toList(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
