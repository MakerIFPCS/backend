package com.maker.website.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock AuthService service;
    @InjectMocks AuthController controller;

    @Test
    void delegatesLogin() {
        LoginDTO login = new LoginDTO("user@example.com", "secret");
        AuthResponseDTO response = new AuthResponseDTO("token", "Bearer", 1L, "USER", "User");
        when(service.login(login)).thenReturn(response);

        assertEquals(response, controller.login(login).getBody());
        verify(service).login(login);
    }
}
