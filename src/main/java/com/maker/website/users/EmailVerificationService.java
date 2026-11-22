package com.maker.website.users;

import com.maker.website.reports.ReportAuditService;
import com.maker.website.users.enums.UserStatusENUM;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final Duration TOKEN_LIFETIME = Duration.ofHours(24);
    private static final Duration RESEND_INTERVAL = Duration.ofMinutes(1);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final ReportAuditService reportAuditService;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${app.mail.from:}")
    private String fromAddress;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Transactional
    public void sendVerification(UserEntity user) {
        sendVerification(user, false);
    }

    @Transactional
    public void resendVerification(String email) {
        if (email == null || email.isBlank()) return;
        userRepository.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .filter(user -> user.getStatus() == UserStatusENUM.PENDING_VERIFICATION)
                .ifPresent(user -> sendVerification(user, true));
    }

    @Transactional
    public void verifyEmail(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("O link de confirmação é inválido ou está incompleto.");
        }

        EmailVerificationTokenEntity token = tokenRepository.findByTokenHash(hashToken(rawToken.trim()))
                .orElseThrow(() -> new IllegalArgumentException("O link de confirmação é inválido ou já foi utilizado."));
        if (!token.getExpiresAt().isAfter(LocalDateTime.now())) {
            tokenRepository.delete(token);
            throw new IllegalArgumentException("O link de confirmação expirou. Solicite um novo e-mail.");
        }

        UserEntity user = token.getUser();
        if (user.getStatus() != UserStatusENUM.PENDING_VERIFICATION) {
            tokenRepository.delete(token);
            throw new IllegalArgumentException("Este e-mail já foi confirmado.");
        }

        user.setStatus(UserStatusENUM.ACTIVE);
        userRepository.save(user);
        tokenRepository.delete(token);
        reportAuditService.record("USUÁRIO", "E-mail confirmado", user.getName(), user.getId(),
                "Endereço confirmado: " + user.getEmail());
    }

    private void sendVerification(UserEntity user, boolean enforceResendInterval) {
        if (user.getStatus() != UserStatusENUM.PENDING_VERIFICATION) return;

        EmailVerificationTokenEntity token = tokenRepository.findById(user.getId())
                .orElseGet(EmailVerificationTokenEntity::new);
        if (enforceResendInterval
                && token.getCreatedAt() != null
                && token.getCreatedAt().isAfter(LocalDateTime.now().minus(RESEND_INTERVAL))) {
            return;
        }

        if (mailHost == null || mailHost.isBlank() || fromAddress == null || fromAddress.isBlank()) {
            throw new EmailDeliveryException(
                    "O envio de e-mail não está configurado. Configure MAIL_HOST e MAIL_FROM no backend.");
        }

        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        token.setUser(user);
        token.setTokenHash(hashToken(rawToken));
        token.setExpiresAt(LocalDateTime.now().plus(TOKEN_LIFETIME));
        token.setCreatedAt(LocalDateTime.now());
        tokenRepository.save(token);

        String verificationUrl = frontendUrl.replaceAll("/+$", "")
                + "/verify-email?token=" + rawToken;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(user.getEmail());
        message.setSubject("Confirme seu e-mail - Lab Maker");
        message.setText("""
                Olá, %s!

                Para confirmar seu endereço de e-mail e ativar sua conta no Lab Maker, acesse:
                %s

                Este link expira em 24 horas. Se você não criou uma conta, ignore esta mensagem.
                """.formatted(user.getName(), verificationUrl));

        try {
            mailSender.send(message);
        } catch (MailException exception) {
            throw new EmailDeliveryException("Não foi possível enviar o e-mail de confirmação. Tente novamente mais tarde.", exception);
        }
    }

    private String hashToken(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available.", exception);
        }
    }
}
