package com.maker.website.configs;

import com.maker.website.users.UserEntity;
import com.maker.website.users.UserRepository;
import com.maker.website.users.enums.UserRoleENUM;
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminBootstrapConfigurationTest {

    private final AdminBootstrapConfiguration configuration = new AdminBootstrapConfiguration();
    private final UserRepository repository = mock(UserRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);

    @Test
    void createsConfiguredAdminAndDemoUserWhenMissing() throws Exception {
        when(repository.findByEmail("admin@example.com")).thenReturn(Optional.empty());
        when(repository.existsByEmail("demo@example.com")).thenReturn(false);
        when(encoder.encode("long-admin-password")).thenReturn("admin-hash");
        when(encoder.encode("demo-password")).thenReturn("demo-hash");
        when(repository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CommandLineRunner runner = runner("admin@example.com", "long-admin-password", "demo@example.com", "demo-password");

        runner.run();

        verify(repository, times(2)).save(any(UserEntity.class));
        verify(encoder).encode("long-admin-password");
        verify(encoder).encode("demo-password");
    }

    @Test
    void keepsExistingAdminAndDemoUser() throws Exception {
        UserEntity existingAdmin = new UserEntity();
        existingAdmin.setRole(UserRoleENUM.ADMIN);
        when(repository.findByEmail("admin@example.com")).thenReturn(Optional.of(existingAdmin));
        when(repository.existsByEmail("demo@example.com")).thenReturn(true);

        runner("admin@example.com", "long-admin-password", "demo@example.com", "demo-password").run();

        verify(repository, never()).save(any(UserEntity.class));
    }

    @Test
    void rejectsIncompleteOrWeakAdminConfiguration() {
        assertThrows(IllegalStateException.class, () ->
                runner("admin@example.com", "", "demo@example.com", "demo").run());
        assertThrows(IllegalStateException.class, () ->
                runner("", "long-admin-password", "demo@example.com", "demo").run());
        assertThrows(IllegalStateException.class, () ->
                runner("admin@example.com", "short", "demo@example.com", "demo").run());
    }

    @Test
    void refusesToReplaceExistingNonAdminAccount() {
        UserEntity existingUser = new UserEntity();
        existingUser.setRole(UserRoleENUM.USER);
        when(repository.findByEmail("admin@example.com")).thenReturn(Optional.of(existingUser));

        assertThrows(IllegalStateException.class, () ->
                runner("admin@example.com", "long-admin-password", "demo@example.com", "demo").run());
    }

    private CommandLineRunner runner(String adminEmail, String adminPassword, String demoEmail, String demoPassword) {
        return configuration.bootstrapAdmin(repository, encoder, adminEmail, adminPassword, demoEmail, demoPassword);
    }
}
