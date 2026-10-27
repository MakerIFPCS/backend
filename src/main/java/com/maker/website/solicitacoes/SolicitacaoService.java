package com.maker.website.solicitacoes;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitacaoService {
    private static final List<String> TYPES = List.of("PEDIDO", "EMPRESTIMO");
    private static final List<String> STATUSES = List.of("Aguardando análise", "Aprovado", "Recusado", "Devolvido");
    private final SolicitacaoRepository repository;

    public List<SolicitacaoDTO> findAll(String type) {
        validateType(type);
        return repository.findAllByTypeOrderByCreatedAtDesc(type).stream().map(SolicitacaoDTO::new).toList();
    }

    public SolicitacaoDTO create(SolicitacaoRequest request, String type) {
        validate(request, type);
        SolicitacaoEntity entity = new SolicitacaoEntity();
        apply(entity, request, type);
        entity.setStatus("Aguardando análise");
        SolicitacaoEntity saved = repository.save(entity);
        saved.setCode(String.format("%s-%04d", type.equals("PEDIDO") ? "PED" : "EMP", saved.getId()));
        return new SolicitacaoDTO(repository.save(saved));
    }

    public SolicitacaoDTO update(Long id, SolicitacaoRequest request, String type) {
        validate(request, type);
        SolicitacaoEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada."));
        if (!type.equals(entity.getType())) throw new IllegalArgumentException("Tipo de solicitação inválido.");
        apply(entity, request, type);
        return new SolicitacaoDTO(repository.save(entity));
    }

    private void apply(SolicitacaoEntity entity, SolicitacaoRequest request, String type) {
        entity.setType(type);
        entity.setRequesterName(request.requesterName());
        entity.setRequesterType(request.requesterType());
        entity.setEmail(request.email());
        entity.setRegistrationNumber(request.registrationNumber());
        entity.setItem(request.item());
        entity.setQuantity(request.quantity());
        entity.setRequestedAt(request.requestedAt());
        entity.setReturnDate(request.returnDate());
        entity.setPurpose(request.purpose());
        entity.setNotes(request.notes());
        entity.setStatus(request.status());
        entity.setAdminNotes(request.adminNotes());
        entity.setRejectionReason(request.rejectionReason());
        entity.setDecidedAt(request.decidedAt());
    }

    private void validate(SolicitacaoRequest request, String type) {
        validateType(type);
        if (request.requesterName() == null || request.requesterName().isBlank()
                || request.email() == null || request.email().isBlank()
                || request.item() == null || request.item().isBlank()
                || request.requestedAt() == null || request.requestedAt().isBlank()) {
            throw new IllegalArgumentException("Dados obrigatórios da solicitação não informados.");
        }
        if (request.quantity() == null || request.quantity() < 1) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        if (request.status() != null && !STATUSES.contains(request.status())) {
            throw new IllegalArgumentException("Status de solicitação inválido.");
        }
    }

    private void validateType(String type) {
        if (!TYPES.contains(type)) throw new IllegalArgumentException("Tipo de solicitação inválido.");
    }
}
