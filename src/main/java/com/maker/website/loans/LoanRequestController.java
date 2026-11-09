package com.maker.website.loans;

import com.maker.website.loans.dtos.CreateLoanRequestDTO;
import com.maker.website.loans.dtos.LoanRequestResponseDTO;
import com.maker.website.loans.dtos.UpdateLoanRequestStatusDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanRequestController {

    private final LoanRequestService loanRequestService;

    @GetMapping("/mine")
    public ResponseEntity<List<LoanRequestResponseDTO>> getMyRequests(Authentication authentication) {
        return ResponseEntity.ok(loanRequestService.getMyRequests(currentUserId(authentication)));
    }

    @PostMapping
    public ResponseEntity<LoanRequestResponseDTO> createRequest(
            @RequestBody CreateLoanRequestDTO dto,
            Authentication authentication
    ) {
        LoanRequestResponseDTO request = loanRequestService.createRequest(currentUserId(authentication), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(request);
    }

    @GetMapping
    public ResponseEntity<List<LoanRequestResponseDTO>> getAllRequests() {
        return ResponseEntity.ok(loanRequestService.getAllRequests());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<LoanRequestResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateLoanRequestStatusDTO dto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(loanRequestService.updateStatus(id, dto, currentUserId(authentication)));
    }

    private Long currentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
