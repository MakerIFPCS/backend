package com.maker.website.orders;

import com.maker.website.machines.MachineEntity;
import com.maker.website.machines.MachineRepository;
import com.maker.website.machines.enums.MachineStatusENUM;
import com.maker.website.orders.dtos.ProductionOrderFileDTO;
import com.maker.website.orders.dtos.ProductionOrderResponseDTO;
import com.maker.website.orders.dtos.ReviewProductionOrderDTO;
import com.maker.website.users.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.Normalizer;
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
            MultipartFile file
    ) throws IOException {
        validateRequest(type, machineId, notes, file);
        var requester = userService.findActiveUserById(requesterId);

        MachineEntity machine = null;
        if (type == ProductionOrderTypeENUM.THREE_D_PRINTING) {
            machine = machineRepository.findByIdAndStatusNot(machineId, MachineStatusENUM.UNUSABLE)
                    .orElseThrow(() -> new IllegalArgumentException("3D printer not found."));
            if (!isThreeDPrinter(machine.getType())) {
                throw new IllegalArgumentException("The selected machine is not a 3D printer.");
            }
            if (machine.getStatus() != MachineStatusENUM.AVAILABLE
                    && machine.getStatus() != MachineStatusENUM.FUNCTIONAL) {
                throw new IllegalArgumentException("The selected 3D printer is not available.");
            }
        }

        String fileName = sanitizeFileName(file.getOriginalFilename());
        String contentType = file.getContentType() == null || file.getContentType().isBlank()
                ? "application/octet-stream"
                : file.getContentType();
        if (contentType.length() > 255) {
            contentType = "application/octet-stream";
        }

        ProductionOrderEntity order = new ProductionOrderEntity();
        order.setRequester(requester);
        order.setType(type);
        order.setMachine(machine);
        order.setFileName(fileName);
        order.setContentType(contentType);
        order.setFileSize(file.getSize());
        order.setFileData(file.getBytes());
        order.setNotes(notes == null ? "" : notes.trim());
        order.setStatus(ProductionOrderStatusENUM.PENDING);
        return new ProductionOrderResponseDTO(productionOrderRepository.save(order));
    }

    @Transactional
    public ProductionOrderResponseDTO reviewOrder(Long id, ReviewProductionOrderDTO review) {
        validateReview(review);
        ProductionOrderEntity order = productionOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Production order not found."));
        if (order.getStatus() != ProductionOrderStatusENUM.PENDING) {
            throw new IllegalArgumentException("Only pending production orders can be reviewed.");
        }
        order.setStatus(review.status());
        order.setAdminNotes(review.adminNotes() == null || review.adminNotes().isBlank()
                ? null
                : review.adminNotes().trim());
        return new ProductionOrderResponseDTO(productionOrderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public ProductionOrderFileDTO getFile(Long id, Long requesterId, boolean isAdmin) {
        ProductionOrderEntity order = productionOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Production order not found."));
        if (!isAdmin && !order.getRequester().getId().equals(requesterId)) {
            throw new AccessDeniedException("Only the requester or an administrator can download this file.");
        }
        return new ProductionOrderFileDTO(order.getFileName(), order.getContentType(), order.getFileData());
    }

    private void validateRequest(
            ProductionOrderTypeENUM type,
            Long machineId,
            String notes,
            MultipartFile file
    ) {
        if (type == null) {
            throw new IllegalArgumentException("Select a production order type.");
        }
        if (type == ProductionOrderTypeENUM.THREE_D_PRINTING
                && (machineId == null || machineId < 1)) {
            throw new IllegalArgumentException("Select a 3D printer.");
        }
        if (type == ProductionOrderTypeENUM.LASER_CUTTING && machineId != null) {
            throw new IllegalArgumentException("A laser cutting order cannot include a 3D printer.");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Select a non-empty file to upload.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("The uploaded file must not exceed 100 MB.");
        }
        if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            throw new IllegalArgumentException("The uploaded file must have a name.");
        }
        if (sanitizeFileName(file.getOriginalFilename()).length() > 1024) {
            throw new IllegalArgumentException("The uploaded file name must contain at most 1024 characters.");
        }
        if (notes != null && notes.trim().length() > 2000) {
            throw new IllegalArgumentException("Notes must contain at most 2000 characters.");
        }
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

    private void validateReview(ReviewProductionOrderDTO review) {
        if (review == null
                || review.status() == null
                || review.status() == ProductionOrderStatusENUM.PENDING) {
            throw new IllegalArgumentException("A review status of APPROVED or REJECTED is required.");
        }
        if (review.adminNotes() != null && review.adminNotes().trim().length() > 500) {
            throw new IllegalArgumentException("Admin notes must contain at most 500 characters.");
        }
    }
}
