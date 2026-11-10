package com.maker.website.reports;

import com.maker.website.agendamentos.AgendamentoEntity;
import com.maker.website.agendamentos.AgendamentoRepository;
import com.maker.website.loans.LoanRequestEntity;
import com.maker.website.loans.LoanRequestItemEntity;
import com.maker.website.loans.LoanRequestRepository;
import com.maker.website.loans.LoanRequestStatusENUM;
import com.maker.website.loans.LoanResourceTypeENUM;
import com.maker.website.machines.MachineEntity;
import com.maker.website.machines.MachineRepository;
import com.maker.website.machines.enums.MachineStatusENUM;
import com.maker.website.materials.MaterialEntity;
import com.maker.website.materials.MaterialRepository;
import com.maker.website.users.UserEntity;
import com.maker.website.users.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock ReportEventRepository eventRepository;
    @Mock UserRepository userRepository;
    @Mock MachineRepository machineRepository;
    @Mock MaterialRepository materialRepository;
    @Mock AgendamentoRepository agendamentoRepository;
    @Mock LoanRequestRepository loanRequestRepository;
    @InjectMocks ReportService service;

    @Test
    void buildsReportFromAuditAndEntityCreationEvents() {
        LocalDateTime occurredAt = LocalDateTime.of(2026, 11, 10, 10, 0);
        ReportEventEntity audit = new ReportEventEntity();
        audit.setOccurredAt(occurredAt);
        audit.setCategory("USUÁRIO");
        audit.setAction("Conta criada");
        audit.setSubject("Ana");
        audit.setActor("Sistema");
        audit.setDetails("Cadastro");
        when(eventRepository.findAllByOrderByOccurredAtDescIdDesc()).thenReturn(List.of(audit));

        UserEntity user = new UserEntity();
        user.setName("Ana");
        user.setEmail("ana@example.com");
        user.setCreatedAt(occurredAt);
        when(userRepository.findAll()).thenReturn(List.of(user));

        MachineEntity machine = new MachineEntity();
        machine.setName("Printer");
        machine.setCode(null);
        machine.setStatus(MachineStatusENUM.AVAILABLE);
        machine.setCreatedAt(occurredAt);
        when(machineRepository.findAll()).thenReturn(List.of(machine));

        MaterialEntity material = new MaterialEntity();
        material.setName("PLA");
        material.setQuantity(3);
        material.setUnit("kg");
        material.setCreatedAt(occurredAt);
        when(materialRepository.findAll()).thenReturn(List.of(material));

        AgendamentoEntity appointment = new AgendamentoEntity();
        appointment.setRequesterName("Bruno");
        appointment.setCode("#1");
        appointment.setDate("2026-12-01");
        appointment.setCreatedAt(occurredAt);
        when(agendamentoRepository.findAll()).thenReturn(List.of(appointment));

        UserEntity requester = new UserEntity();
        requester.setName("Carla");
        LoanRequestEntity loan = new LoanRequestEntity();
        loan.setId(8L);
        loan.setRequester(requester);
        loan.setPickupDate(LocalDate.of(2026, 12, 1));
        loan.setReturnDate(LocalDate.of(2026, 12, 2));
        loan.setStatus(LoanRequestStatusENUM.PENDING);
        loan.setCreatedAt(occurredAt);
        LoanRequestItemEntity item = new LoanRequestItemEntity();
        item.setType(LoanResourceTypeENUM.MATERIAL);
        item.setQuantity(2);
        MaterialEntity loanMaterial = new MaterialEntity();
        loanMaterial.setName("ABS");
        item.setMaterial(loanMaterial);
        loan.addItem(item);
        when(loanRequestRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(loan));

        var report = service.getActivityReport(null, null);

        assertEquals(5, report.events().size());
        assertEquals("USUÁRIO", report.events().get(0).category());
        verify(eventRepository).findAllByOrderByOccurredAtDescIdDesc();
    }

    @Test
    void selectsDateQueriesAndRejectsInvertedRange() {
        LocalDate from = LocalDate.of(2026, 11, 1);
        LocalDate to = LocalDate.of(2026, 11, 10);
        when(eventRepository.findByOccurredAtGreaterThanEqualAndOccurredAtLessThanOrderByOccurredAtDescIdDesc(
                from.atStartOfDay(), to.plusDays(1).atStartOfDay())).thenReturn(List.of());
        when(userRepository.findAll()).thenReturn(List.of());
        when(machineRepository.findAll()).thenReturn(List.of());
        when(materialRepository.findAll()).thenReturn(List.of());
        when(agendamentoRepository.findAll()).thenReturn(List.of());
        when(loanRequestRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        assertTrue(service.getActivityReport(from, to).events().isEmpty());
        verify(eventRepository).findByOccurredAtGreaterThanEqualAndOccurredAtLessThanOrderByOccurredAtDescIdDesc(
                from.atStartOfDay(), to.plusDays(1).atStartOfDay());

        service.getActivityReport(from, null);
        service.getActivityReport(null, to);
        verify(eventRepository).findByOccurredAtGreaterThanEqualOrderByOccurredAtDescIdDesc(from.atStartOfDay());
        verify(eventRepository).findByOccurredAtLessThanOrderByOccurredAtDescIdDesc(to.plusDays(1).atStartOfDay());

        assertThrows(IllegalArgumentException.class,
                () -> service.getActivityReport(to, from));
    }
}
