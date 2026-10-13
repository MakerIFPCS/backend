package com.maker.website.machines.dtos;

import com.maker.website.machines.enums.MachineStatusENUM;
import com.maker.website.machines.enums.MachineAccessENUM;

public record UpdateMachineDTO(
        String name,
        String description,

        MachineStatusENUM status,
        String code,
        String type,
        String image,
        MachineAccessENUM access
) {

}
