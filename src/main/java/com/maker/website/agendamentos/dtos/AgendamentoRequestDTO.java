package com.maker.website.agendamentos.dtos;

import java.util.List;

public record AgendamentoRequestDTO(
        String code,
        String requesterName,
        String requesterType,
        String course,
        String email,
        String registrationNumber,
        String date,
        String dayOfWeek,
        String startTime,
        String endTime,
        String purpose,
        String reason,
        String description,
        List<String> equipment,
        String status,
        String workstation,
        String adminNotes,
        String rejectionReason,
        String decidedAt,
        String decidedBy
) {
}
