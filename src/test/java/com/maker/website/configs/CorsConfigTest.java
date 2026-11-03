package com.maker.website.configs;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistration;

import static org.mockito.Mockito.*;

class CorsConfigTest {

    @Test
    void configuresAllowedLocalOriginsAndMethods() {
        CorsRegistry registry = mock(CorsRegistry.class);
        CorsRegistration registration = mock(CorsRegistration.class);
        when(registry.addMapping("/**")).thenReturn(registration);
        when(registration.allowedOrigins("http://localhost:3000", "http://localhost:3002"))
                .thenReturn(registration);
        when(registration.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"))
                .thenReturn(registration);
        when(registration.allowedHeaders("*")).thenReturn(registration);

        new CorsConfig().addCorsMappings(registry);

        verify(registration).allowedHeaders("*");
    }
}
