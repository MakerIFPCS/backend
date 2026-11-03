package com.maker.website.handlers;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void mapsIllegalArgumentsToBadRequest() {
        var response = handler.handleIllegalArgumentException(new IllegalArgumentException("Invalid input"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid input", response.getBody().message());
    }

    @Test
    void mapsAuthorizationAndAuthenticationErrors() {
        var forbidden = handler.handleAccessDenied(new AccessDeniedException("Denied"));
        var unauthorized = handler.handleAuthentication(new BadCredentialsException("Bad credentials"));

        assertEquals(HttpStatus.FORBIDDEN, forbidden.getStatusCode());
        assertEquals("Denied", forbidden.getBody().message());
        assertEquals(HttpStatus.UNAUTHORIZED, unauthorized.getStatusCode());
        assertEquals("Authentication is required.", unauthorized.getBody().message());
    }
}
