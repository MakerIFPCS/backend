package com.maker.website.machines.dtos;

import com.maker.website.machines.MachineEntity;
import com.maker.website.machines.enums.MachineAccessENUM;
import com.maker.website.machines.enums.MachineStatusENUM;
import java.time.LocalDateTime;

public record MachineResponseDTO(
        Long id,
        String name,
        String description,
        MachineStatusENUM status,
        MachineAccessENUM access,
        String code,
        String type,
        String image,
        LocalDateTime createdAt,
        LocalDateTime updatedAt


) {
    public MachineResponseDTO(MachineEntity machine) {
        this(
                machine.getId(),
                machine.getName(),
                machine.getDescription(),
                machine.getStatus(),
                machine.getAccess(),
                machine.getCode(),
                machine.getType(),
                machine.getImage(),
                machine.getCreatedAt(),
                machine.getUpdatedAt()
        );
    }
}
