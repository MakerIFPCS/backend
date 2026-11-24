package com.maker.website.handlers;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void reportsMissingUploadAsBadRequest() {
        var response = handler.handleMissingRequestPart(new MissingServletRequestPartException("file"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Required request part 'file' is missing.", response.getBody().message());
    }

    @Test
    void reportsOversizedUploadAsPayloadTooLarge() {
        var response = handler.handleMaxUploadSizeExceeded(new MaxUploadSizeExceededException(100L));

        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
        assertEquals("The uploaded file must not exceed 100 MB.", response.getBody().message());
    }

    @Test
    void reportsMissingAuthenticationAsUnauthorized() {
        var response = handler.handleAuthentication(new BadCredentialsException("Missing credentials."));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void reportsAuthorizationDenialAsForbidden() {
        var response = handler.handleAccessDenied(new AccessDeniedException("Not the owner."));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
