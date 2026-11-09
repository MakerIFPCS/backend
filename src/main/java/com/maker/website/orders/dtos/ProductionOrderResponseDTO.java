package com.maker.website.orders.dtos;

import com.maker.website.orders.ProductionOrderEntity;
import com.maker.website.orders.ProductionOrderStatusENUM;
import com.maker.website.orders.ProductionOrderTypeENUM;

import java.time.LocalDateTime;

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
        ProductionOrderStatusENUM status,
        String adminNotes,
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
                order.getStatus(),
                order.getAdminNotes(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
