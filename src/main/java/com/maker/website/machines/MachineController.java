package com.maker.website.machines;

import com.maker.website.machines.dtos.CreateMachineDTO;
import com.maker.website.machines.dtos.MachineResponseDTO;
import com.maker.website.machines.dtos.UpdateMachineDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/machines")
@RequiredArgsConstructor
public class MachineController {

    private final MachineService machineService;

    @GetMapping
    public ResponseEntity<List<MachineResponseDTO>> getAllMachines() {
        List<MachineResponseDTO> machines = machineService.getAllMachines();
        return ResponseEntity.ok(machines);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MachineResponseDTO> getMachineById(@PathVariable Long id) {
        MachineResponseDTO machine = machineService.getMachineById(id);
        return ResponseEntity.ok(machine);
    }

    @PostMapping
    public ResponseEntity<MachineResponseDTO> createMachine(@RequestBody @Valid CreateMachineDTO dto, Authentication authentication) {
        MachineResponseDTO createdMachine = machineService.createMachine(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMachine);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MachineResponseDTO> updateMachine(
            @PathVariable Long id,
            @RequestBody @Valid UpdateMachineDTO dto,
            Authentication authentication) {
        MachineResponseDTO updatedMachine = machineService.updateMachine(id, dto);
        return ResponseEntity.ok(updatedMachine);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMachine(@PathVariable Long id, Authentication authentication) {
        machineService.deleteMachine(id);
        return ResponseEntity.noContent().build();
    }

    private Long currentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}