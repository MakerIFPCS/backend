package com.maker.website.reports;

import com.maker.website.agendamentos.AgendamentoEntity;
import com.maker.website.agendamentos.AgendamentoRepository;
import com.maker.website.loans.LoanRequestEntity;
import com.maker.website.loans.LoanRequestRepository;
import com.maker.website.machines.MachineEntity;
import com.maker.website.machines.MachineRepository;
import com.maker.website.materials.MaterialEntity;
import com.maker.website.materials.MaterialRepository;
import com.maker.website.reports.dtos.ActivityReportEventDTO;
import com.maker.website.reports.dtos.ActivityReportResponseDTO;
import com.maker.website.users.UserEntity;
import com.maker.website.users.UserRepository;
import com.maker.website.users.enums.UserStatusENUM;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityReportService {

    private final UserRepository userRepository;
    private final MachineRepository machineRepository;
    private final MaterialRepository materialRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final LoanRequestRepository loanRequestRepository;

    @Transactional(readOnly = true)
    public ActivityReportResponseDTO getReport(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }

        LocalDateTime fromDate = from == null ? null : from.atStartOfDay();
        LocalDateTime untilDate = to == null ? null : to.plusDays(1).atStartOfDay();
        List<ActivityReportEventDTO> events = new ArrayList<>();

        userRepository.findAll().forEach(user -> addUserEvents(user, events, fromDate, untilDate));
        machineRepository.findAll().forEach(machine -> addMachineEvents(machine, events, fromDate, untilDate));
        materialRepository.findAll().forEach(material -> addMaterialEvents(material, events, fromDate, untilDate));
        agendamentoRepository.findAll().forEach(agendamento -> addAgendamentoEvents(agendamento, events, fromDate, untilDate));
        loanRequestRepository.findAllByOrderByCreatedAtDesc().forEach(request -> addLoanEvents(request, events, fromDate, untilDate));

        events.sort(Comparator.comparing(ActivityReportEventDTO::occurredAt).reversed());
        return new ActivityReportResponseDTO(LocalDateTime.now(), fromDate, untilDate, List.copyOf(events));
    }

    private void addUserEvents(
            UserEntity user,
            List<ActivityReportEventDTO> events,
            LocalDateTime from,
            LocalDateTime until
    ) {
        addEvent(events, from, until, user.getCreatedAt(), "Usuários", "Cadastro criado",
                display(user.getName(), user.getEmail()), display(user.getName(), user.getEmail()), "Perfil " + user.getRole() + ".");
        if (hasUpdated(user.getCreatedAt(), user.getUpdatedAt())) {
            addEvent(events, from, until, user.getUpdatedAt(), "Usuários", "Cadastro atualizado",
                    display(user.getName(), user.getEmail()), "Administração", "Perfil " + user.getRole() + ".");
        }
    }

    private void addMachineEvents(
            MachineEntity machine,
            List<ActivityReportEventDTO> events,
            LocalDateTime from,
            LocalDateTime until
    ) {
        addEvent(events, from, until, machine.getCreatedAt(), "Máquinas", "Máquina cadastrada",
                display(machine.getName(), machine.getCode()), "Administração", "Status: " + machine.getStatus() + ".");
        if (hasUpdated(machine.getCreatedAt(), machine.getUpdatedAt())) {
            addEvent(events, from, until, machine.getUpdatedAt(), "Máquinas", "Máquina atualizada",
                    display(machine.getName(), machine.getCode()), "Administração", "Status atual: " + machine.getStatus() + ".");
        }
    }

    private void addMaterialEvents(
            MaterialEntity material,
            List<ActivityReportEventDTO> events,
            LocalDateTime from,
            LocalDateTime until
    ) {
        addEvent(events, from, until, material.getCreatedAt(), "Materiais", "Material cadastrado",
                material.getName(), "Administração", "Estoque: " + material.getQuantity() + " " + material.getUnit() + ".");
        if (hasUpdated(material.getCreatedAt(), material.getUpdatedAt())) {
            addEvent(events, from, until, material.getUpdatedAt(), "Materiais", "Estoque ou cadastro atualizado",
                    material.getName(), "Administração", "Estoque atual: " + material.getQuantity() + " " + material.getUnit() + ".");
        }
    }

    private void addAgendamentoEvents(
            AgendamentoEntity agendamento,
            List<ActivityReportEventDTO> events,
            LocalDateTime from,
            LocalDateTime until
    ) {
        String subject = display(agendamento.getCode(), agendamento.getPurpose());
        addEvent(events, from, until, agendamento.getCreatedAt(), "Agendamentos", "Solicitação criada",
                subject, display(agendamento.getRequesterName(), agendamento.getEmail()), agendamento.getStatus());

        LocalDateTime decidedAt = parseDateTime(agendamento.getDecidedAt());
        if (decidedAt != null && !isPending(agendamento.getStatus())) {
            addEvent(events, from, until, decidedAt, "Agendamentos", "Solicitação analisada",
                    subject, display(agendamento.getDecidedBy(), "Administração"),
                    agendamento.getStatus() + (agendamento.getAdminNotes() == null || agendamento.getAdminNotes().isBlank()
                            ? "."
                            : " — " + agendamento.getAdminNotes()));
        }
    }

    private void addLoanEvents(
            LoanRequestEntity request,
            List<ActivityReportEventDTO> events,
            LocalDateTime from,
            LocalDateTime until
    ) {
        String subject = "Pedido de empréstimo #" + request.getId();
        String items = request.getItems().stream()
                .map(item -> item.getName() + (item.getQuantity() > 1 ? " × " + item.getQuantity() : ""))
                .reduce((first, second) -> first + ", " + second)
                .orElse("Sem itens");
        addEvent(events, from, until, request.getCreatedAt(), "Empréstimos", "Pedido enviado",
                subject, display(request.getRequester().getName(), request.getRequester().getEmail()), items);

        if (hasUpdated(request.getCreatedAt(), request.getUpdatedAt())) {
            String details = request.getStatus().name();
            if (request.getAdminNotes() != null && !request.getAdminNotes().isBlank()) {
                details += " — " + request.getAdminNotes();
            }
            addEvent(events, from, until, request.getUpdatedAt(), "Empréstimos", "Pedido analisado",
                    subject, "Administração", details);
        }
    }

    private void addEvent(
            List<ActivityReportEventDTO> events,
            LocalDateTime from,
            LocalDateTime until,
            LocalDateTime occurredAt,
            String category,
            String action,
            String subject,
            String actor,
            String details
    ) {
        if (occurredAt == null
                || (from != null && occurredAt.isBefore(from))
                || (until != null && !occurredAt.isBefore(until))) {
            return;
        }
        events.add(new ActivityReportEventDTO(
                occurredAt,
                category,
                action,
                subject,
                actor,
                details
        ));
    }

    private boolean hasUpdated(LocalDateTime createdAt, LocalDateTime updatedAt) {
        return createdAt != null && updatedAt != null && updatedAt.isAfter(createdAt);
    }

    private boolean isPending(String status) {
        return status == null || status.isBlank()
                || status.equalsIgnoreCase("PENDING")
                || status.equalsIgnoreCase("Aguardando análise");
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException localDateTimeError) {
            try {
                return OffsetDateTime.parse(value).toLocalDateTime();
            } catch (DateTimeParseException offsetDateTimeError) {
                return null;
            }
        }
    }

    private String display(String first, String fallback) {
        if (first != null && !first.isBlank()) return first;
        if (fallback != null && !fallback.isBlank()) return fallback;
        return "—";
    }
}
