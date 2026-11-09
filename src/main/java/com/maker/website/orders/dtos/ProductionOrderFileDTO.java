package com.maker.website.orders.dtos;

public record ProductionOrderFileDTO(
        String fileName,
        String contentType,
        byte[] fileData
) {
}
