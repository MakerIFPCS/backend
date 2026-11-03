package com.maker.website.machines;

import com.maker.website.machines.dtos.CreateMachineDTO;
import com.maker.website.machines.dtos.UpdateMachineDTO;
import com.maker.website.machines.enums.MachineAccessENUM;
import com.maker.website.machines.enums.MachineStatusENUM;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MachineServiceTest {

    @Mock MachineRepository machineRepository;
    @InjectMocks MachineService machineService;

    @Test
    void listsMachinesExcludingUnusableAndFindsMachine() {
        MachineEntity machine = machine(1L);
        when(machineRepository.findAllByStatusNot(MachineStatusENUM.UNUSABLE)).thenReturn(List.of(machine));
        when(machineRepository.findByIdAndStatusNot(1L, MachineStatusENUM.UNUSABLE)).thenReturn(Optional.of(machine));

        assertEquals(1, machineService.getAllMachines().size());
        assertEquals(1L, machineService.getMachineById(1L).id());
        assertThrows(IllegalArgumentException.class, () -> machineService.getMachineById(2L));
    }

    @Test
    void createsMachineAndValidatesNameAndCode() {
        when(machineRepository.existsByName("Printer")).thenReturn(false);
        when(machineRepository.existsByCode("M-1")).thenReturn(false);
        when(machineRepository.save(any(MachineEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var result = machineService.createMachine(new CreateMachineDTO(
                "Printer", "Description", null, "M-1", "3D", "data:image/png;base64,abc",
                MachineAccessENUM.RESTRICTED));
        assertEquals(MachineStatusENUM.AVAILABLE, result.status());
        assertEquals("data:image/png;base64,abc", result.image());

        assertThrows(IllegalArgumentException.class, () -> machineService.createMachine(
                new CreateMachineDTO(" ", "Description", null, null, null, null, null)));
        when(machineRepository.existsByName("Printer")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> machineService.createMachine(
                new CreateMachineDTO("Printer", "Description", null, "M-1", null, null, null)));
        when(machineRepository.existsByName("Another")).thenReturn(false);
        when(machineRepository.existsByCode("M-1")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> machineService.createMachine(
                new CreateMachineDTO("Another", "Description", null, "M-1", null, null, null)));
    }

    @Test
    void updatesProvidedFieldsAndAllowsBlankCode() {
        MachineEntity machine = machine(1L);
        when(machineRepository.findByIdAndStatusNot(1L, MachineStatusENUM.UNUSABLE)).thenReturn(Optional.of(machine));
        when(machineRepository.existsByName("Changed")).thenReturn(false);
        when(machineRepository.existsByCode("M-2")).thenReturn(false);
        when(machineRepository.save(any(MachineEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var updated = machineService.updateMachine(1L, new UpdateMachineDTO(
                "Changed", "New description", MachineStatusENUM.FUNCTIONAL,
                "M-2", "CNC", "image-data", MachineAccessENUM.FREE));
        assertEquals("Changed", updated.name());
        assertEquals(MachineStatusENUM.FUNCTIONAL, updated.status());
        assertEquals("image-data", updated.image());
        assertEquals(MachineAccessENUM.FREE, updated.access());

        machineService.updateMachine(1L, new UpdateMachineDTO(null, null, null, "", null, null, null));
        assertThrows(IllegalArgumentException.class, () -> machineService.updateMachine(2L, new UpdateMachineDTO(
                "Changed", null, null, null, null, null, null)));
    }

    @Test
    void deletesMachineByMarkingItUnusable() {
        MachineEntity machine = machine(1L);
        when(machineRepository.findByIdAndStatusNot(1L, MachineStatusENUM.UNUSABLE)).thenReturn(Optional.of(machine));
        when(machineRepository.save(machine)).thenReturn(machine);

        machineService.deleteMachine(1L);

        assertEquals(MachineStatusENUM.UNUSABLE, machine.getStatus());
        verify(machineRepository).save(machine);
    }

    private MachineEntity machine(Long id) {
        MachineEntity entity = new MachineEntity();
        entity.setId(id);
        entity.setName("Printer");
        entity.setDescription("Description");
        entity.setCode("M-1");
        entity.setType("3D");
        entity.setStatus(MachineStatusENUM.AVAILABLE);
        entity.setAccess(MachineAccessENUM.RESTRICTED);
        return entity;
    }
}
