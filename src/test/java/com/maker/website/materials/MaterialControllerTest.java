package com.maker.website.materials;

import com.maker.website.materials.dtos.MaterialRequestDTO;
import com.maker.website.materials.dtos.MaterialResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialControllerTest {

    @Mock MaterialService service;
    @InjectMocks MaterialController controller;

    @Test
    void delegatesMaterialEndpoints() {
        MaterialResponseDTO response = mock(MaterialResponseDTO.class);
        MaterialRequestDTO request = new MaterialRequestDTO("PLA", null, 2, "rolls");
        when(service.getAllMaterials()).thenReturn(List.of(response));
        when(service.createMaterial(request)).thenReturn(response);
        when(service.updateMaterial(4L, request)).thenReturn(response);

        assertEquals(1, controller.getAllMaterials().getBody().size());
        assertEquals(201, controller.createMaterial(request).getStatusCode().value());
        assertEquals(response, controller.updateMaterial(4L, request).getBody());
    }
}
