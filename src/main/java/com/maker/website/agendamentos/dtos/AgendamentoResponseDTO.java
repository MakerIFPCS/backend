package com.maker.website.agendamentos.dtos;

import com.maker.website.agendamentos.AgendamentoEntity;

import java.time.LocalDateTime;
import java.util.List;

public record AgendamentoResponseDTO(
        Long id,
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
        String decidedBy,
        LocalDateTime createdAt
) {
    public AgendamentoResponseDTO(AgendamentoEntity entity) {
        this(
                entity.getId(),
                entity.getCode(),
                entity.getRequesterName(),
                entity.getRequesterType(),
                entity.getCourse(),
                entity.getEmail(),
                entity.getRegistrationNumber(),
                entity.getDate(),
                entity.getDayOfWeek(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getPurpose(),
                entity.getReason(),
                entity.getDescription(),
                entity.equipmentList(),
                entity.getStatus(),
                entity.getWorkstation(),
                entity.getAdminNotes(),
                entity.getRejectionReason(),
                entity.getDecidedAt(),
                entity.getDecidedBy(),
                entity.getCreatedAt()
        );
    }
}
