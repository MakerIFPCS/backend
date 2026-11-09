package com.maker.website.materials.dtos;

public record CreateMaterialDTO(
        String name,
        String description,
        Integer quantity,
        String unit
) {
}
