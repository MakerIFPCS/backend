package com.maker.website.loans;

import com.maker.website.loans.dtos.CreateLoanRequestDTO;
import com.maker.website.loans.dtos.LoanRequestItemDTO;
import com.maker.website.loans.dtos.LoanRequestResponseDTO;
import com.maker.website.loans.dtos.ReviewLoanRequestDTO;
import com.maker.website.machines.MachineEntity;
import com.maker.website.machines.MachineRepository;
import com.maker.website.machines.enums.MachineStatusENUM;
import com.maker.website.materials.MaterialEntity;
import com.maker.website.materials.MaterialRepository;
import com.maker.website.users.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LoanRequestService {

    private final LoanRequestRepository loanRequestRepository;
    private final LoanRequestItemRepository loanRequestItemRepository;
    private final MaterialRepository materialRepository;
    private final MachineRepository machineRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<LoanRequestResponseDTO> getMyRequests(Long requesterId) {
        return loanRequestRepository.findAllByRequesterIdOrderByCreatedAtDesc(requesterId)
                .stream().map(LoanRequestResponseDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public List<LoanRequestResponseDTO> getAllRequests() {
        return loanRequestRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(LoanRequestResponseDTO::new).toList();
    }

    @Transactional
    public LoanRequestResponseDTO createRequest(Long requesterId, CreateLoanRequestDTO request) {
        validateRequest(request);
        var requester = userService.findActiveUserById(requesterId);
        Map<Long, MachineEntity> machines = lockRequestedMachines(request.items());
        Map<Long, MaterialEntity> materials = lockRequestedMaterials(request.items());
        for (LoanRequestItemDTO item : request.items()) {
            if (item.type() == LoanResourceTypeENUM.MATERIAL
                    && materials.get(item.resourceId()).getQuantity() < item.quantity()) {
                throw new IllegalArgumentException(
                        "Insufficient stock for material: " + materials.get(item.resourceId()).getName() + ".");
            }
        }

        LoanRequestEntity entity = new LoanRequestEntity();
        entity.setRequester(requester);
        entity.setPickupDate(request.pickupDate());
        entity.setReturnDate(request.returnDate());
        entity.setReason(request.reason().trim());
        entity.setStatus(LoanRequestStatusENUM.PENDING);

        for (LoanRequestItemDTO requestedItem : request.items()) {
            LoanRequestItemEntity item = new LoanRequestItemEntity();
            item.setType(requestedItem.type());
            item.setResourceId(requestedItem.resourceId());
            item.setQuantity(requestedItem.quantity());
            item.setName(requestedItem.type() == LoanResourceTypeENUM.MACHINE
                    ? machines.get(requestedItem.resourceId()).getName()
                    : materials.get(requestedItem.resourceId()).getName());
            entity.addItem(item);
        }

        return new LoanRequestResponseDTO(loanRequestRepository.save(entity));
    }

    @Transactional
    public LoanRequestResponseDTO reviewRequest(Long id, ReviewLoanRequestDTO review) {
        validateReview(review);
        LoanRequestEntity request = loanRequestRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Loan request not found."));
        if (request.getStatus() != LoanRequestStatusENUM.PENDING) {
            throw new IllegalArgumentException("Only pending loan requests can be reviewed.");
        }

        if (review.status() == LoanRequestStatusENUM.APPROVED) {
            approveRequest(request);
        }
        request.setStatus(review.status());
        request.setAdminNotes(review.adminNotes() == null || review.adminNotes().isBlank()
                ? null
                : review.adminNotes().trim());
        return new LoanRequestResponseDTO(loanRequestRepository.save(request));
    }

    private void approveRequest(LoanRequestEntity request) {
        List<LoanRequestItemDTO> items = request.getItems().stream()
                .map(item -> new LoanRequestItemDTO(item.getType(), item.getResourceId(), item.getQuantity()))
                .toList();
        Map<Long, MachineEntity> machines = lockRequestedMachines(items);
        Map<Long, MaterialEntity> materials = lockRequestedMaterials(items);

        for (LoanRequestItemEntity item : request.getItems()) {
            if (item.getType() == LoanResourceTypeENUM.MACHINE) {
                MachineEntity machine = machines.get(item.getResourceId());
                validateMachineAvailable(machine);
                if (loanRequestItemRepository.countApprovedMachineConflicts(
                        LoanResourceTypeENUM.MACHINE,
                        machine.getId(),
                        LoanRequestStatusENUM.APPROVED,
                        request.getPickupDate(),
                        request.getReturnDate()) > 0) {
                    throw new IllegalArgumentException("Machine is already booked for the requested dates.");
                }
            } else {
                MaterialEntity material = materials.get(item.getResourceId());
                if (material.getQuantity() < item.getQuantity()) {
                    throw new IllegalArgumentException("Insufficient stock for material: " + material.getName() + ".");
                }
            }
        }

        for (LoanRequestItemEntity item : request.getItems()) {
            if (item.getType() == LoanResourceTypeENUM.MATERIAL) {
                MaterialEntity material = materials.get(item.getResourceId());
                material.setQuantity(material.getQuantity() - item.getQuantity());
            }
        }
    }

    private Map<Long, MachineEntity> lockRequestedMachines(List<LoanRequestItemDTO> items) {
        Map<Long, MachineEntity> machines = new HashMap<>();
        items.stream()
                .filter(item -> item.type() == LoanResourceTypeENUM.MACHINE)
                .map(LoanRequestItemDTO::resourceId)
                .distinct()
                .sorted()
                .forEach(id -> {
                    MachineEntity machine = machineRepository.findByIdForUpdate(id)
                            .orElseThrow(() -> new IllegalArgumentException("Machine not found."));
                    validateMachineAvailable(machine);
                    machines.put(id, machine);
                });
        return machines;
    }

    private Map<Long, MaterialEntity> lockRequestedMaterials(List<LoanRequestItemDTO> items) {
        Map<Long, MaterialEntity> materials = new HashMap<>();
        items.stream()
                .filter(item -> item.type() == LoanResourceTypeENUM.MATERIAL)
                .map(LoanRequestItemDTO::resourceId)
                .distinct()
                .sorted()
                .forEach(id -> {
                    MaterialEntity material = materialRepository.findByIdForUpdate(id)
                            .orElseThrow(() -> new IllegalArgumentException("Material not found."));
                    materials.put(id, material);
                });
        return materials;
    }

    private void validateMachineAvailable(MachineEntity machine) {
        if (machine.getStatus() != MachineStatusENUM.AVAILABLE
                && machine.getStatus() != MachineStatusENUM.FUNCTIONAL) {
            throw new IllegalArgumentException("Machine is not available for loan: " + machine.getName() + ".");
        }
    }

    private void validateRequest(CreateLoanRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Loan request details are required.");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Select at least one machine or material.");
        }
        if (request.items().stream().anyMatch(item -> item == null
                || item.type() == null
                || item.resourceId() == null
                || item.resourceId() < 1
                || item.quantity() == null
                || item.quantity() < 1)) {
            throw new IllegalArgumentException("Loan request items are invalid.");
        }
        Set<String> uniqueItems = new HashSet<>();
        for (LoanRequestItemDTO item : request.items()) {
            if (!uniqueItems.add(item.type() + ":" + item.resourceId())) {
                throw new IllegalArgumentException("A resource can only be listed once in a loan request.");
            }
            if (item.type() == LoanResourceTypeENUM.MACHINE && item.quantity() != 1) {
                throw new IllegalArgumentException("Machine quantity must be one.");
            }
        }
        if (request.pickupDate() == null || request.returnDate() == null) {
            throw new IllegalArgumentException("Pickup and return dates are required.");
        }
        if (request.pickupDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Pickup date cannot be in the past.");
        }
        if (request.returnDate().isBefore(request.pickupDate())) {
            throw new IllegalArgumentException("Return date cannot be before pickup date.");
        }
        if (request.reason() == null || request.reason().isBlank()) {
            throw new IllegalArgumentException("Loan purpose is required.");
        }
        if (request.reason().trim().length() > 1000) {
            throw new IllegalArgumentException("Loan purpose must contain at most 1000 characters.");
        }
    }

    private void validateReview(ReviewLoanRequestDTO review) {
        if (review == null
                || review.status() == null
                || review.status() == LoanRequestStatusENUM.PENDING) {
            throw new IllegalArgumentException("A review status of APPROVED or REJECTED is required.");
        }
        if (review.adminNotes() != null && review.adminNotes().trim().length() > 500) {
            throw new IllegalArgumentException("Admin notes must contain at most 500 characters.");
        }
    }
}
