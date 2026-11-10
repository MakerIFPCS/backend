package com.maker.website.auth;

import com.maker.website.reports.ReportAuditService;
import com.maker.website.users.UserEntity;
import com.maker.website.users.UserService;
import com.maker.website.users.enums.UserRoleENUM;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAndAuthServiceTest {

    @Mock UserService userService;
    @Mock JwtService jwtService;
    @Mock ReportAuditService auditService;
    @InjectMocks AuthService authService;

    @Test
    void logsInAndReturnsBearerToken() {
        UserEntity user = new UserEntity();
        user.setId(4L);
        user.setName("Ana");
        user.setRole(UserRoleENUM.ADMIN);
        when(userService.authenticate("ana@example.com", "secret")).thenReturn(user);
        when(jwtService.generateToken(4L, "ADMIN")).thenReturn("token");

        AuthResponseDTO response = authService.login(new LoginDTO("ana@example.com", "secret"));

        assertEquals("token", response.token());
        assertEquals("Bearer", response.tokenType());
        assertEquals(UserRoleENUM.ADMIN.name(), response.role());
        verify(auditService).record("USUÁRIO", "Login realizado", "Ana", 4L, "Perfil: ADMIN");
    }

    @Test
    void createsAndParsesJwtAndRejectsShortSecret() {
        assertThrows(IllegalArgumentException.class, () -> new JwtService("short", 1000));
        JwtService service = new JwtService("12345678901234567890123456789012", 3600000);
        String token = service.generateToken(7L, "USER");
        assertEquals(7L, service.extractUserId(token));
    }
}
