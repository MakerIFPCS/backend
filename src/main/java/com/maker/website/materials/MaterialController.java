package com.maker.website.materials;

import com.maker.website.materials.dtos.MaterialRequestDTO;
import com.maker.website.materials.dtos.MaterialResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @GetMapping
    public ResponseEntity<List<MaterialResponseDTO>> getAllMaterials() {
        return ResponseEntity.ok(materialService.getAllMaterials());
    }

    @PostMapping
    public ResponseEntity<MaterialResponseDTO> createMaterial(@RequestBody MaterialRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(materialService.createMaterial(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaterialResponseDTO> updateMaterial(
            @PathVariable Long id,
            @RequestBody MaterialRequestDTO request
    ) {
        return ResponseEntity.ok(materialService.updateMaterial(id, request));
    }
}
