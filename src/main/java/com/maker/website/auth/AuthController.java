package com.maker.website.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<AuthMessageDTO> verifyEmail(@RequestBody VerifyEmailDTO dto) {
        authService.verifyEmail(dto.token());
        return ResponseEntity.ok(new AuthMessageDTO("E-mail confirmado. Sua conta já está ativa."));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<AuthMessageDTO> resendVerification(@RequestBody ResendVerificationDTO dto) {
        authService.resendVerification(dto.email());
        return ResponseEntity.ok(new AuthMessageDTO(
                "Se houver um cadastro pendente para esse e-mail, enviaremos um novo link de confirmação."));
    }
}