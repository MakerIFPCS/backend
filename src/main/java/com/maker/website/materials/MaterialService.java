package com.maker.website.materials;

import com.maker.website.materials.dtos.MaterialRequestDTO;
import com.maker.website.materials.dtos.MaterialResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;

    @Transactional(readOnly = true)
    public List<MaterialResponseDTO> getAllMaterials() {
        return materialRepository.findAllByOrderByNameAsc()
                .stream().map(MaterialResponseDTO::new).toList();
    }

    @Transactional
    public MaterialResponseDTO createMaterial(MaterialRequestDTO request) {
        validate(request);
        MaterialEntity material = new MaterialEntity();
        apply(material, request);
        return new MaterialResponseDTO(materialRepository.save(material));
    }

    @Transactional
    public MaterialResponseDTO updateMaterial(Long id, MaterialRequestDTO request) {
        validate(request);
        MaterialEntity material = materialRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Material not found."));
        apply(material, request);
        return new MaterialResponseDTO(materialRepository.save(material));
    }

    private void validate(MaterialRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Material details are required.");
        }
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Material name is required.");
        }
        if (request.name().trim().length() > 120) {
            throw new IllegalArgumentException("Material name must contain at most 120 characters.");
        }
        if (request.description() != null && request.description().length() > 500) {
            throw new IllegalArgumentException("Material description must contain at most 500 characters.");
        }
        if (request.quantity() == null || request.quantity() < 0) {
            throw new IllegalArgumentException("Material quantity must be zero or greater.");
        }
        if (request.unit() == null || request.unit().isBlank()) {
            throw new IllegalArgumentException("Material unit is required.");
        }
        if (request.unit().trim().length() > 30) {
            throw new IllegalArgumentException("Material unit must contain at most 30 characters.");
        }
    }

    private void apply(MaterialEntity material, MaterialRequestDTO request) {
        material.setName(request.name().trim());
        material.setDescription(request.description() == null ? "" : request.description().trim());
        material.setQuantity(request.quantity());
        material.setUnit(request.unit().trim());
    }
}
