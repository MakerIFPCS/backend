package com.maker.website.agendamentos;

import com.maker.website.agendamentos.dtos.AgendamentoRequestDTO;
import com.maker.website.agendamentos.dtos.AgendamentoResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoControllerTest {

    @Mock AgendamentoService service;
    @InjectMocks AgendamentoController controller;

    @Test
    void delegatesAppointmentEndpoints() {
        AgendamentoResponseDTO response = mock(AgendamentoResponseDTO.class);
        AgendamentoRequestDTO request = mock(AgendamentoRequestDTO.class);
        when(service.findAll()).thenReturn(List.of(response));
        when(service.findById(2L)).thenReturn(response);
        when(service.create(request)).thenReturn(response);
        when(service.update(2L, request)).thenReturn(response);

        assertEquals(1, controller.findAll().size());
        assertEquals(response, controller.findById(2L));
        assertEquals(201, controller.create(request).getStatusCode().value());
        assertEquals(response, controller.update(2L, request));
        controller.delete(2L);
        verify(service).delete(2L);
    }
}
