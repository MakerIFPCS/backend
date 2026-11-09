package com.maker.website.materials;

import com.maker.website.reports.ReportAuditService;
import com.maker.website.materials.dtos.CreateMaterialDTO;
import com.maker.website.materials.dtos.MaterialResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final ReportAuditService reportAuditService;

    public List<MaterialResponseDTO> getAllMaterials() {
        return materialRepository.findAllByOrderByNameAsc()
                .stream()
                .map(MaterialResponseDTO::new)
                .toList();
    }

    @Transactional
    public MaterialResponseDTO createMaterial(CreateMaterialDTO dto, Long actorId) {
        validate(dto);

        MaterialEntity material = new MaterialEntity();
        material.setName(dto.name().trim());
        material.setDescription(dto.description() == null ? "" : dto.description().trim());
        material.setQuantity(dto.quantity());
        material.setUnit(dto.unit().trim());

        MaterialEntity savedMaterial = materialRepository.save(material);
        reportAuditService.record("MATERIAL", "Material cadastrado", savedMaterial.getName(), actorId,
                "Estoque inicial: " + savedMaterial.getQuantity() + " " + savedMaterial.getUnit());
        return new MaterialResponseDTO(savedMaterial);
    }

    @Transactional
    public MaterialResponseDTO updateMaterial(Long id, CreateMaterialDTO dto, Long actorId) {
        validate(dto);
        MaterialEntity material = materialRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Material not found."));
        String oldName = material.getName();
        String oldDescription = material.getDescription();
        int oldQuantity = material.getQuantity();
        String oldUnit = material.getUnit();

        material.setName(dto.name().trim());
        material.setDescription(dto.description() == null ? "" : dto.description().trim());
        material.setQuantity(dto.quantity());
        material.setUnit(dto.unit().trim());

        MaterialEntity updatedMaterial = materialRepository.save(material);
        reportAuditService.record("MATERIAL", "Material atualizado", updatedMaterial.getName(), actorId,
                "Nome: " + oldName + " → " + updatedMaterial.getName()
                        + " · Descrição: " + (oldDescription == null ? "" : oldDescription)
                        + " → " + updatedMaterial.getDescription()
                        + " · Estoque: " + oldQuantity + " " + oldUnit
                        + " → " + updatedMaterial.getQuantity() + " " + updatedMaterial.getUnit());
        return new MaterialResponseDTO(updatedMaterial);
    }

    private void validate(CreateMaterialDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Material data is required.");
        if (dto.name() == null || dto.name().isBlank()) {
            throw new IllegalArgumentException("Material name is required.");
        }
        if (dto.name().trim().length() > 120) {
            throw new IllegalArgumentException("Material name must contain at most 120 characters.");
        }
        if (dto.quantity() == null || dto.quantity() < 0) {
            throw new IllegalArgumentException("Material quantity must be zero or greater.");
        }
        if (dto.unit() == null || dto.unit().isBlank()) {
            throw new IllegalArgumentException("Material unit is required.");
        }
        if (dto.unit().trim().length() > 30) {
            throw new IllegalArgumentException("Material unit must contain at most 30 characters.");
        }
        if (dto.description() != null && dto.description().length() > 500) {
            throw new IllegalArgumentException("Material description must contain at most 500 characters.");
        }
    }
}
