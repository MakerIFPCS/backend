package com.maker.website.auth;

public record AuthResponseDTO(String token, String tokenType, Long userId, String role, String userName) {
}