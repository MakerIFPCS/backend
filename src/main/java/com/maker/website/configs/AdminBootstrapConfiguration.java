package com.maker.website.configs;

import com.maker.website.users.UserEntity;
import com.maker.website.users.UserRepository;
import com.maker.website.users.enums.UserRoleENUM;
import com.maker.website.users.enums.UserStatusENUM;
import com.maker.website.users.enums.UserTypeENUM;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminBootstrapConfiguration {

    @Bean
    CommandLineRunner bootstrapAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email:}") String email,
            @Value("${app.admin.password:}") String password
    ) {
        return args -> {
            if (email.isBlank() && password.isBlank()) return;
            if (email.isBlank() || password.isBlank()) {
                throw new IllegalStateException("Set both ADMIN_EMAIL and ADMIN_PASSWORD to bootstrap the admin account.");
            }
            if (password.length() < 12) {
                throw new IllegalStateException("ADMIN_PASSWORD must contain at least 12 characters.");
            }

            var existing = userRepository.findByEmail(email);
            if (existing.isPresent()) {
                if (existing.get().getRole() != UserRoleENUM.ADMIN) {
                    throw new IllegalStateException("The configured admin email belongs to a non-admin user.");
                }
                return;
            }

            UserEntity admin = new UserEntity();
            admin.setName("Administrator");
            admin.setEmail(email);
            admin.setPassword(passwordEncoder.encode(password));
            admin.setRole(UserRoleENUM.ADMIN);
            admin.setType(UserTypeENUM.SERVIDOR);
            admin.setStatus(UserStatusENUM.ACTIVE);
            userRepository.save(admin);
        };
    }
}
