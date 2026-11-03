package com.maker.website.loans;

import com.maker.website.loans.dtos.CreateLoanRequestDTO;
import com.maker.website.loans.dtos.LoanRequestResponseDTO;
import com.maker.website.loans.dtos.ReviewLoanRequestDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanRequestControllerTest {

    @Mock LoanRequestService service;
    @Mock Authentication authentication;
    @InjectMocks LoanRequestController controller;

    @Test
    void delegatesUserAndAdminLoanRoutes() {
        LoanRequestResponseDTO response = mock(LoanRequestResponseDTO.class);
        CreateLoanRequestDTO create = mock(CreateLoanRequestDTO.class);
        ReviewLoanRequestDTO review = mock(ReviewLoanRequestDTO.class);
        when(authentication.getPrincipal()).thenReturn(7L);
        when(service.getMyRequests(7L)).thenReturn(List.of(response));
        when(service.getAllRequests()).thenReturn(List.of(response));
        when(service.createRequest(7L, create)).thenReturn(response);
        when(service.reviewRequest(3L, review)).thenReturn(response);

        assertEquals(1, controller.getMyRequests(authentication).getBody().size());
        assertEquals(1, controller.getAllRequests().getBody().size());
        assertEquals(201, controller.createRequest(create, authentication).getStatusCode().value());
        assertEquals(response, controller.reviewRequest(3L, review).getBody());
    }
}
