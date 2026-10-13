package com.maker.website.agendamentos;

import com.maker.website.agendamentos.dtos.AgendamentoRequestDTO;
import com.maker.website.agendamentos.dtos.AgendamentoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository repository;

    public List<AgendamentoResponseDTO> findAll() {
        return repository.findAll().stream().map(AgendamentoResponseDTO::new).toList();
    }

    public AgendamentoResponseDTO findById(Long id) {
        return new AgendamentoResponseDTO(findEntity(id));
    }

    public AgendamentoResponseDTO create(AgendamentoRequestDTO request) {
        validate(request);
        AgendamentoEntity entity = new AgendamentoEntity();
        apply(entity, request);
        AgendamentoEntity saved = repository.save(entity);
        if (saved.getCode() == null || saved.getCode().isBlank()) {
            saved.setCode(String.format("#%04d", saved.getId()));
            saved = repository.save(saved);
        }
        return new AgendamentoResponseDTO(saved);
    }

    public AgendamentoResponseDTO update(Long id, AgendamentoRequestDTO request) {
        validate(request);
        AgendamentoEntity entity = findEntity(id);
        apply(entity, request);
        return new AgendamentoResponseDTO(repository.save(entity));
    }

    public void delete(Long id) {
        repository.delete(findEntity(id));
    }

    private AgendamentoEntity findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento not found."));
    }

    private void validate(AgendamentoRequestDTO request) {
        if (request.requesterName() == null || request.requesterName().isBlank()) {
            throw new IllegalArgumentException("Requester name is required.");
        }
        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Requester email is required.");
        }
        if (request.date() == null || request.date().isBlank()) {
            throw new IllegalArgumentException("Agendamento date is required.");
        }
        if (request.status() != null && !List.of("Aguardando análise", "Aprovado", "Recusado").contains(request.status())) {
            throw new IllegalArgumentException("Agendamento status is invalid.");
        }
        if (request.equipment() != null && request.equipment().stream().anyMatch(item -> item != null && item.contains("|"))) {
            throw new IllegalArgumentException("Equipment names cannot contain the reserved | character.");
        }
    }

    private void apply(AgendamentoEntity entity, AgendamentoRequestDTO request) {
        entity.setCode(request.code());
        entity.setRequesterName(request.requesterName());
        entity.setRequesterType(request.requesterType());
        entity.setCourse(request.course());
        entity.setEmail(request.email());
        entity.setRegistrationNumber(request.registrationNumber());
        entity.setDate(request.date());
        entity.setDayOfWeek(request.dayOfWeek());
        entity.setStartTime(request.startTime());
        entity.setEndTime(request.endTime());
        entity.setPurpose(request.purpose());
        entity.setReason(request.reason());
        entity.setDescription(request.description());
        entity.setEquipment(request.equipment() == null ? null : String.join("|", request.equipment()));
        entity.setStatus(request.status());
        entity.setWorkstation(request.workstation());
        entity.setAdminNotes(request.adminNotes());
        entity.setRejectionReason(request.rejectionReason());
        entity.setDecidedAt(request.decidedAt());
        entity.setDecidedBy(request.decidedBy());
    }
}
