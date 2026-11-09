package com.maker.website.reports;

import com.maker.website.agendamentos.AgendamentoRepository;
import com.maker.website.loans.LoanRequestRepository;
import com.maker.website.materials.MaterialRepository;
import com.maker.website.machines.MachineRepository;
import com.maker.website.reports.dtos.ActivityReportDTO;
import com.maker.website.reports.dtos.ReportEventDTO;
import com.maker.website.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportEventRepository reportEventRepository;
    private final UserRepository userRepository;
    private final MachineRepository machineRepository;
    private final MaterialRepository materialRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final LoanRequestRepository loanRequestRepository;

    @Transactional(readOnly = true)
    public ActivityReportDTO getActivityReport(LocalDate from, LocalDate to) {
        if (from != null && to != null && to.isBefore(from)) {
            throw new IllegalArgumentException("The end date must be on or after the start date.");
        }

        LocalDateTime fromDate = from == null ? null : from.atStartOfDay();
        LocalDateTime untilDate = to == null ? null : to.plusDays(1).atStartOfDay();
        List<ReportEventDTO> events = new ArrayList<>();

        findAuditEvents(fromDate, untilDate).stream()
                .map(ReportEventDTO::new)
                .forEach(events::add);

        userRepository.findAll().forEach(user -> addCreationEventIfMissing(
                events, user.getCreatedAt(), "USUÁRIO", "Conta criada", user.getName(),
                "Cadastro: " + user.getEmail(), fromDate, untilDate));
        machineRepository.findAll().forEach(machine -> addCreationEventIfMissing(
                events, machine.getCreatedAt(), "MÁQUINA", "Máquina cadastrada", machine.getName(),
                "Código: " + (machine.getCode() == null ? "não informado" : machine.getCode())
                        + " · Status: " + machine.getStatus(),
                fromDate, untilDate));
        materialRepository.findAll().forEach(material -> addCreationEventIfMissing(
                events, material.getCreatedAt(), "MATERIAL", "Material cadastrado", material.getName(),
                "Estoque atual: " + material.getQuantity() + " " + material.getUnit(),
                fromDate, untilDate));
        agendamentoRepository.findAll().forEach(request -> addCreationEventIfMissing(
                events, request.getCreatedAt(), "AGENDAMENTO", "Pedido de agendamento criado",
                request.getRequesterName(),
                "Código: " + request.getCode() + " · Data solicitada: " + request.getDate(),
                fromDate, untilDate));
        loanRequestRepository.findAllByOrderByCreatedAtDesc().forEach(request -> addCreationEventIfMissing(
                events, request.getCreatedAt(), "EMPRÉSTIMO", "Pedido de empréstimo criado",
                "Pedido #" + request.getId(),
                "Solicitante: " + request.getRequester().getName()
                        + " · Itens: " + request.getItems().stream()
                        .map(item -> (item.getType().name().equals("MACHINE")
                                ? item.getMachine().getName()
                                : item.getMaterial().getName())
                                + (item.getType().name().equals("MATERIAL") ? " (quantidade: " + item.getQuantity() + ")" : ""))
                        .reduce((first, second) -> first + ", " + second)
                        .orElse("nenhum")
                        + " · Retirada: " + request.getPickupDate()
                        + " · Devolução: " + request.getReturnDate()
                        + " · Status: " + request.getStatus(),
                fromDate, untilDate));

        events.sort(Comparator.comparing(ReportEventDTO::occurredAt).reversed());
        return new ActivityReportDTO(LocalDateTime.now(), fromDate, untilDate, events);
    }

    private void addCreationEvent(
            List<ReportEventDTO> events,
            LocalDateTime occurredAt,
            String category,
            String action,
            String subject,
            String details,
            LocalDateTime fromDate,
            LocalDateTime untilDate
    ) {
        if (occurredAt == null) return;
        if (fromDate != null && occurredAt.isBefore(fromDate)) return;
        if (untilDate != null && !occurredAt.isBefore(untilDate)) return;
        events.add(new ReportEventDTO(occurredAt, category, action, subject, "Sistema", details));
    }

    private List<ReportEventEntity> findAuditEvents(LocalDateTime fromDate, LocalDateTime untilDate) {
        if (fromDate != null && untilDate != null) {
            return reportEventRepository
                    .findByOccurredAtGreaterThanEqualAndOccurredAtLessThanOrderByOccurredAtDescIdDesc(fromDate, untilDate);
        }
        if (fromDate != null) {
            return reportEventRepository.findByOccurredAtGreaterThanEqualOrderByOccurredAtDescIdDesc(fromDate);
        }
        if (untilDate != null) {
            return reportEventRepository.findByOccurredAtLessThanOrderByOccurredAtDescIdDesc(untilDate);
        }
        return reportEventRepository.findAllByOrderByOccurredAtDescIdDesc();
    }

    private void addCreationEventIfMissing(
            List<ReportEventDTO> events,
            LocalDateTime occurredAt,
            String category,
            String action,
            String subject,
            String details,
            LocalDateTime fromDate,
            LocalDateTime untilDate
    ) {
        boolean alreadyRecorded = events.stream().anyMatch(event ->
                event.category().equals(category)
                        && event.action().equals(action)
                        && event.subject().equals(subject));
        if (!alreadyRecorded) {
            addCreationEvent(events, occurredAt, category, action, subject, details, fromDate, untilDate);
        }
    }
}
