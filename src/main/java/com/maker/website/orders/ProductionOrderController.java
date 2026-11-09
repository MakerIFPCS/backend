package com.maker.website.orders;

import com.maker.website.orders.dtos.ProductionOrderFileDTO;
import com.maker.website.orders.dtos.ProductionOrderResponseDTO;
import com.maker.website.orders.dtos.AddProductionOrderCommentDTO;
import com.maker.website.orders.dtos.ReviewProductionOrderDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class ProductionOrderController {

    private final ProductionOrderService productionOrderService;

    @GetMapping("/mine")
    public ResponseEntity<List<ProductionOrderResponseDTO>> getMyOrders(Authentication authentication) {
        return ResponseEntity.ok(productionOrderService.getMyOrders((Long) authentication.getPrincipal()));
    }

    @GetMapping
    public ResponseEntity<List<ProductionOrderResponseDTO>> getAllOrders() {
        return ResponseEntity.ok(productionOrderService.getAllOrders());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductionOrderResponseDTO> createOrder(
            @RequestParam ProductionOrderTypeENUM type,
            @RequestParam(required = false) Long machineId,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) String dimensions,
            @RequestParam(required = false) MultipartFile file,
            Authentication authentication
    ) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(productionOrderService.createOrder(
                (Long) authentication.getPrincipal(), type, machineId, notes, dimensions, file));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductionOrderResponseDTO> updateOrder(
            @PathVariable Long id,
            @RequestParam ProductionOrderTypeENUM type,
            @RequestParam(required = false) Long machineId,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) String dimensions,
            @RequestParam(required = false) MultipartFile file,
            Authentication authentication
    ) throws IOException {
        return ResponseEntity.ok(productionOrderService.updateOrder(
                id, (Long) authentication.getPrincipal(), type, machineId, notes, dimensions, file));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ProductionOrderResponseDTO> reviewOrder(
            @PathVariable Long id,
            @RequestBody ReviewProductionOrderDTO review,
            Authentication authentication
    ) {
        return ResponseEntity.ok(productionOrderService.reviewOrder(
                id, review, (Long) authentication.getPrincipal()));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ProductionOrderResponseDTO> addComment(
            @PathVariable Long id,
            @RequestBody AddProductionOrderCommentDTO comment,
            Authentication authentication
    ) {
        return ResponseEntity.ok(productionOrderService.addComment(
                id, (Long) authentication.getPrincipal(), comment.message()));
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<byte[]> getFile(@PathVariable Long id, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        ProductionOrderFileDTO file = productionOrderService.getFile(
                id,
                (Long) authentication.getPrincipal(),
                isAdmin
        );
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.fileName(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(file.fileData());
    }
}
