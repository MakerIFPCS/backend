package com.maker.website.reports.dtos;

import java.time.LocalDateTime;
import java.util.List;

public record ActivityReportResponseDTO(
        LocalDateTime generatedAt,
        LocalDateTime fromDate,
        LocalDateTime untilDate,
        List<ActivityReportEventDTO> events
) {
}
