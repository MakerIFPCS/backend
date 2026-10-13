package com.maker.website.machines;
import com.maker.website.machines.enums.MachineAccessENUM;
import com.maker.website.machines.enums.MachineStatusENUM;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "machines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor



public class MachineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String code;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MachineStatusENUM status;

    @Enumerated(EnumType.STRING)
    private MachineAccessENUM access;

    private String type;

    @Column(columnDefinition = "text")
    private String image;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;

        if (this.access == null) {
            this.access = MachineAccessENUM.RESTRICTED;
        }
        if (this.status == null) {
            this.status = MachineStatusENUM.MAINTENANCE;
        }
    }

}
