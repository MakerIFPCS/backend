package com.maker.website.materials.dtos;

public record MaterialRequestDTO(
        String name,
        String description,
        Integer quantity,
        String unit
) {
}
