package com.maker.website.machines;

import com.maker.website.machines.dtos.CreateMachineDTO;
import com.maker.website.machines.dtos.MachineResponseDTO;
import com.maker.website.machines.dtos.UpdateMachineDTO;
import com.maker.website.machines.enums.MachineStatusENUM; // Ajuste o pacote do Enum se necessário
import com.maker.website.reports.ReportAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MachineService {

    private final MachineRepository machineRepository;
    private final ReportAuditService reportAuditService;

    public List<MachineResponseDTO> getAllMachines() {
        return machineRepository.findAllByStatusNot(MachineStatusENUM.UNUSABLE)
                .stream().map(MachineResponseDTO::new).toList();
    }

    public MachineResponseDTO getMachineById(Long id) {
        return new MachineResponseDTO(findMachineById(id));
    }

    @Transactional
    public MachineResponseDTO createMachine(CreateMachineDTO dto, Long actorId) {
        validateMachineName(dto.name());
        validateMachineCode(dto.code());

        MachineEntity machine = new MachineEntity();
        machine.setName(dto.name());
        machine.setDescription(dto.description());
        machine.setCode(dto.code());
        machine.setType(dto.type());
        machine.setImage(dto.image());
        machine.setAccess(dto.access());
        machine.setStatus(dto.status() == null ? MachineStatusENUM.AVAILABLE : dto.status());

        MachineEntity savedMachine = machineRepository.save(machine);
        reportAuditService.record("MÁQUINA", "Máquina cadastrada", savedMachine.getName(), actorId,
                "Código: " + savedMachine.getCode() + " · Status: " + savedMachine.getStatus());
        return new MachineResponseDTO(savedMachine);
    }

    @Transactional
    public MachineResponseDTO updateMachine(Long id, UpdateMachineDTO dto, Long actorId) {
        MachineEntity machine = findMachineById(id);
        String oldName = machine.getName();
        MachineStatusENUM oldStatus = machine.getStatus();

        if (dto.name() != null && !dto.name().equals(machine.getName())) {
            validateMachineName(dto.name());
            machine.setName(dto.name());
        }
        if (dto.description() != null) {
            machine.setDescription(dto.description());
        }
        if (dto.status() != null) {
            machine.setStatus(dto.status());
        }
        if (dto.code() != null && !dto.code().equals(machine.getCode())) {
            validateMachineCode(dto.code());
            machine.setCode(dto.code());
        }
        if (dto.type() != null) machine.setType(dto.type());
        if (dto.image() != null) machine.setImage(dto.image());
        if (dto.access() != null) machine.setAccess(dto.access());

        MachineEntity updatedMachine = machineRepository.save(machine);
        reportAuditService.record("MÁQUINA", "Dados atualizados", updatedMachine.getName(), actorId,
                "Nome anterior: " + oldName + " · Status: " + oldStatus + " → " + updatedMachine.getStatus());
        return new MachineResponseDTO(updatedMachine);
    }

    @Transactional
    public void deleteMachine(Long id, Long actorId) {
        MachineEntity machine = findMachineById(id);
        machine.setStatus(MachineStatusENUM.UNUSABLE);
        machineRepository.save(machine);
        reportAuditService.record("MÁQUINA", "Máquina desativada", machine.getName(), actorId,
                "Código: " + machine.getCode());
    }

    public MachineEntity findMachineById(Long id) {
        return machineRepository.findByIdAndStatusNot(id, MachineStatusENUM.UNUSABLE)
                .orElseThrow(() -> new IllegalArgumentException("Machine not found."));
    }

    private void validateMachineName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Machine name is required.");
        }
        if (machineRepository.existsByName(name)) {
            throw new IllegalArgumentException("Machine name is already in use.");
        }
    }

    private void validateMachineCode(String code) {
        if (code != null && !code.isBlank() && machineRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Machine code is already in use.");
        }
    }
}