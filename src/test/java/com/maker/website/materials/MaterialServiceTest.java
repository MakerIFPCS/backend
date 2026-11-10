package com.maker.website.materials;

import com.maker.website.materials.dtos.CreateMaterialDTO;
import com.maker.website.reports.ReportAuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialServiceTest {

    @Mock MaterialRepository repository;
    @Mock ReportAuditService auditService;
    @InjectMocks MaterialService service;

    @Test
    void createsAndUpdatesMaterial() {
        when(repository.save(any())).thenAnswer(invocation -> {
            MaterialEntity material = invocation.getArgument(0);
            material.setId(1L);
            return material;
        });
        var created = service.createMaterial(new CreateMaterialDTO("  PLA  ", null, 4, " g "), 2L);
        assertEquals("PLA", created.name());
        assertEquals("g", created.unit());

        MaterialEntity material = new MaterialEntity();
        material.setName("PLA");
        material.setDescription("old");
        material.setQuantity(4);
        material.setUnit("g");
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(material));
        var updated = service.updateMaterial(1L, new CreateMaterialDTO("ABS", " new ", 5, "kg"), 2L);
        assertEquals("ABS", updated.name());
        assertEquals("new", updated.description());
        assertEquals(5, updated.quantity());
    }

    @Test
    void validatesMaterialFields() {
        assertThrows(IllegalArgumentException.class, () -> service.createMaterial(null, 1L));
        assertThrows(IllegalArgumentException.class, () -> service.createMaterial(
                new CreateMaterialDTO("", "", 1, "u"), 1L));
        assertThrows(IllegalArgumentException.class, () -> service.createMaterial(
                new CreateMaterialDTO("x", "", -1, "u"), 1L));
        assertThrows(IllegalArgumentException.class, () -> service.createMaterial(
                new CreateMaterialDTO("x", "", 1, ""), 1L));
        assertThrows(IllegalArgumentException.class, () -> service.createMaterial(
                new CreateMaterialDTO("x", "a".repeat(501), 1, "u"), 1L));
        when(repository.findByIdForUpdate(4L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.updateMaterial(
                4L, new CreateMaterialDTO("x", "", 1, "u"), 1L));
    }
}
