package com.maker.website.orders;

import com.maker.website.machines.MachineEntity;
import com.maker.website.machines.MachineRepository;
import com.maker.website.machines.enums.MachineStatusENUM;
import com.maker.website.orders.dtos.ReviewProductionOrderDTO;
import com.maker.website.users.UserEntity;
import com.maker.website.users.UserService;
import com.maker.website.users.enums.UserRoleENUM;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.access.AccessDeniedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductionOrderServiceTest {

    @Mock ProductionOrderRepository productionOrderRepository;
    @Mock MachineRepository machineRepository;
    @Mock UserService userService;
    @InjectMocks ProductionOrderService productionOrderService;

    @Test
    void createsLaserOrderAndStoresArbitraryFileBytes() throws Exception {
        UserEntity requester = user();
        when(userService.findActiveUserById(12L)).thenReturn(requester);
        when(productionOrderRepository.save(any(ProductionOrderEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        MockMultipartFile file = new MockMultipartFile(
                "file", "part.custom-extension", "application/x-unlisted-format", new byte[]{1, 2, 3});

        var result = productionOrderService.createOrder(
                12L, ProductionOrderTypeENUM.LASER_CUTTING, null, "Please check dimensions.", "100 x 100 mm", file);

        assertEquals(ProductionOrderTypeENUM.LASER_CUTTING, result.type());
        assertEquals("part.custom-extension", result.fileName());
        assertEquals("100 x 100 mm", result.dimensions());
        assertEquals(3, result.fileSize());
        assertEquals(ProductionOrderStatusENUM.PENDING, result.status());
        verify(productionOrderRepository).save(argThat(order ->
                order.getFileData().length == 3
                        && order.getFileData()[0] == 1
                        && order.getContentType().equals("application/x-unlisted-format")));
        verifyNoInteractions(machineRepository);
    }

    @Test
    void createsPrintingOrderForAvailablePrinterWithUnaccentedSeedType() throws Exception {
        UserEntity requester = user();
        MachineEntity printer = new MachineEntity();
        printer.setId(5L);
        printer.setName("Printer");
        printer.setType("Impressao 3D");
        printer.setStatus(MachineStatusENUM.AVAILABLE);
        when(userService.findActiveUserById(12L)).thenReturn(requester);
        when(machineRepository.findByIdAndStatusNot(5L, MachineStatusENUM.UNUSABLE))
                .thenReturn(Optional.of(printer));
        when(productionOrderRepository.save(any(ProductionOrderEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = productionOrderService.createOrder(
                12L, ProductionOrderTypeENUM.THREE_D_PRINTING, 5L, null, null,
                new MockMultipartFile("file", "model.stl", "model/stl", new byte[]{4}));

        assertEquals(5L, result.machineId());
        assertEquals("Printer", result.machineName());
        assertEquals("", result.notes());
    }

    @Test
    void createsRequestsWithoutFileAndValidatesMachineAndDetails() throws Exception {
        when(userService.findActiveUserById(12L)).thenReturn(user());
        when(productionOrderRepository.save(any(ProductionOrderEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        var noFileOrder = productionOrderService.createOrder(
                12L, ProductionOrderTypeENUM.LASER_CUTTING, null, null, null, null);
        assertNull(noFileOrder.fileName());
        assertEquals(0, noFileOrder.fileSize());
        assertNotNull(noFileOrder.comments());
        assertTrue(noFileOrder.comments().isEmpty());

        assertThrows(IllegalArgumentException.class, () -> productionOrderService.createOrder(
                12L, null, null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> productionOrderService.createOrder(
                12L, ProductionOrderTypeENUM.THREE_D_PRINTING, null, null, null,
                new MockMultipartFile("file", "model.stl", "model/stl", new byte[]{1})));
        assertThrows(IllegalArgumentException.class, () -> productionOrderService.createOrder(
                12L, ProductionOrderTypeENUM.LASER_CUTTING, null, "x".repeat(2001), null,
                new MockMultipartFile("file", "model.stl", "model/stl", new byte[]{1})));
        verify(machineRepository, never()).findByIdAndStatusNot(anyLong(), any());
    }

    @Test
    void acceptsFilesUpTo100MbAndRejectsLargerFiles() throws Exception {
        MultipartFile maxSizeFile = mockFile(100L * 1024 * 1024);
        when(userService.findActiveUserById(12L)).thenReturn(user());
        when(productionOrderRepository.save(any(ProductionOrderEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = productionOrderService.createOrder(
                12L, ProductionOrderTypeENUM.LASER_CUTTING, null, null, null, maxSizeFile);

        assertEquals(100L * 1024 * 1024, result.fileSize());
        verify(productionOrderRepository).save(any(ProductionOrderEntity.class));
        MultipartFile oversizedFile = mock(MultipartFile.class);
        when(oversizedFile.isEmpty()).thenReturn(false);
        when(oversizedFile.getSize()).thenReturn(100L * 1024 * 1024 + 1);
        assertThrows(IllegalArgumentException.class, () -> productionOrderService.createOrder(
                12L, ProductionOrderTypeENUM.LASER_CUTTING, null, null, null,
                oversizedFile));
    }

    @Test
    void rejectsUnavailableOrNonPrinterMachines() {
        MachineEntity unavailablePrinter = new MachineEntity();
        unavailablePrinter.setType("Impressão 3D");
        unavailablePrinter.setStatus(MachineStatusENUM.MAINTENANCE);
        when(machineRepository.findByIdAndStatusNot(5L, MachineStatusENUM.UNUSABLE))
                .thenReturn(Optional.of(unavailablePrinter));

        assertThrows(IllegalArgumentException.class, () -> productionOrderService.createOrder(
                12L, ProductionOrderTypeENUM.THREE_D_PRINTING, 5L, null, null,
                new MockMultipartFile("file", "model.stl", "model/stl", new byte[]{1})));

        unavailablePrinter.setStatus(MachineStatusENUM.AVAILABLE);
        unavailablePrinter.setType("Corte a Laser");
        assertThrows(IllegalArgumentException.class, () -> productionOrderService.createOrder(
                12L, ProductionOrderTypeENUM.THREE_D_PRINTING, 5L, null, null,
                new MockMultipartFile("file", "model.stl", "model/stl", new byte[]{1})));
        verifyNoInteractions(productionOrderRepository);
    }

    @Test
    void adminsCanRequestUpdatesAndReopenOrders() {
        ProductionOrderEntity order = order();
        UserEntity admin = user();
        admin.setId(99L);
        admin.setRole(UserRoleENUM.ADMIN);
        when(productionOrderRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(order));
        when(productionOrderRepository.save(order)).thenReturn(order);
        when(userService.findActiveUserById(99L)).thenReturn(admin);

        var result = productionOrderService.reviewOrder(
                9L, new ReviewProductionOrderDTO(ProductionOrderStatusENUM.WAITING_FOR_UPDATE, "Please attach the model file."), 99L);
        assertEquals(ProductionOrderStatusENUM.WAITING_FOR_UPDATE, result.status());
        assertEquals(1, result.comments().size());

        var reopened = productionOrderService.reviewOrder(
                9L, new ReviewProductionOrderDTO(ProductionOrderStatusENUM.PENDING, null), 99L);
        assertEquals(ProductionOrderStatusENUM.PENDING, reopened.status());
        assertThrows(IllegalArgumentException.class, () -> productionOrderService.reviewOrder(
                9L, new ReviewProductionOrderDTO(ProductionOrderStatusENUM.REJECTED, null), 99L));
    }

    @Test
    void adminsCanSetAndClearEstimatedCompletionFor3DPrintingOrders() {
        ProductionOrderEntity order = order();
        order.setType(ProductionOrderTypeENUM.THREE_D_PRINTING);
        UserEntity admin = user();
        admin.setId(99L);
        admin.setRole(UserRoleENUM.ADMIN);
        LocalDateTime estimatedCompletionAt = LocalDateTime.of(2026, 11, 12, 16, 30);
        when(userService.findActiveUserById(99L)).thenReturn(admin);
        when(productionOrderRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(order));
        when(productionOrderRepository.save(order)).thenReturn(order);

        var scheduled = productionOrderService.updateEstimatedCompletion(9L, estimatedCompletionAt, 99L);
        assertEquals(estimatedCompletionAt, scheduled.estimatedCompletionAt());

        var cleared = productionOrderService.clearEstimatedCompletion(9L, 99L);
        assertNull(cleared.estimatedCompletionAt());
    }

    @Test
    void onlyAdminsCanSchedule3DPrintingOrders() {
        ProductionOrderEntity order = order();
        order.setType(ProductionOrderTypeENUM.THREE_D_PRINTING);
        when(userService.findActiveUserById(12L)).thenReturn(user());

        assertThrows(AccessDeniedException.class, () -> productionOrderService.updateEstimatedCompletion(
                9L, LocalDateTime.of(2026, 11, 12, 16, 30), 12L));
        verify(productionOrderRepository, never()).findByIdForUpdate(anyLong());
    }

    @Test
    void estimatedCompletionIsOnlyAvailableFor3DPrintingOrders() {
        ProductionOrderEntity order = order();
        UserEntity admin = user();
        admin.setRole(UserRoleENUM.ADMIN);
        when(userService.findActiveUserById(12L)).thenReturn(admin);
        when(productionOrderRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> productionOrderService.updateEstimatedCompletion(
                9L, LocalDateTime.of(2026, 11, 12, 16, 30), 12L));
        verify(productionOrderRepository, never()).save(any(ProductionOrderEntity.class));
    }

    @Test
    void userCanUpdateWaitingRequestAndCompleteItWithoutReplacingFile() throws Exception {
        ProductionOrderEntity order = order();
        order.setStatus(ProductionOrderStatusENUM.WAITING_FOR_UPDATE);
        order.setAdminNotes("Please add dimensions.");
        when(productionOrderRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(order));
        when(productionOrderRepository.save(order)).thenReturn(order);

        var result = productionOrderService.updateOrder(
                9L, 12L, ProductionOrderTypeENUM.LASER_CUTTING, null, "Updated notes.",
                "50 x 40 mm", null);

        assertEquals(ProductionOrderStatusENUM.PENDING, result.status());
        assertEquals("50 x 40 mm", result.dimensions());
        assertEquals("model.stl", result.fileName());
        assertNull(result.adminNotes());
    }

    @Test
    void onlyTheRequesterCanEditAnOpenOrder() {
        ProductionOrderEntity order = order();
        when(productionOrderRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class, () -> productionOrderService.updateOrder(
                9L, 99L, ProductionOrderTypeENUM.LASER_CUTTING, null, "", "", null));

        order.setStatus(ProductionOrderStatusENUM.APPROVED);
        assertThrows(IllegalArgumentException.class, () -> productionOrderService.updateOrder(
                9L, 12L, ProductionOrderTypeENUM.LASER_CUTTING, null, "", "", null));
        verify(productionOrderRepository, never()).save(any(ProductionOrderEntity.class));
    }

    @Test
    void validatesCncMachineTypeAndAllowsAvailableCnc() throws Exception {
        MachineEntity cnc = new MachineEntity();
        cnc.setId(8L);
        cnc.setName("Router CNC");
        cnc.setType("CNC");
        cnc.setStatus(MachineStatusENUM.FUNCTIONAL);
        when(userService.findActiveUserById(12L)).thenReturn(user());
        when(machineRepository.findByIdAndStatusNot(8L, MachineStatusENUM.UNUSABLE))
                .thenReturn(Optional.of(cnc));
        when(productionOrderRepository.save(any(ProductionOrderEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = productionOrderService.createOrder(
                12L, ProductionOrderTypeENUM.CNC_MACHINING, 8L, null, null, null);

        assertEquals("Router CNC", result.machineName());
        assertEquals(ProductionOrderTypeENUM.CNC_MACHINING, result.type());
    }

    @Test
    void returnsFilesOnlyToTheRequesterOrAdministrator() {
        ProductionOrderEntity order = order();
        order.setFileName("model.stl");
        order.setFileData(new byte[]{7});
        when(productionOrderRepository.findById(9L)).thenReturn(Optional.of(order));

        assertArrayEquals(new byte[]{7}, productionOrderService.getFile(9L, 12L, false).fileData());
        assertArrayEquals(new byte[]{7}, productionOrderService.getFile(9L, 99L, true).fileData());
        assertThrows(AccessDeniedException.class, () -> productionOrderService.getFile(9L, 99L, false));
    }

    @Test
    void listsRequestsForUserAndAdministrator() {
        ProductionOrderEntity order = order();
        when(productionOrderRepository.findAllByRequesterIdOrderByCreatedAtDesc(12L)).thenReturn(List.of(order));
        when(productionOrderRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(order));

        assertEquals(1, productionOrderService.getMyOrders(12L).size());
        assertEquals(1, productionOrderService.getAllOrders().size());
    }

    private UserEntity user() {
        UserEntity user = new UserEntity();
        user.setId(12L);
        user.setName("Request User");
        user.setEmail("user@example.test");
        user.setRole(UserRoleENUM.USER);
        return user;
    }

    private ProductionOrderEntity order() {
        ProductionOrderEntity order = new ProductionOrderEntity();
        order.setId(9L);
        order.setRequester(user());
        order.setType(ProductionOrderTypeENUM.LASER_CUTTING);
        order.setFileName("model.stl");
        order.setContentType("application/octet-stream");
        order.setFileData(new byte[]{1});
        order.setFileSize(1);
        order.setNotes("");
        order.setDimensions("");
        order.setStatus(ProductionOrderStatusENUM.PENDING);
        return order;
    }

    private MultipartFile mockFile(long size) throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(size);
        when(file.getOriginalFilename()).thenReturn("file.any-format");
        when(file.getContentType()).thenReturn("application/x-arbitrary");
        when(file.getBytes()).thenReturn(new byte[]{1});
        return file;
    }
}
