package com.maker.website.auth;

import com.maker.website.users.UserEntity;
import com.maker.website.users.UserService;
import com.maker.website.users.enums.UserRoleENUM;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserService userService;
    @Mock JwtService jwtService;
    @InjectMocks AuthService authService;

    @Test
    void logsInAndReturnsBearerTokenAndUserDetails() {
        UserEntity user = new UserEntity();
        user.setId(5L);
        user.setName("Admin");
        user.setRole(UserRoleENUM.ADMIN);
        when(userService.authenticate("admin@example.com", "secret")).thenReturn(user);
        when(jwtService.generateToken(5L, "ADMIN")).thenReturn("signed-token");

        var response = authService.login(new LoginDTO("admin@example.com", "secret"));

        assertEquals("signed-token", response.token());
        assertEquals("Bearer", response.tokenType());
        assertEquals(5L, response.userId());
        assertEquals("ADMIN", response.role());
        assertEquals("Admin", response.userName());
    }
}
