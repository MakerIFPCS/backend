package com.maker.website.machines;

import com.maker.website.machines.dtos.CreateMachineDTO;
import com.maker.website.machines.dtos.MachineResponseDTO;
import com.maker.website.machines.dtos.UpdateMachineDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MachineControllerTest {

    @Mock MachineService service;
    @InjectMocks MachineController controller;

    @Test
    void delegatesMachineEndpoints() {
        MachineResponseDTO response = mock(MachineResponseDTO.class);
        when(service.getAllMachines()).thenReturn(List.of(response));
        when(service.getMachineById(1L)).thenReturn(response);
        when(service.createMachine(any(CreateMachineDTO.class))).thenReturn(response);
        when(service.updateMachine(eq(1L), any(UpdateMachineDTO.class))).thenReturn(response);

        assertEquals(1, controller.getAllMachines().getBody().size());
        assertEquals(response, controller.getMachineById(1L).getBody());
        assertEquals(201, controller.createMachine(mock(CreateMachineDTO.class), null).getStatusCode().value());
        assertEquals(response, controller.updateMachine(1L, mock(UpdateMachineDTO.class), null).getBody());
        assertEquals(204, controller.deleteMachine(1L, null).getStatusCode().value());
    }
}
