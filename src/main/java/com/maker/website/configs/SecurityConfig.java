package com.maker.website.configs;

import com.maker.website.auth.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpStatus;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**", "/api/normas/pdf", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/users/*/promote-to-admin").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/users/*").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/users/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/users/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/materials", "/api/materials/**").permitAll()
                        .requestMatchers("/api/materials", "/api/materials/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/loans/mine").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/loans").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/loans").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/loans/*/status").hasRole("ADMIN")
                        .requestMatchers("/api/loans/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/orders").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/orders/mine").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/orders").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/orders/*/file").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/orders/*/status").hasRole("ADMIN")
                        .requestMatchers("/api/reports/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/machines/**").authenticated()
                        .requestMatchers("/api/machines/**").hasRole("ADMIN")
                        .requestMatchers("/api/agendamentos/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) -> {
                            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                            boolean authenticated = Boolean.TRUE.equals(request.getAttribute("jwtAuthenticated"))
                                    || (authentication != null
                                    && authentication.isAuthenticated()
                                    && !(authentication instanceof AnonymousAuthenticationToken));
                            response.sendError(
                                    authenticated ? HttpStatus.FORBIDDEN.value() : HttpStatus.UNAUTHORIZED.value(),
                                    authenticated ? "Access is denied." : "Authentication is required."
                            );
                        })
                        .accessDeniedHandler((request, response, exception) ->
                                response.sendError(HttpStatus.FORBIDDEN.value(), "Access is denied.")))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}