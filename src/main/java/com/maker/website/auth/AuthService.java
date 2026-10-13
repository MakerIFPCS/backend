package com.maker.website.auth;

import com.maker.website.users.UserEntity;
import com.maker.website.users.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthResponseDTO login(LoginDTO dto) {
        UserEntity user = userService.authenticate(dto.email(), dto.password());
        String token = jwtService.generateToken(user.getId(), user.getRole().name());
        return new AuthResponseDTO(token, "Bearer", user.getId(), user.getRole().name(), user.getName());
    }
}