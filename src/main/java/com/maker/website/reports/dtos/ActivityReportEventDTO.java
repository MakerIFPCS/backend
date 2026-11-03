package com.maker.website.reports.dtos;

import java.time.LocalDateTime;

public record ActivityReportEventDTO(
        LocalDateTime occurredAt,
        String category,
        String action,
        String subject,
        String actor,
        String details
) {
}
