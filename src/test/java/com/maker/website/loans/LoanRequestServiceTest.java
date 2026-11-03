package com.maker.website.loans;

import com.maker.website.loans.dtos.CreateLoanRequestDTO;
import com.maker.website.loans.dtos.LoanRequestItemDTO;
import com.maker.website.loans.dtos.ReviewLoanRequestDTO;
import com.maker.website.machines.MachineEntity;
import com.maker.website.machines.MachineRepository;
import com.maker.website.machines.enums.MachineStatusENUM;
import com.maker.website.materials.MaterialEntity;
import com.maker.website.materials.MaterialRepository;
import com.maker.website.users.UserEntity;
import com.maker.website.users.UserService;
import com.maker.website.users.enums.UserRoleENUM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanRequestServiceTest {

    @Mock LoanRequestRepository loanRequestRepository;
    @Mock LoanRequestItemRepository loanRequestItemRepository;
    @Mock MaterialRepository materialRepository;
    @Mock MachineRepository machineRepository;
    @Mock UserService userService;
    @InjectMocks LoanRequestService service;

    private UserEntity requester;
    private MaterialEntity material;
    private MachineEntity machine;
    private LocalDate pickup;
    private LocalDate returned;

    @BeforeEach
    void setUp() {
        requester = new UserEntity();
        requester.setId(10L);
        requester.setName("Borrower");
        requester.setEmail("borrower@example.com");
        requester.setRole(UserRoleENUM.USER);
        material = new MaterialEntity();
        material.setId(20L);
        material.setName("PLA");
        material.setQuantity(10);
        material.setUnit("rolls");
        machine = new MachineEntity();
        machine.setId(30L);
        machine.setName("Printer");
        machine.setStatus(MachineStatusENUM.AVAILABLE);
        pickup = LocalDate.now().plusDays(2);
        returned = pickup.plusDays(2);
    }

    @Test
    void listsRequestsForUserAndAdmin() {
        LoanRequestEntity request = request(1L, LoanRequestStatusENUM.PENDING);
        when(loanRequestRepository.findAllByRequesterIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(request));
        when(loanRequestRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(request));

        assertEquals(1, service.getMyRequests(10L).size());
        assertEquals(1, service.getAllRequests().size());
    }

    @Test
    void createsPendingRequestWithMachineAndMaterialItems() {
        when(userService.findActiveUserById(10L)).thenReturn(requester);
        when(machineRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(machine));
        when(materialRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(material));
        when(loanRequestRepository.save(any(LoanRequestEntity.class))).thenAnswer(invocation -> {
            LoanRequestEntity saved = invocation.getArgument(0);
            saved.setId(5L);
            saved.setCreatedAt(LocalDateTime.now());
            return saved;
        });

        var created = service.createRequest(10L, requestBody(
                List.of(new LoanRequestItemDTO(LoanResourceTypeENUM.MACHINE, 30L, 1),
                        new LoanRequestItemDTO(LoanResourceTypeENUM.MATERIAL, 20L, 3))));

        assertEquals(LoanRequestStatusENUM.PENDING, created.status());
        assertEquals("Borrower", created.requesterName());
        assertEquals(2, created.items().size());
        assertEquals("PLA", created.items().get(1).name());
        assertEquals(10, material.getQuantity());
    }

    @Test
    void rejectsInvalidRequestsAndUnavailableResources() {
        assertInvalid(null);
        assertInvalid(new CreateLoanRequestDTO(List.of(), pickup, returned, "reason"));
        assertInvalid(new CreateLoanRequestDTO(List.of(new LoanRequestItemDTO(null, 1L, 1)), pickup, returned, "reason"));
        assertInvalid(new CreateLoanRequestDTO(List.of(new LoanRequestItemDTO(LoanResourceTypeENUM.MATERIAL, null, 1)), pickup, returned, "reason"));
        assertInvalid(new CreateLoanRequestDTO(List.of(new LoanRequestItemDTO(LoanResourceTypeENUM.MATERIAL, 1L, 0)), pickup, returned, "reason"));
        assertInvalid(new CreateLoanRequestDTO(List.of(
                new LoanRequestItemDTO(LoanResourceTypeENUM.MATERIAL, 1L, 1),
                new LoanRequestItemDTO(LoanResourceTypeENUM.MATERIAL, 1L, 1)), pickup, returned, "reason"));
        assertInvalid(new CreateLoanRequestDTO(List.of(new LoanRequestItemDTO(LoanResourceTypeENUM.MACHINE, 1L, 2)), pickup, returned, "reason"));
        assertInvalid(new CreateLoanRequestDTO(List.of(new LoanRequestItemDTO(LoanResourceTypeENUM.MATERIAL, 1L, 1)), null, returned, "reason"));
        assertInvalid(new CreateLoanRequestDTO(List.of(new LoanRequestItemDTO(LoanResourceTypeENUM.MATERIAL, 1L, 1)), pickup, null, "reason"));
        assertInvalid(new CreateLoanRequestDTO(List.of(new LoanRequestItemDTO(LoanResourceTypeENUM.MATERIAL, 1L, 1)), LocalDate.now().minusDays(1), returned, "reason"));
        assertInvalid(new CreateLoanRequestDTO(List.of(new LoanRequestItemDTO(LoanResourceTypeENUM.MATERIAL, 1L, 1)), returned, pickup, "reason"));
        assertInvalid(new CreateLoanRequestDTO(List.of(new LoanRequestItemDTO(LoanResourceTypeENUM.MATERIAL, 1L, 1)), pickup, returned, " "));
        assertInvalid(new CreateLoanRequestDTO(List.of(new LoanRequestItemDTO(LoanResourceTypeENUM.MATERIAL, 1L, 1)), pickup, returned, "x".repeat(1001)));

        when(userService.findActiveUserById(10L)).thenReturn(requester);
        when(materialRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.createRequest(10L, requestBody(
                List.of(new LoanRequestItemDTO(LoanResourceTypeENUM.MATERIAL, 99L, 1)))));
        when(machineRepository.findByIdForUpdate(31L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.createRequest(10L, requestBody(
                List.of(new LoanRequestItemDTO(LoanResourceTypeENUM.MACHINE, 31L, 1)))));
        machine.setStatus(MachineStatusENUM.MAINTENANCE);
        when(machineRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(machine));
        assertThrows(IllegalArgumentException.class, () -> service.createRequest(10L, requestBody(
                List.of(new LoanRequestItemDTO(LoanResourceTypeENUM.MACHINE, 30L, 1)))));
    }

    @Test
    void rejectsInsufficientMaterialStockAtSubmission() {
        when(userService.findActiveUserById(10L)).thenReturn(requester);
        when(materialRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(material));
        assertThrows(IllegalArgumentException.class, () -> service.createRequest(10L, requestBody(
                List.of(new LoanRequestItemDTO(LoanResourceTypeENUM.MATERIAL, 20L, 11)))));
    }

    @Test
    void approvesRequestAndDeductsMaterialStock() {
        LoanRequestEntity request = request(1L, LoanRequestStatusENUM.PENDING);
        request.addItem(item(LoanResourceTypeENUM.MATERIAL, 20L, 3, "PLA"));
        when(loanRequestRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(request));
        when(materialRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(material));
        when(loanRequestRepository.save(request)).thenReturn(request);

        var reviewed = service.reviewRequest(1L, new ReviewLoanRequestDTO(LoanRequestStatusENUM.APPROVED, " approved "));

        assertEquals(LoanRequestStatusENUM.APPROVED, reviewed.status());
        assertEquals("approved", reviewed.adminNotes());
        assertEquals(7, material.getQuantity());
    }

    @Test
    void rejectsRequestWithoutChangingStockAndRejectsRepeatedReview() {
        LoanRequestEntity request = request(1L, LoanRequestStatusENUM.PENDING);
        request.addItem(item(LoanResourceTypeENUM.MATERIAL, 20L, 3, "PLA"));
        when(loanRequestRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(request));
        when(loanRequestRepository.save(request)).thenReturn(request);

        var reviewed = service.reviewRequest(1L, new ReviewLoanRequestDTO(LoanRequestStatusENUM.REJECTED, null));
        assertEquals(LoanRequestStatusENUM.REJECTED, reviewed.status());
        assertEquals(10, material.getQuantity());
        assertThrows(IllegalArgumentException.class, () -> service.reviewRequest(1L,
                new ReviewLoanRequestDTO(LoanRequestStatusENUM.APPROVED, null)));
        when(loanRequestRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.reviewRequest(99L,
                new ReviewLoanRequestDTO(LoanRequestStatusENUM.REJECTED, null)));
    }

    @Test
    void validatesStockAndMachineConflictsOnApproval() {
        LoanRequestEntity materialRequest = request(1L, LoanRequestStatusENUM.PENDING);
        materialRequest.addItem(item(LoanResourceTypeENUM.MATERIAL, 20L, 11, "PLA"));
        when(loanRequestRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(materialRequest));
        when(materialRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(material));
        assertThrows(IllegalArgumentException.class, () -> service.reviewRequest(1L,
                new ReviewLoanRequestDTO(LoanRequestStatusENUM.APPROVED, null)));

        LoanRequestEntity machineRequest = request(2L, LoanRequestStatusENUM.PENDING);
        machineRequest.addItem(item(LoanResourceTypeENUM.MACHINE, 30L, 1, "Printer"));
        when(loanRequestRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(machineRequest));
        when(machineRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(machine));
        when(loanRequestItemRepository.countApprovedMachineConflicts(
                eq(LoanResourceTypeENUM.MACHINE), eq(30L), eq(LoanRequestStatusENUM.APPROVED),
                eq(pickup), eq(returned))).thenReturn(1L);
        assertThrows(IllegalArgumentException.class, () -> service.reviewRequest(2L,
                new ReviewLoanRequestDTO(LoanRequestStatusENUM.APPROVED, null)));

        machine.setStatus(MachineStatusENUM.UNUSABLE);
        when(loanRequestRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(machineRequest));
        assertThrows(IllegalArgumentException.class, () -> service.reviewRequest(3L,
                new ReviewLoanRequestDTO(LoanRequestStatusENUM.APPROVED, null)));
    }

    @Test
    void rejectsInvalidReviewData() {
        assertThrows(IllegalArgumentException.class, () -> service.reviewRequest(1L, null));
        assertThrows(IllegalArgumentException.class, () -> service.reviewRequest(1L,
                new ReviewLoanRequestDTO(LoanRequestStatusENUM.PENDING, null)));
        assertThrows(IllegalArgumentException.class, () -> service.reviewRequest(1L,
                new ReviewLoanRequestDTO(LoanRequestStatusENUM.REJECTED, "x".repeat(501))));
    }

    private void assertInvalid(CreateLoanRequestDTO request) {
        assertThrows(IllegalArgumentException.class, () -> service.createRequest(10L, request));
    }

    private CreateLoanRequestDTO requestBody(List<LoanRequestItemDTO> items) {
        return new CreateLoanRequestDTO(items, pickup, returned, "Research project");
    }

    private LoanRequestEntity request(Long id, LoanRequestStatusENUM status) {
        LoanRequestEntity request = new LoanRequestEntity();
        request.setId(id);
        request.setRequester(requester);
        request.setPickupDate(pickup);
        request.setReturnDate(returned);
        request.setReason("Research project");
        request.setStatus(status);
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        return request;
    }

    private LoanRequestItemEntity item(LoanResourceTypeENUM type, Long resourceId, int quantity, String name) {
        LoanRequestItemEntity item = new LoanRequestItemEntity();
        item.setType(type);
        item.setResourceId(resourceId);
        item.setQuantity(quantity);
        item.setName(name);
        return item;
    }
}
