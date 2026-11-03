package com.maker.website.users;

import com.maker.website.users.dtos.CreateUserDTO;
import com.maker.website.users.dtos.UpdateUserDTO;
import com.maker.website.users.dtos.UserResponseDTO;
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
class UserControllerTest {

    @Mock UserService service;
    @Mock Authentication authentication;
    @InjectMocks UserController controller;

    @Test
    void delegatesUserEndpoints() {
        UserResponseDTO response = mock(UserResponseDTO.class);
        CreateUserDTO create = new CreateUserDTO("User", "secret", "user@example.com", null);
        UpdateUserDTO update = mock(UpdateUserDTO.class);
        when(authentication.getPrincipal()).thenReturn(7L);
        when(service.getAllUsers()).thenReturn(List.of(response));
        when(service.getUserById(3L, 7L)).thenReturn(response);
        when(service.createUser(create)).thenReturn(response);
        when(service.updateUser(3L, update, 7L)).thenReturn(response);
        when(service.promoteUserToAdmin(3L, 7L)).thenReturn(response);

        assertEquals(1, controller.getAllUsers().getBody().size());
        assertEquals(response, controller.getUserById(3L, authentication).getBody());
        assertEquals(201, controller.createUser(create).getStatusCode().value());
        assertEquals(response, controller.updateUser(3L, update, authentication).getBody());
        controller.deleteUser(3L, authentication);
        assertEquals(response, controller.promoteUserToAdmin(3L, authentication).getBody());
        verify(service).deleteUser(3L, 7L);
    }
}
