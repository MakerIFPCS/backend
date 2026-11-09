package com.maker.website.loans;

import com.maker.website.loans.dtos.CreateLoanRequestDTO;
import com.maker.website.loans.dtos.LoanRequestItemDTO;
import com.maker.website.loans.dtos.LoanRequestResponseDTO;
import com.maker.website.loans.dtos.UpdateLoanRequestStatusDTO;
import com.maker.website.machines.MachineEntity;
import com.maker.website.machines.MachineRepository;
import com.maker.website.machines.enums.MachineStatusENUM;
import com.maker.website.materials.MaterialEntity;
import com.maker.website.materials.MaterialRepository;
import com.maker.website.reports.ReportAuditService;
import com.maker.website.users.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LoanRequestService {

    private static final int MAX_REASON_LENGTH = 1000;
    private static final int MAX_ADMIN_NOTES_LENGTH = 500;

    private final LoanRequestRepository loanRequestRepository;
    private final MachineRepository machineRepository;
    private final MaterialRepository materialRepository;
    private final UserService userService;
    private final ReportAuditService reportAuditService;

    @Transactional
    public LoanRequestResponseDTO createRequest(Long requesterId, CreateLoanRequestDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Loan request data is required.");
        if (dto.items() == null || dto.items().isEmpty()) {
            throw new IllegalArgumentException("Select at least one machine or material.");
        }
        if (dto.pickupDate() == null || dto.returnDate() == null) {
            throw new IllegalArgumentException("Pickup and return dates are required.");
        }
        if (dto.pickupDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Pickup date cannot be in the past.");
        }
        if (dto.returnDate().isBefore(dto.pickupDate())) {
            throw new IllegalArgumentException("Return date cannot be before pickup date.");
        }
        if (dto.reason() == null || dto.reason().isBlank()) {
            throw new IllegalArgumentException("A reason for the loan is required.");
        }
        if (dto.reason().trim().length() > MAX_REASON_LENGTH) {
            throw new IllegalArgumentException("The reason must contain at most 1000 characters.");
        }

        LoanRequestEntity request = new LoanRequestEntity();
        request.setRequester(userService.findActiveUserById(requesterId));
        request.setPickupDate(dto.pickupDate());
        request.setReturnDate(dto.returnDate());
        request.setReason(dto.reason().trim());
        request.setStatus(LoanRequestStatusENUM.PENDING);

        Set<String> selectedItems = new HashSet<>();
        for (LoanRequestItemDTO requestedItem : dto.items()) {
            if (requestedItem == null || requestedItem.type() == null || requestedItem.resourceId() == null) {
                throw new IllegalArgumentException("Each loan item must specify its type and resource.");
            }
            if (requestedItem.quantity() == null || requestedItem.quantity() < 1) {
                throw new IllegalArgumentException("Item quantities must be at least one.");
            }

            String itemKey = requestedItem.type() + ":" + requestedItem.resourceId();
            if (!selectedItems.add(itemKey)) {
                throw new IllegalArgumentException("An item cannot appear more than once in the same request.");
            }

            LoanRequestItemEntity item = new LoanRequestItemEntity();
            item.setType(requestedItem.type());
            item.setQuantity(requestedItem.quantity());
            switch (requestedItem.type()) {
                case MACHINE -> {
                    if (requestedItem.quantity() != 1) {
                        throw new IllegalArgumentException("Machine quantity must be one.");
                    }
                    MachineEntity machine = machineRepository.findById(requestedItem.resourceId())
                            .orElseThrow(() -> new IllegalArgumentException("Machine not found."));
                    requireMachineAvailable(machine);
                    item.setMachine(machine);
                }
                case MATERIAL -> {
                    MaterialEntity material = materialRepository.findById(requestedItem.resourceId())
                            .orElseThrow(() -> new IllegalArgumentException("Material not found."));
                    if (requestedItem.quantity() > material.getQuantity()) {
                        throw new IllegalArgumentException("Requested material quantity exceeds current stock.");
                    }
                    item.setMaterial(material);
                }
            }
            request.addItem(item);
        }

        LoanRequestEntity savedRequest = loanRequestRepository.save(request);
        reportAuditService.record("EMPRÉSTIMO", "Pedido de empréstimo criado",
                "Pedido #" + savedRequest.getId(), requesterId,
                "Retirada: " + savedRequest.getPickupDate()
                        + " · Devolução: " + savedRequest.getReturnDate()
                        + " · Itens: " + savedRequest.getItems().stream()
                        .map(item -> (item.getType() == LoanResourceTypeENUM.MACHINE
                                ? item.getMachine().getName()
                                : item.getMaterial().getName())
                                + (item.getType() == LoanResourceTypeENUM.MATERIAL
                                ? " (quantidade: " + item.getQuantity() + ")" : ""))
                        .reduce((first, second) -> first + ", " + second)
                        .orElse("nenhum"));
        return new LoanRequestResponseDTO(savedRequest);
    }

    @Transactional(readOnly = true)
    public List<LoanRequestResponseDTO> getMyRequests(Long requesterId) {
        userService.findActiveUserById(requesterId);
        return loanRequestRepository.findByRequester_IdOrderByCreatedAtDesc(requesterId)
                .stream().map(LoanRequestResponseDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public List<LoanRequestResponseDTO> getAllRequests() {
        return loanRequestRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(LoanRequestResponseDTO::new).toList();
    }

    @Transactional
    public LoanRequestResponseDTO updateStatus(Long requestId, UpdateLoanRequestStatusDTO dto, Long actorId) {
        if (dto == null || dto.status() == null || dto.status() == LoanRequestStatusENUM.PENDING) {
            throw new IllegalArgumentException("Choose APPROVED or REJECTED for the request.");
        }
        if (dto.adminNotes() != null && dto.adminNotes().trim().length() > MAX_ADMIN_NOTES_LENGTH) {
            throw new IllegalArgumentException("Admin notes must contain at most 500 characters.");
        }

        LoanRequestEntity request = loanRequestRepository.findWithDetailsById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Loan request not found."));
        if (request.getStatus() != LoanRequestStatusENUM.PENDING) {
            throw new IllegalArgumentException("Only pending requests can be reviewed.");
        }

        if (dto.status() == LoanRequestStatusENUM.APPROVED) {
            approveRequest(request, actorId);
        }
        request.setStatus(dto.status());
        request.setAdminNotes(dto.adminNotes() == null || dto.adminNotes().isBlank()
                ? null
                : dto.adminNotes().trim());

        LoanRequestEntity updatedRequest = loanRequestRepository.save(request);
        reportAuditService.record("EMPRÉSTIMO", dto.status() == LoanRequestStatusENUM.APPROVED
                        ? "Pedido de empréstimo aprovado" : "Pedido de empréstimo recusado",
                "Pedido #" + updatedRequest.getId(), actorId,
                "Solicitante: " + updatedRequest.getRequester().getName()
                        + " · Observação: " + (updatedRequest.getAdminNotes() == null ? "nenhuma" : updatedRequest.getAdminNotes()));
        return new LoanRequestResponseDTO(updatedRequest);
    }

    private void approveRequest(LoanRequestEntity request, Long actorId) {
        List<LoanRequestItemEntity> orderedItems = request.getItems().stream()
                .sorted(Comparator.comparing(LoanRequestItemEntity::getType)
                        .thenComparing(item -> item.getType() == LoanResourceTypeENUM.MACHINE
                                ? item.getMachine().getId()
                                : item.getMaterial().getId()))
                .toList();

        for (LoanRequestItemEntity item : orderedItems) {
            if (item.getType() == LoanResourceTypeENUM.MACHINE) {
                MachineEntity machine = machineRepository.findByIdForUpdate(item.getMachine().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Machine not found."));
                requireMachineAvailable(machine);
                List<LoanRequestEntity> overlappingRequests =
                        loanRequestRepository.findAllByStatusAndPickupDateLessThanEqualAndReturnDateGreaterThanEqual(
                                LoanRequestStatusENUM.APPROVED,
                                request.getReturnDate(),
                                request.getPickupDate()
                        );
                boolean alreadyLoaned = overlappingRequests.stream()
                        .flatMap(existing -> existing.getItems().stream())
                        .anyMatch(existingItem -> existingItem.getMachine() != null
                                && existingItem.getMachine().getId().equals(machine.getId()));
                if (alreadyLoaned) {
                    throw new IllegalArgumentException("Machine is already approved for another loan during these dates.");
                }
            } else {
                MaterialEntity material = materialRepository.findByIdForUpdate(item.getMaterial().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Material not found."));
                if (item.getQuantity() > material.getQuantity()) {
                    throw new IllegalArgumentException("Not enough material remains in stock to approve this request.");
                }
                int previousQuantity = material.getQuantity();
                material.setQuantity(previousQuantity - item.getQuantity());
                MaterialEntity updatedMaterial = materialRepository.save(material);
                reportAuditService.record("MATERIAL", "Estoque descontado por empréstimo",
                        updatedMaterial.getName(), actorId,
                        "Pedido #" + request.getId() + " · Quantidade: " + previousQuantity
                                + " → " + updatedMaterial.getQuantity() + " " + updatedMaterial.getUnit());
            }
        }
    }

    private void requireMachineAvailable(MachineEntity machine) {
        if (machine.getStatus() != MachineStatusENUM.AVAILABLE
                && machine.getStatus() != MachineStatusENUM.FUNCTIONAL) {
            throw new IllegalArgumentException("Machine is not available for loan.");
        }
    }
}
