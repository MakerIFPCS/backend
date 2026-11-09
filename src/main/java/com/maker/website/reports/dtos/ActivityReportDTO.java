package com.maker.website.reports.dtos;

import java.time.LocalDateTime;
import java.util.List;

public record ActivityReportDTO(
        LocalDateTime generatedAt,
        LocalDateTime fromDate,
        LocalDateTime untilDate,
        List<ReportEventDTO> events
) {
}
