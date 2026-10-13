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
            @Value("${app.admin.password:}") String password,
            @Value("${demo.user.email:usuario@sitemaker.local}") String demoUserEmail,
            @Value("${demo.user.password:usuario123}") String demoUserPassword
    ) {
        return args -> {
            if (!email.isBlank() || !password.isBlank()) {
                if (email.isBlank() || password.isBlank()) {
                    throw new IllegalStateException("Set both ADMIN_EMAIL and ADMIN_PASSWORD to bootstrap the admin account.");
                }
                if (password.length() < 12) {
                    throw new IllegalStateException("ADMIN_PASSWORD must contain at least 12 characters.");
                }

                var existingAdmin = userRepository.findByEmail(email);
                if (existingAdmin.isPresent()) {
                    if (existingAdmin.get().getRole() != UserRoleENUM.ADMIN) {
                        throw new IllegalStateException("The configured admin email belongs to a non-admin user.");
                    }
                } else {
                    UserEntity admin = new UserEntity();
                    admin.setName("Administrator");
                    admin.setEmail(email);
                    admin.setPassword(passwordEncoder.encode(password));
                    admin.setRole(UserRoleENUM.ADMIN);
                    admin.setType(UserTypeENUM.SERVIDOR);
                    admin.setStatus(UserStatusENUM.ACTIVE);
                    userRepository.save(admin);
                }
            }

            if (!userRepository.existsByEmail(demoUserEmail)) {
                UserEntity demoUser = new UserEntity();
                demoUser.setName("Usuario de demonstracao");
                demoUser.setEmail(demoUserEmail);
                demoUser.setPassword(passwordEncoder.encode(demoUserPassword));
                demoUser.setRole(UserRoleENUM.USER);
                demoUser.setType(UserTypeENUM.ALUNO);
                demoUser.setStatus(UserStatusENUM.ACTIVE);
                userRepository.save(demoUser);
            }
        };
    }
}
