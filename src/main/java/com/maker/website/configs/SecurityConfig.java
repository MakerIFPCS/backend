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
                        .requestMatchers(HttpMethod.GET, "/api/machines/**").authenticated()
                        .requestMatchers("/api/machines/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/agendamentos/**").authenticated()
                        .requestMatchers("/api/agendamentos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/pedidos", "/api/emprestimos").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/admin", "/api/emprestimos/admin").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/**", "/api/emprestimos/**").hasRole("ADMIN")
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