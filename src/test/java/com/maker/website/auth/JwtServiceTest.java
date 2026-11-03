package com.maker.website.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "a-secret-key-that-is-at-least-32-characters-long";

    @Test
    void generatesAndParsesSignedToken() {
        JwtService service = new JwtService(SECRET, 60_000);
        String token = service.generateToken(123L, "USER");

        assertEquals(123L, service.extractUserId(token));
    }

    @Test
    void rejectsShortKeyAndTokenSignedWithAnotherKey() {
        assertThrows(IllegalArgumentException.class, () -> new JwtService("short", 1000));
        JwtService service = new JwtService(SECRET, 60_000);
        JwtService other = new JwtService("another-secret-key-that-is-at-least-32-characters", 60_000);
        assertThrows(RuntimeException.class, () -> service.extractUserId(other.generateToken(1L, "USER")));
        assertThrows(RuntimeException.class, () -> service.extractUserId("not-a-token"));
    }
}
