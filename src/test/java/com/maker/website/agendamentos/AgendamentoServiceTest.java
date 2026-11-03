package com.maker.website.agendamentos;

import com.maker.website.agendamentos.dtos.AgendamentoRequestDTO;
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
class AgendamentoServiceTest {

    @Mock AgendamentoRepository repository;
    @InjectMocks AgendamentoService service;

    @Test
    void listsFindsAndDeletesAppointments() {
        AgendamentoEntity entity = appointment();
        when(repository.findAll()).thenReturn(List.of(entity));
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        assertEquals(1, service.findAll().size());
        assertEquals("REQ-1", service.findById(1L).code());
        assertThrows(IllegalArgumentException.class, () -> service.findById(2L));

        service.delete(1L);
        verify(repository).delete(entity);
    }

    @Test
    void createsAppointmentAndGeneratesCodeWhenMissing() {
        when(repository.save(any(AgendamentoEntity.class))).thenAnswer(invocation -> {
            AgendamentoEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) entity.setId(7L);
            return entity;
        });

        var created = service.create(request(null, List.of("Laser")));
        assertEquals("#0007", created.code());
        assertEquals(List.of("Laser"), created.equipment());
        verify(repository, times(2)).save(any(AgendamentoEntity.class));
    }

    @Test
    void createsAppointmentWithProvidedCodeAndUpdatesExistingAppointment() {
        AgendamentoEntity entity = appointment();
        when(repository.save(any(AgendamentoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var created = service.create(request("REQ-2", null));
        assertEquals("REQ-2", created.code());
        assertEquals(List.of(), created.equipment());

        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        var updated = service.update(1L, request("REQ-3", List.of("CNC")));
        assertEquals("REQ-3", updated.code());
        assertEquals(List.of("CNC"), updated.equipment());
    }

    @Test
    void validatesRequiredValuesStatusAndEquipmentDelimiter() {
        assertThrows(IllegalArgumentException.class, () -> service.create(new AgendamentoRequestDTO(
                "X", "Name", null, null, "email", null, "", null, null, null, null, null, null,
                null, null, null, null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> service.create(new AgendamentoRequestDTO(
                "X", "Name", null, null, "email", null, "2030-01-01", null, null, null, null, null, null,
                null, "Invalid", null, null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> service.create(request("X", List.of("Laser|CNC"))));
        assertThrows(IllegalArgumentException.class, () -> service.create(new AgendamentoRequestDTO(
                "X", " ", null, null, "email", null, "2030-01-01", null, null, null, null, null, null,
                null, null, null, null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> service.create(new AgendamentoRequestDTO(
                "X", "Name", null, null, " ", null, "2030-01-01", null, null, null, null, null, null,
                null, null, null, null, null, null, null)));
    }

    private AgendamentoRequestDTO request(String code, List<String> equipment) {
        return new AgendamentoRequestDTO(code, "Name", "ALUNO", null, "name@example.com", "123",
                "2030-01-01", "Tuesday", "10:00", "11:00", "Purpose", "Reason", "Description",
                equipment, "Aguardando análise", null, null, null, null, null);
    }

    private AgendamentoEntity appointment() {
        AgendamentoEntity entity = new AgendamentoEntity();
        entity.setId(1L);
        entity.setCode("REQ-1");
        entity.setRequesterName("Name");
        entity.setEmail("name@example.com");
        entity.setDate("2030-01-01");
        entity.setEquipment("Laser|CNC");
        entity.setStatus("Aguardando análise");
        return entity;
    }
}
