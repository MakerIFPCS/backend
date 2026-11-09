package com.maker.website.orders;

import com.maker.website.machines.MachineEntity;
import com.maker.website.machines.MachineRepository;
import com.maker.website.machines.enums.MachineStatusENUM;
import com.maker.website.orders.dtos.ProductionOrderFileDTO;
import com.maker.website.orders.dtos.ProductionOrderResponseDTO;
import com.maker.website.orders.dtos.ReviewProductionOrderDTO;
import com.maker.website.users.UserService;
import com.maker.website.users.UserEntity;
import com.maker.website.users.enums.UserRoleENUM;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductionOrderService {

    private static final long MAX_FILE_SIZE_BYTES = 100L * 1024 * 1024;

    private final ProductionOrderRepository productionOrderRepository;
    private final MachineRepository machineRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<ProductionOrderResponseDTO> getMyOrders(Long requesterId) {
        return productionOrderRepository.findAllByRequesterIdOrderByCreatedAtDesc(requesterId)
                .stream().map(ProductionOrderResponseDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductionOrderResponseDTO> getAllOrders() {
        return productionOrderRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(ProductionOrderResponseDTO::new).toList();
    }

    @Transactional
    public ProductionOrderResponseDTO createOrder(
            Long requesterId,
            ProductionOrderTypeENUM type,
            Long machineId,
            String notes,
            String dimensions,
            MultipartFile file
    ) throws IOException {
        validateRequest(type, machineId, notes, dimensions, file);
        var requester = userService.findActiveUserById(requesterId);

        MachineEntity machine = null;
        if (machineId != null) machine = findAvailableMachine(type, machineId);

        ProductionOrderEntity order = new ProductionOrderEntity();
        order.setRequester(requester);
        order.setType(type);
        order.setMachine(machine);
        setFile(order, file);
        order.setNotes(notes == null ? "" : notes.trim());
        order.setDimensions(dimensions == null ? "" : dimensions.trim());
        order.setStatus(ProductionOrderStatusENUM.PENDING);
        return new ProductionOrderResponseDTO(productionOrderRepository.save(order));
    }

    @Transactional
    public ProductionOrderResponseDTO updateOrder(
            Long id,
            Long requesterId,
            ProductionOrderTypeENUM type,
            Long machineId,
            String notes,
            String dimensions,
            MultipartFile file
    ) throws IOException {
        validateRequest(type, machineId, notes, dimensions, file);
        ProductionOrderEntity order = productionOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Production order not found."));
        if (!order.getRequester().getId().equals(requesterId)) {
            throw new AccessDeniedException("Only the requester can update this production order.");
        }
        if (order.getStatus() != ProductionOrderStatusENUM.PENDING
                && order.getStatus() != ProductionOrderStatusENUM.WAITING_FOR_UPDATE) {
            throw new IllegalArgumentException("Only pending or waiting production orders can be updated.");
        }

        order.setType(type);
        order.setMachine(machineId == null ? null : findAvailableMachine(type, machineId));
        order.setNotes(notes == null ? "" : notes.trim());
        order.setDimensions(dimensions == null ? "" : dimensions.trim());
        if (file != null && !file.isEmpty()) setFile(order, file);
        if (order.getStatus() == ProductionOrderStatusENUM.WAITING_FOR_UPDATE) {
            order.setStatus(ProductionOrderStatusENUM.PENDING);
            order.setAdminNotes(null);
        }
        return new ProductionOrderResponseDTO(productionOrderRepository.save(order));
    }

    @Transactional
    public ProductionOrderResponseDTO reviewOrder(Long id, ReviewProductionOrderDTO review, Long adminId) {
        validateReview(review);
        ProductionOrderEntity order = productionOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Production order not found."));
        UserEntity admin = userService.findActiveUserById(adminId);
        order.setStatus(review.status());
        String message = review.adminNotes() == null ? "" : review.adminNotes().trim();
        if (!message.isEmpty()) {
            order.setAdminNotes(message);
            addComment(order, admin, message);
        } else {
            order.setAdminNotes(null);
        }
        return new ProductionOrderResponseDTO(productionOrderRepository.save(order));
    }

    @Transactional
    public ProductionOrderResponseDTO updateEstimatedCompletion(
            Long id,
            LocalDateTime estimatedCompletionAt,
            Long adminId
    ) {
        if (estimatedCompletionAt == null) {
            throw new IllegalArgumentException("An estimated completion date and time is required.");
        }
        ProductionOrderEntity order = find3DOrderForScheduling(id, adminId);
        order.setEstimatedCompletionAt(estimatedCompletionAt);
        return new ProductionOrderResponseDTO(productionOrderRepository.save(order));
    }

    @Transactional
    public ProductionOrderResponseDTO clearEstimatedCompletion(Long id, Long adminId) {
        ProductionOrderEntity order = find3DOrderForScheduling(id, adminId);
        order.setEstimatedCompletionAt(null);
        return new ProductionOrderResponseDTO(productionOrderRepository.save(order));
    }

    @Transactional
    public ProductionOrderResponseDTO addComment(Long id, Long actorId, String message) {
        if (message == null || message.isBlank() || message.trim().length() > 2000) {
            throw new IllegalArgumentException("A comment of 1 to 2000 characters is required.");
        }
        ProductionOrderEntity order = productionOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Production order not found."));
        UserEntity actor = userService.findActiveUserById(actorId);
        if (actor.getRole() != UserRoleENUM.ADMIN) {
            if (!order.getRequester().getId().equals(actorId)) {
                throw new AccessDeniedException("Only the requester or an administrator can comment on this order.");
            }
            if (order.getStatus() != ProductionOrderStatusENUM.PENDING
                    && order.getStatus() != ProductionOrderStatusENUM.WAITING_FOR_UPDATE) {
                throw new IllegalArgumentException("Comments are only allowed while the order is open.");
            }
        }
        addComment(order, actor, message.trim());
        return new ProductionOrderResponseDTO(productionOrderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public ProductionOrderFileDTO getFile(Long id, Long requesterId, boolean isAdmin) {
        ProductionOrderEntity order = productionOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Production order not found."));
        if (!isAdmin && !order.getRequester().getId().equals(requesterId)) {
            throw new AccessDeniedException("Only the requester or an administrator can download this file.");
        }
        if (order.getFileData() == null || order.getFileData().length == 0) {
            throw new IllegalArgumentException("This production order does not have an uploaded file.");
        }
        return new ProductionOrderFileDTO(order.getFileName(), order.getContentType(), order.getFileData());
    }

    private void validateRequest(
            ProductionOrderTypeENUM type,
            Long machineId,
            String notes,
            String dimensions,
            MultipartFile file
    ) {
        if (type == null) {
            throw new IllegalArgumentException("Select a production order type.");
        }
        if (type != ProductionOrderTypeENUM.LASER_CUTTING
                && (machineId == null || machineId < 1)) {
            throw new IllegalArgumentException("Select a machine for this production order.");
        }
        if (type == ProductionOrderTypeENUM.LASER_CUTTING && machineId != null) {
            throw new IllegalArgumentException("A laser cutting order cannot include a machine.");
        }
        if (file != null && !file.isEmpty()) validateFile(file);
        if (notes != null && notes.trim().length() > 2000) {
            throw new IllegalArgumentException("Notes must contain at most 2000 characters.");
        }
        if (dimensions != null && dimensions.trim().length() > 2000) {
            throw new IllegalArgumentException("Dimensions and details must contain at most 2000 characters.");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("The uploaded file must not exceed 100 MB.");
        }
        if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            throw new IllegalArgumentException("The uploaded file must have a name.");
        }
        if (sanitizeFileName(file.getOriginalFilename()).length() > 1024) {
            throw new IllegalArgumentException("The uploaded file name must contain at most 1024 characters.");
        }
    }

    private MachineEntity findAvailableMachine(ProductionOrderTypeENUM type, Long machineId) {
        MachineEntity machine = machineRepository.findByIdAndStatusNot(machineId, MachineStatusENUM.UNUSABLE)
                .orElseThrow(() -> new IllegalArgumentException("The selected machine was not found."));
        boolean correctType = switch (type) {
            case THREE_D_PRINTING -> isThreeDPrinter(machine.getType());
            case CNC_MACHINING -> isCncMachine(machine.getType());
            case LASER_CUTTING -> false;
        };
        if (!correctType) {
            throw new IllegalArgumentException("The selected machine does not match the production order type.");
        }
        if (machine.getStatus() != MachineStatusENUM.AVAILABLE
                && machine.getStatus() != MachineStatusENUM.FUNCTIONAL) {
            throw new IllegalArgumentException("The selected machine is not available.");
        }
        return machine;
    }

    private ProductionOrderEntity find3DOrderForScheduling(Long id, Long adminId) {
        UserEntity admin = userService.findActiveUserById(adminId);
        if (admin.getRole() != UserRoleENUM.ADMIN) {
            throw new AccessDeniedException("Only an administrator can update an estimated completion date.");
        }
        ProductionOrderEntity order = productionOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Production order not found."));
        if (order.getType() != ProductionOrderTypeENUM.THREE_D_PRINTING) {
            throw new IllegalArgumentException("Estimated completion dates are only available for 3D printing orders.");
        }
        return order;
    }

    private void setFile(ProductionOrderEntity order, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return;
        String contentType = file.getContentType() == null || file.getContentType().isBlank()
                ? "application/octet-stream"
                : file.getContentType();
        if (contentType.length() > 255) contentType = "application/octet-stream";
        order.setFileName(sanitizeFileName(file.getOriginalFilename()));
        order.setContentType(contentType);
        order.setFileSize(file.getSize());
        order.setFileData(file.getBytes());
    }

    private void addComment(ProductionOrderEntity order, UserEntity author, String message) {
        var comment = new ProductionOrderCommentEntity();
        comment.setAuthor(author);
        comment.setMessage(message);
        order.addComment(comment);
    }

    private String sanitizeFileName(String fileName) {
        String normalized = fileName.replace('\\', '/');
        String baseName = normalized.substring(normalized.lastIndexOf('/') + 1);
        return baseName.isBlank() ? "upload" : baseName;
    }

    private boolean isThreeDPrinter(String type) {
        return type != null && Normalizer.normalize(type, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .equalsIgnoreCase("Impressao 3D");
    }

    private boolean isCncMachine(String type) {
        return type != null && Normalizer.normalize(type, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase()
                .contains("CNC");
    }

    private void validateReview(ReviewProductionOrderDTO review) {
        if (review == null || review.status() == null) {
            throw new IllegalArgumentException("A production order status is required.");
        }
        if (review.adminNotes() != null && review.adminNotes().trim().length() > 500) {
            throw new IllegalArgumentException("Admin notes must contain at most 500 characters.");
        }
        if ((review.status() == ProductionOrderStatusENUM.REJECTED
                || review.status() == ProductionOrderStatusENUM.WAITING_FOR_UPDATE)
                && (review.adminNotes() == null || review.adminNotes().isBlank())) {
            throw new IllegalArgumentException("An explanation is required when rejecting or requesting an update.");
        }
    }
}
