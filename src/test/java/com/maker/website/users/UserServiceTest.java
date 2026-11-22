package com.maker.website.users;

import com.maker.website.reports.ReportAuditService;
import com.maker.website.users.dtos.CreateUserDTO;
import com.maker.website.users.enums.UserStatusENUM;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock ReportAuditService reportAuditService;
    @Mock EmailVerificationService emailVerificationService;
    @InjectMocks UserService userService;

    @Test
    void createsPendingAccountAndRequestsEmailVerification() {
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(9L);
            return user;
        });

        var response = userService.createUser(new CreateUserDTO(
                "Teste",
                "password",
                "  User@Example.com ",
                null
        ));

        assertEquals(9L, response.id());
        assertEquals("user@example.com", response.email());
        assertEquals(UserStatusENUM.PENDING_VERIFICATION, response.status());
        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(emailVerificationService).sendVerification(userCaptor.capture());
        assertEquals(UserStatusENUM.PENDING_VERIFICATION, userCaptor.getValue().getStatus());
        assertEquals("user@example.com", userCaptor.getValue().getEmail());
    }

    @Test
    void doesNotAllowPendingAccountToAuthenticate() {
        UserEntity user = new UserEntity();
        user.setId(9L);
        user.setEmail("user@example.com");
        user.setPassword("encoded-password");
        user.setStatus(UserStatusENUM.PENDING_VERIFICATION);
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.authenticate(" USER@example.com ", "password")
        );

        assertEquals("Confirme seu e-mail pelo link enviado antes de entrar.", exception.getMessage());
        verifyNoInteractions(emailVerificationService);
    }
}
