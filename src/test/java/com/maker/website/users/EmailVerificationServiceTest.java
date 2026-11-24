package com.maker.website.users;

import com.maker.website.reports.ReportAuditService;
import com.maker.website.users.enums.UserStatusENUM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock EmailVerificationTokenRepository tokenRepository;
    @Mock UserRepository userRepository;
    @Mock org.springframework.mail.javamail.JavaMailSender mailSender;
    @Mock ReportAuditService reportAuditService;
    @InjectMocks EmailVerificationService emailVerificationService;

    @BeforeEach
    void configureMail() {
        ReflectionTestUtils.setField(emailVerificationService, "mailHost", "smtp.example.com");
        ReflectionTestUtils.setField(emailVerificationService, "fromAddress", "no-reply@example.com");
        ReflectionTestUtils.setField(emailVerificationService, "frontendUrl", "https://frontend.example");
    }

    @Test
    void sendsHashedTokenAndActivatesUserAfterVerification() {
        UserEntity user = pendingUser();
        AtomicReference<EmailVerificationTokenEntity> savedToken = new AtomicReference<>();
        when(tokenRepository.findById(user.getId())).thenReturn(Optional.empty());
        when(tokenRepository.save(any(EmailVerificationTokenEntity.class))).thenAnswer(invocation -> {
            EmailVerificationTokenEntity token = invocation.getArgument(0);
            savedToken.set(token);
            return token;
        });

        emailVerificationService.sendVerification(user);

        ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(mailCaptor.capture());
        Matcher matcher = Pattern.compile("https://frontend\\.example/verify-email\\?token=([^\\s]+)")
                .matcher(mailCaptor.getValue().getText());
        assertTrue(matcher.find());
        String rawToken = matcher.group(1);
        assertNotEquals(rawToken, savedToken.get().getTokenHash());
        assertEquals(64, savedToken.get().getTokenHash().length());
        assertTrue(savedToken.get().getExpiresAt().isAfter(LocalDateTime.now()));

        when(tokenRepository.findByTokenHash(savedToken.get().getTokenHash()))
                .thenReturn(Optional.of(savedToken.get()));
        emailVerificationService.verifyEmail(rawToken);

        assertEquals(UserStatusENUM.ACTIVE, user.getStatus());
        verify(tokenRepository).delete(savedToken.get());
        verify(reportAuditService).record(
                "USUÁRIO",
                "E-mail confirmado",
                user.getName(),
                user.getId(),
                "Endereço confirmado: " + user.getEmail()
        );
    }

    @Test
    void rejectsUnknownVerificationToken() {
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> emailVerificationService.verifyEmail("unknown-token"));

        verifyNoInteractions(userRepository, reportAuditService);
    }

    @Test
    void failsClearlyWhenMailIsNotConfigured() {
        UserEntity user = pendingUser();
        ReflectionTestUtils.setField(emailVerificationService, "mailHost", "");
        ReflectionTestUtils.setField(emailVerificationService, "fromAddress", "");
        when(tokenRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(EmailDeliveryException.class,
                () -> emailVerificationService.sendVerification(user));

        verifyNoInteractions(mailSender, reportAuditService);
    }

    private UserEntity pendingUser() {
        UserEntity user = new UserEntity();
        user.setId(7L);
        user.setName("Usuário de teste");
        user.setEmail("user@example.com");
        user.setStatus(UserStatusENUM.PENDING_VERIFICATION);
        return user;
    }
}
