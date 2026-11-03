package com.maker.website.reports;

import com.maker.website.reports.dtos.ActivityReportResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityReportControllerTest {

    @Mock ActivityReportService service;
    @InjectMocks ActivityReportController controller;

    @Test
    void passesOptionalDateRangeToService() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);
        ActivityReportResponseDTO report = new ActivityReportResponseDTO(LocalDateTime.now(), null, null, List.of());
        when(service.getReport(from, to)).thenReturn(report);

        assertEquals(report, controller.getActivityReport(from, to).getBody());
        verify(service).getReport(from, to);
    }
}
