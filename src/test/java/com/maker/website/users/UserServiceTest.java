package com.maker.website.users;

import com.maker.website.reports.ReportAuditService;
import com.maker.website.users.dtos.CreateUserDTO;
import com.maker.website.users.dtos.UpdateUserDTO;
import com.maker.website.users.enums.UserRoleENUM;
import com.maker.website.users.enums.UserStatusENUM;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository repository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock ReportAuditService auditService;
    @InjectMocks UserService service;

    @Test
    void createsAndAuthenticatesUser() {
        when(repository.existsByEmail("new@example.com")).thenReturn(false);
        when(repository.existsByCpf("123")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(repository.save(any())).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        var response = service.createUser(new CreateUserDTO("Ana", "secret", "new@example.com", "123"));

        assertEquals(1L, response.id());
        verify(passwordEncoder).encode("secret");
        verify(auditService).record(eq("USUÁRIO"), eq("Conta criada"), eq("Ana"), isNull(), anyString());

        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setEmail("new@example.com");
        user.setPassword("encoded");
        user.setRole(UserRoleENUM.USER);
        when(repository.findByEmailAndStatusNot("new@example.com", UserStatusENUM.DELETED))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);
        assertSame(user, service.authenticate("new@example.com", "secret"));
    }

    @Test
    void updatesOwnProfileAndDeletesIt() {
        UserEntity user = user(2L, UserRoleENUM.USER);
        when(repository.findByIdAndStatusNot(2L, UserStatusENUM.DELETED)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-password")).thenReturn("new-encoded");
        when(repository.save(user)).thenReturn(user);

        service.updateUser(2L, new UpdateUserDTO("Updated", "new-password", "updated@example.com",
                "999", "456", null, LocalDate.of(2000, 1, 1)), 2L);
        assertEquals("Updated", user.getName());
        assertEquals("new-encoded", user.getPassword());
        assertEquals("updated@example.com", user.getEmail());

        service.deleteUser(2L, 2L);
        assertEquals(UserStatusENUM.DELETED, user.getStatus());
        verify(auditService, times(2)).record(anyString(), anyString(), anyString(), eq(2L), anyString());
    }

    @Test
    void enforcesPermissionsAndUniqueness() {
        UserEntity user = user(2L, UserRoleENUM.USER);
        when(repository.findByIdAndStatusNot(2L, UserStatusENUM.DELETED)).thenReturn(Optional.of(user));
        assertThrows(AccessDeniedException.class, () -> service.getUserById(3L, 2L));

        when(repository.existsByEmail("used@example.com")).thenReturn(true);
        assertThrows(IllegalArgumentException.class,
                () -> service.createUser(new CreateUserDTO("A", "p", "used@example.com", null)));

        when(repository.findByEmailAndStatusNot("missing@example.com", UserStatusENUM.DELETED))
                .thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.authenticate("missing@example.com", "p"));

        when(repository.findByEmailAndStatusNot("new@example.com", UserStatusENUM.DELETED))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", user.getPassword())).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> service.authenticate("new@example.com", "bad"));
    }

    @Test
    void promotesUserOnlyWhenActorIsAdmin() {
        UserEntity actor = user(9L, UserRoleENUM.ADMIN);
        UserEntity target = user(3L, UserRoleENUM.USER);
        when(repository.findByIdAndStatusNot(9L, UserStatusENUM.DELETED)).thenReturn(Optional.of(actor));
        when(repository.findByIdAndStatusNot(3L, UserStatusENUM.DELETED)).thenReturn(Optional.of(target));
        when(repository.save(target)).thenReturn(target);

        assertEquals(UserRoleENUM.ADMIN, service.promoteUserToAdmin(3L, 9L).role());
        target.setRole(UserRoleENUM.ADMIN);
        assertThrows(IllegalArgumentException.class, () -> service.promoteUserToAdmin(3L, 9L));

        actor.setRole(UserRoleENUM.USER);
        assertThrows(AccessDeniedException.class, () -> service.promoteUserToAdmin(3L, 9L));
    }

    private UserEntity user(Long id, UserRoleENUM role) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setName("User " + id);
        user.setEmail("user" + id + "@example.com");
        user.setPassword("password");
        user.setRole(role);
        user.setStatus(UserStatusENUM.ACTIVE);
        return user;
    }
}
