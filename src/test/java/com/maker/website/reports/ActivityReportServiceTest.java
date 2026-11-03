package com.maker.website.reports;

import com.maker.website.agendamentos.AgendamentoEntity;
import com.maker.website.agendamentos.AgendamentoRepository;
import com.maker.website.loans.LoanRequestEntity;
import com.maker.website.loans.LoanRequestRepository;
import com.maker.website.loans.LoanRequestStatusENUM;
import com.maker.website.loans.LoanResourceTypeENUM;
import com.maker.website.loans.LoanRequestItemEntity;
import com.maker.website.machines.MachineEntity;
import com.maker.website.machines.MachineRepository;
import com.maker.website.machines.enums.MachineStatusENUM;
import com.maker.website.materials.MaterialEntity;
import com.maker.website.materials.MaterialRepository;
import com.maker.website.users.UserEntity;
import com.maker.website.users.UserRepository;
import com.maker.website.users.enums.UserRoleENUM;
import com.maker.website.users.enums.UserStatusENUM;
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
class ActivityReportServiceTest {

    @Mock UserRepository userRepository;
    @Mock MachineRepository machineRepository;
    @Mock MaterialRepository materialRepository;
    @Mock AgendamentoRepository agendamentoRepository;
    @Mock LoanRequestRepository loanRequestRepository;
    @InjectMocks ActivityReportService service;

    @Test
    void aggregatesCreationAndUpdateEventsAndSortsNewestFirst() {
        LocalDateTime created = LocalDateTime.of(2026, 10, 10, 10, 0);
        LocalDateTime updated = created.plusHours(1);
        UserEntity user = new UserEntity();
        user.setName("User");
        user.setEmail("user@example.com");
        user.setRole(UserRoleENUM.USER);
        user.setCreatedAt(created);
        user.setUpdatedAt(updated);

        MachineEntity machine = new MachineEntity();
        machine.setName("Printer");
        machine.setCode("M-1");
        machine.setStatus(MachineStatusENUM.AVAILABLE);
        machine.setCreatedAt(created.plusMinutes(1));
        machine.setUpdatedAt(created.plusMinutes(2));

        MaterialEntity material = new MaterialEntity();
        material.setName("PLA");
        material.setQuantity(5);
        material.setUnit("rolls");
        material.setCreatedAt(created.plusMinutes(3));
        material.setUpdatedAt(created.plusMinutes(4));

        AgendamentoEntity appointment = new AgendamentoEntity();
        appointment.setCode("A-1");
        appointment.setPurpose("Research");
        appointment.setRequesterName("Requester");
        appointment.setEmail("requester@example.com");
        appointment.setStatus("Aprovado");
        appointment.setCreatedAt(created.plusMinutes(5));
        appointment.setDecidedAt(created.plusMinutes(6).toString());
        appointment.setDecidedBy("Admin");
        appointment.setAdminNotes("Ready");

        UserEntity requester = new UserEntity();
        requester.setName("Borrower");
        requester.setEmail("borrower@example.com");
        LoanRequestEntity loan = new LoanRequestEntity();
        loan.setId(42L);
        loan.setRequester(requester);
        loan.setStatus(LoanRequestStatusENUM.APPROVED);
        loan.setReason("Experiment");
        loan.setCreatedAt(created.plusMinutes(7));
        loan.setUpdatedAt(created.plusMinutes(8));
        LoanRequestItemEntity loanItem = new LoanRequestItemEntity();
        loanItem.setType(LoanResourceTypeENUM.MATERIAL);
        loanItem.setResourceId(9L);
        loanItem.setName("PLA");
        loanItem.setQuantity(2);
        loan.addItem(loanItem);

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(machineRepository.findAll()).thenReturn(List.of(machine));
        when(materialRepository.findAll()).thenReturn(List.of(material));
        when(agendamentoRepository.findAll()).thenReturn(List.of(appointment));
        when(loanRequestRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(loan));

        var report = service.getReport(null, null);

        assertEquals(10, report.events().size());
        assertEquals(updated, report.events().getFirst().occurredAt());
        assertTrue(report.events().stream().anyMatch(event -> event.category().equals("Empréstimos")
                && event.details().contains("PLA × 2")));
        assertTrue(report.events().stream().anyMatch(event -> event.action().equals("Solicitação analisada")
                && event.details().contains("Ready")));
        assertNotNull(report.generatedAt());
    }

    @Test
    void filtersDatesInclusivelyAndHandlesDeletedUsersAndNullCreationDates() {
        UserEntity deletedUser = new UserEntity();
        deletedUser.setName("Deleted");
        deletedUser.setEmail("deleted@example.com");
        deletedUser.setStatus(UserStatusENUM.DELETED);

        UserEntity withoutDates = new UserEntity();
        withoutDates.setName("No dates");
        withoutDates.setEmail("none@example.com");

        MachineEntity machine = new MachineEntity();
        machine.setName("Boundary machine");
        machine.setStatus(MachineStatusENUM.AVAILABLE);
        machine.setCreatedAt(LocalDate.of(2026, 10, 27).atStartOfDay());

        when(userRepository.findAll()).thenReturn(List.of(deletedUser, withoutDates));
        when(machineRepository.findAll()).thenReturn(List.of(machine));
        when(materialRepository.findAll()).thenReturn(List.of());
        when(agendamentoRepository.findAll()).thenReturn(List.of());
        when(loanRequestRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        var report = service.getReport(LocalDate.of(2026, 10, 27), LocalDate.of(2026, 10, 27));

        assertEquals(1, report.events().size());
        assertEquals("Boundary machine", report.events().getFirst().subject());
        assertEquals(LocalDate.of(2026, 10, 27).atStartOfDay(), report.fromDate());
        assertEquals(LocalDate.of(2026, 10, 28).atStartOfDay(), report.untilDate());
    }

    @Test
    void rejectsReversedDateRange() {
        assertThrows(IllegalArgumentException.class, () ->
                service.getReport(LocalDate.of(2026, 10, 28), LocalDate.of(2026, 10, 27)));
        verifyNoInteractions(userRepository, machineRepository, materialRepository, agendamentoRepository, loanRequestRepository);
    }

    @Test
    void parsesOffsetDecisionDatesAndIgnoresPendingOrInvalidDecisions() {
        AgendamentoEntity offsetDecision = appointment("A-2", "Aprovado", "2026-10-10T12:00:00+02:00");
        AgendamentoEntity invalidDecision = appointment("A-3", "Recusado", "not-a-date");
        AgendamentoEntity pendingDecision = appointment("A-4", "Aguardando análise", "2026-10-10T12:00:00");
        when(userRepository.findAll()).thenReturn(List.of());
        when(machineRepository.findAll()).thenReturn(List.of());
        when(materialRepository.findAll()).thenReturn(List.of());
        when(agendamentoRepository.findAll()).thenReturn(List.of(offsetDecision, invalidDecision, pendingDecision));
        when(loanRequestRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        var report = service.getReport(null, null);

        assertEquals(1, report.events().size());
        assertEquals(LocalDateTime.of(2026, 10, 10, 12, 0), report.events().getFirst().occurredAt());
    }

    private AgendamentoEntity appointment(String code, String status, String decidedAt) {
        AgendamentoEntity entity = new AgendamentoEntity();
        entity.setCode(code);
        entity.setPurpose("Purpose");
        entity.setRequesterName("Requester");
        entity.setStatus(status);
        entity.setDecidedAt(decidedAt);
        return entity;
    }
}
