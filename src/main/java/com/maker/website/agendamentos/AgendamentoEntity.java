package com.maker.website.agendamentos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "agendamentos")
@Getter
@Setter
@NoArgsConstructor
public class AgendamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private String requesterName;
    private String requesterType;
    private String course;
    private String email;
    private String registrationNumber;
    @Column(name = "request_date")
    private String date;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private String purpose;

    @Column(length = 4000)
    private String reason;

    @Column(length = 4000)
    private String description;

    @Column(length = 2000)
    private String equipment;

    private String status;
    private String workstation;
    private String adminNotes;
    private String rejectionReason;
    private String decidedAt;
    private String decidedBy;
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null || status.isBlank()) status = "Aguardando análise";
    }

    public List<String> equipmentList() {
        if (equipment == null || equipment.isBlank()) return List.of();
        return List.of(equipment.split("\\|", -1));
    }
}
