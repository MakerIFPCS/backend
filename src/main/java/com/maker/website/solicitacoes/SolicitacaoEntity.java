package com.maker.website.solicitacoes;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacoes")
@Getter
@Setter
@NoArgsConstructor
public class SolicitacaoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private String type;
    private String requesterName;
    private String requesterType;
    private String email;
    private String registrationNumber;
    private String item;
    private Integer quantity;
    private String requestedAt;
    private String returnDate;
    @Column(length = 4000)
    private String purpose;
    @Column(length = 4000)
    private String notes;
    private String status;
    private String adminNotes;
    private String rejectionReason;
    private String decidedAt;
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null || status.isBlank()) status = "Aguardando análise";
    }
}
