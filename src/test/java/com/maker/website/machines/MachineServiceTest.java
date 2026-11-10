package com.maker.website.machines;

import com.maker.website.machines.dtos.CreateMachineDTO;
import com.maker.website.machines.dtos.UpdateMachineDTO;
import com.maker.website.machines.enums.MachineAccessENUM;
import com.maker.website.machines.enums.MachineStatusENUM;
import com.maker.website.reports.ReportAuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MachineServiceTest {

    @Mock MachineRepository repository;
    @Mock ReportAuditService auditService;
    @InjectMocks MachineService service;

    @Test
    void createsUpdatesAndDeletesMachine() {
        when(repository.existsByName("Printer")).thenReturn(false);
        when(repository.existsByCode("P1")).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> {
            MachineEntity machine = invocation.getArgument(0);
            machine.setId(1L);
            return machine;
        });
        var created = service.createMachine(new CreateMachineDTO("Printer", "Desc", null, "P1",
                "Impressao", null, null), 7L);
        assertEquals(MachineStatusENUM.AVAILABLE, created.status());

        MachineEntity machine = machine(1L);
        when(repository.findByIdAndStatusNot(1L, MachineStatusENUM.UNUSABLE)).thenReturn(Optional.of(machine));
        service.updateMachine(1L, new UpdateMachineDTO("Printer 2", "New", MachineStatusENUM.MAINTENANCE,
                "P2", "Laser", "image", MachineAccessENUM.FREE), 7L);
        assertEquals("Printer 2", machine.getName());
        assertEquals(MachineStatusENUM.MAINTENANCE, machine.getStatus());

        service.deleteMachine(1L, 7L);
        assertEquals(MachineStatusENUM.UNUSABLE, machine.getStatus());
    }

    @Test
    void rejectsMissingAndDuplicateMachineData() {
        assertThrows(IllegalArgumentException.class, () -> service.createMachine(
                new CreateMachineDTO("", "d", null, null, null, null, null), 1L));
        when(repository.existsByName("Printer")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.createMachine(
                new CreateMachineDTO("Printer", "d", null, null, null, null, null), 1L));
        when(repository.findByIdAndStatusNot(9L, MachineStatusENUM.UNUSABLE)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.findMachineById(9L));
    }

    private MachineEntity machine(Long id) {
        MachineEntity machine = new MachineEntity();
        machine.setId(id);
        machine.setName("Printer");
        machine.setCode("P1");
        machine.setStatus(MachineStatusENUM.AVAILABLE);
        return machine;
    }
}
