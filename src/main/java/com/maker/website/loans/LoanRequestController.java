package com.maker.website.loans;

import com.maker.website.loans.dtos.CreateLoanRequestDTO;
import com.maker.website.loans.dtos.LoanRequestResponseDTO;
import com.maker.website.loans.dtos.ReviewLoanRequestDTO;
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
        return ResponseEntity.ok(loanRequestService.getMyRequests((Long) authentication.getPrincipal()));
    }

    @GetMapping
    public ResponseEntity<List<LoanRequestResponseDTO>> getAllRequests() {
        return ResponseEntity.ok(loanRequestService.getAllRequests());
    }

    @PostMapping
    public ResponseEntity<LoanRequestResponseDTO> createRequest(
            @RequestBody CreateLoanRequestDTO request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                loanRequestService.createRequest((Long) authentication.getPrincipal(), request)
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<LoanRequestResponseDTO> reviewRequest(
            @PathVariable Long id,
            @RequestBody ReviewLoanRequestDTO review
    ) {
        return ResponseEntity.ok(loanRequestService.reviewRequest(id, review));
    }
}
