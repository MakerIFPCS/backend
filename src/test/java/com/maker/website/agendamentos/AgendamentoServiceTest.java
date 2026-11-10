package com.maker.website.agendamentos;

import com.maker.website.agendamentos.dtos.AgendamentoRequestDTO;
import com.maker.website.reports.ReportAuditService;
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
    @Mock ReportAuditService auditService;
    @InjectMocks AgendamentoService service;

    @Test
    void createsUpdatesAndDeletesAppointment() {
        AgendamentoEntity saved = new AgendamentoEntity();
        saved.setId(12L);
        when(repository.save(any())).thenAnswer(invocation -> {
            AgendamentoEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) entity.setId(12L);
            return entity;
        });
        var response = service.create(request(null), 3L);
        assertEquals("#0012", response.code());
        verify(repository, times(2)).save(any());

        AgendamentoEntity entity = new AgendamentoEntity();
        entity.setId(12L);
        entity.setCode("#0012");
        entity.setRequesterName("Ana");
        entity.setStatus("Aguardando análise");
        when(repository.findById(12L)).thenReturn(Optional.of(entity));
        assertEquals("#0012", service.update(12L, request("#0012"), 3L).code());
        service.delete(12L, 3L);
        verify(repository).delete(entity);
    }

    @Test
    void rejectsInvalidAppointmentRequests() {
        assertThrows(IllegalArgumentException.class, () -> service.create(requestWith(null, "email", "date", null, null), 1L));
        assertThrows(IllegalArgumentException.class, () -> service.create(requestWith("name", null, "date", null, null), 1L));
        assertThrows(IllegalArgumentException.class, () -> service.create(requestWith("name", "email", null, null, null), 1L));
        assertThrows(IllegalArgumentException.class, () -> service.create(requestWith("name", "email", "date", "Invalid", null), 1L));
        assertThrows(IllegalArgumentException.class, () -> service.create(requestWith("name", "email", "date", null, List.of("bad|name")), 1L));
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.findById(99L));
    }

    private AgendamentoRequestDTO request(String code) {
        return requestWith("Ana", "ana@example.com", "2026-12-01", "Aguardando análise", List.of("Laser"), code);
    }

    private AgendamentoRequestDTO requestWith(String name, String email, String date, String status, List<String> equipment) {
        return requestWith(name, email, date, status, equipment, null);
    }

    private AgendamentoRequestDTO requestWith(String name, String email, String date, String status,
                                              List<String> equipment, String code) {
        return new AgendamentoRequestDTO(code, name, "Aluno", "Curso", email, "1", date, "terça",
                "10:00", "11:00", "purpose", "reason", "description", equipment, status,
                "workstation", null, null, null, null);
    }
}
