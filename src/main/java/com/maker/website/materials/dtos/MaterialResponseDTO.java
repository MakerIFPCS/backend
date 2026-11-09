package com.maker.website.materials.dtos;

import com.maker.website.materials.MaterialEntity;

import java.time.LocalDateTime;

public record MaterialResponseDTO(
        Long id,
        String name,
        String description,
        Integer quantity,
        String unit,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public MaterialResponseDTO(MaterialEntity material) {
        this(
                material.getId(),
                material.getName(),
                material.getDescription(),
                material.getQuantity(),
                material.getUnit(),
                material.getCreatedAt(),
                material.getUpdatedAt()
        );
    }
}
