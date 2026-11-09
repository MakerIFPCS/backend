package com.maker.website.reports;

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

@Entity
@Table(name = "report_events")
@Getter
@Setter
@NoArgsConstructor
public class ReportEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String category;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(nullable = false, length = 180)
    private String subject;

    @Column(nullable = false, length = 180)
    private String actor;

    @Column(columnDefinition = "text")
    private String details;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    @PrePersist
    public void prePersist() {
        if (occurredAt == null) occurredAt = LocalDateTime.now();
    }
}
