package com.maker.website.machines;

import com.maker.website.machines.enums.MachineAccessENUM;
import com.maker.website.machines.enums.MachineStatusENUM;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MachineEntityTest {

    @Test
    void initializesDefaultAccessAndStatus() {
        MachineEntity machine = new MachineEntity();

        machine.prePersist();

        assertEquals(MachineAccessENUM.RESTRICTED, machine.getAccess());
        assertEquals(MachineStatusENUM.MAINTENANCE, machine.getStatus());
        assertEquals(machine.getCreatedAt(), machine.getUpdatedAt());
    }

    @Test
    void preservesConfiguredAccessAndStatus() {
        MachineEntity machine = new MachineEntity();
        machine.setAccess(MachineAccessENUM.FREE);
        machine.setStatus(MachineStatusENUM.AVAILABLE);

        machine.prePersist();

        assertEquals(MachineAccessENUM.FREE, machine.getAccess());
        assertEquals(MachineStatusENUM.AVAILABLE, machine.getStatus());
    }
}
