package com.maker.website.materials;

import com.maker.website.materials.dtos.MaterialRequestDTO;
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
class MaterialServiceTest {

    @Mock MaterialRepository repository;
    @InjectMocks MaterialService service;

    @Test
    void listsAndCreatesMaterials() {
        when(repository.findAllByOrderByNameAsc()).thenReturn(List.of(material(1L, 3)));
        assertEquals(1, service.getAllMaterials().size());

        when(repository.save(any(MaterialEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var result = service.createMaterial(new MaterialRequestDTO(" Filament ", null, 4, " rolls "));
        assertEquals("Filament", result.name());
        assertEquals("", result.description());
        assertEquals("rolls", result.unit());
        assertEquals(4, result.quantity());
    }

    @Test
    void updatesMaterialAndRejectsMissingMaterial() {
        MaterialEntity entity = material(2L, 5);
        when(repository.findByIdForUpdate(2L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        var updated = service.updateMaterial(2L, new MaterialRequestDTO("PLA", " Updated ", 9, "kg"));
        assertEquals("PLA", updated.name());
        assertEquals("Updated", updated.description());
        assertEquals(9, updated.quantity());
        assertEquals("kg", updated.unit());

        when(repository.findByIdForUpdate(3L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.updateMaterial(
                3L, new MaterialRequestDTO("PLA", null, 1, "kg")));
    }

    @Test
    void validatesMaterialFieldsAndLimits() {
        assertThrows(IllegalArgumentException.class, () -> service.createMaterial(null));
        assertThrows(IllegalArgumentException.class, () -> service.createMaterial(
                new MaterialRequestDTO(" ", null, 1, "unit")));
        assertThrows(IllegalArgumentException.class, () -> service.createMaterial(
                new MaterialRequestDTO("x".repeat(121), null, 1, "unit")));
        assertThrows(IllegalArgumentException.class, () -> service.createMaterial(
                new MaterialRequestDTO("PLA", "x".repeat(501), 1, "unit")));
        assertThrows(IllegalArgumentException.class, () -> service.createMaterial(
                new MaterialRequestDTO("PLA", null, null, "unit")));
        assertThrows(IllegalArgumentException.class, () -> service.createMaterial(
                new MaterialRequestDTO("PLA", null, -1, "unit")));
        assertThrows(IllegalArgumentException.class, () -> service.createMaterial(
                new MaterialRequestDTO("PLA", null, 1, " ")));
        assertThrows(IllegalArgumentException.class, () -> service.createMaterial(
                new MaterialRequestDTO("PLA", null, 1, "u".repeat(31))));
    }

    private MaterialEntity material(Long id, int quantity) {
        MaterialEntity material = new MaterialEntity();
        material.setId(id);
        material.setName("PLA");
        material.setDescription("Filament");
        material.setQuantity(quantity);
        material.setUnit("rolls");
        return material;
    }
}
