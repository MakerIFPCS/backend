package com.maker.website.reports.dtos;

import com.maker.website.reports.ReportEventEntity;

import java.time.LocalDateTime;

public record ReportEventDTO(
        LocalDateTime occurredAt,
        String category,
        String action,
        String subject,
        String actor,
        String details
) {
    public ReportEventDTO(ReportEventEntity event) {
        this(
                event.getOccurredAt(),
                event.getCategory(),
                event.getAction(),
                event.getSubject(),
                event.getActor(),
                event.getDetails()
        );
    }
}
