package com.maker.website.users;

import com.maker.website.users.dtos.CreateUserDTO;
import com.maker.website.users.dtos.UpdateUserDTO;
import com.maker.website.users.enums.UserRoleENUM;
import com.maker.website.users.enums.UserStatusENUM;
import com.maker.website.users.enums.UserTypeENUM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserService userService;

    private UserEntity user;
    private UserEntity admin;

    @BeforeEach
    void setUp() {
        user = user(1L, UserRoleENUM.USER);
        admin = user(2L, UserRoleENUM.ADMIN);
    }

    @Test
    void listsNonDeletedUsers() {
        when(userRepository.findAllByStatusNot(UserStatusENUM.DELETED)).thenReturn(List.of(user));

        assertEquals(1, userService.getAllUsers().size());
        verify(userRepository).findAllByStatusNot(UserStatusENUM.DELETED);
    }

    @Test
    void createsUserWithEncodedPasswordAndDefaults() {
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsByCpf("123")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = userService.createUser(new CreateUserDTO("New User", "secret", "new@example.com", "123"));

        assertEquals("new@example.com", result.email());
        assertEquals(UserRoleENUM.USER, result.role());
        assertEquals(UserStatusENUM.ACTIVE, result.status());
        verify(passwordEncoder).encode("secret");
    }

    @Test
    void rejectsMissingOrDuplicateEmailAndDuplicateCpf() {
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(
                new CreateUserDTO("Name", "secret", " ", null)));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(
                new CreateUserDTO("Name", "secret", "taken@example.com", null)));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsByCpf("123")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(
                new CreateUserDTO("Name", "secret", "new@example.com", "123")));
    }

    @Test
    void ownersAndAdminsCanReadAndUpdateUsers() {
        when(userRepository.findByIdAndStatusNot(1L, UserStatusENUM.DELETED))
                .thenReturn(Optional.of(user));
        when(userRepository.findByIdAndStatusNot(2L, UserStatusENUM.DELETED))
                .thenReturn(Optional.of(admin));
        when(userRepository.existsByEmail("changed@example.com")).thenReturn(false);
        when(userRepository.existsByCpf("456")).thenReturn(false);
        when(passwordEncoder.encode("new-secret")).thenReturn("encoded");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(user.getId(), userService.getUserById(1L, 1L).id());
        assertEquals(user.getId(), userService.getUserById(1L, 2L).id());
        var updated = userService.updateUser(1L, new UpdateUserDTO(
                "Updated", "new-secret", "changed@example.com", "555", "456",
                UserTypeENUM.SERVIDOR, LocalDate.of(2000, 1, 1)), 1L);
        assertEquals("Updated", updated.name());
        assertEquals("changed@example.com", updated.email());
        assertEquals("encoded", user.getPassword());
        assertEquals(UserTypeENUM.SERVIDOR, updated.type());
    }

    @Test
    void rejectsNonOwnerAndAllowsPartialUpdate() {
        when(userRepository.findByIdAndStatusNot(1L, UserStatusENUM.DELETED))
                .thenReturn(Optional.of(user));
        assertThrows(AccessDeniedException.class, () -> userService.getUserById(2L, 1L));
        assertThrows(AccessDeniedException.class, () -> userService.updateUser(
                2L, new UpdateUserDTO(null, null, null, null, null, null, null), 1L));

        when(userRepository.save(user)).thenReturn(user);
        var result = userService.updateUser(1L,
                new UpdateUserDTO("Name", " ", null, null, null, null, null), 1L);
        assertEquals("Name", result.name());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void rejectsDuplicateValuesOnUpdateAndHandlesDeletedUsers() {
        when(userRepository.findByIdAndStatusNot(1L, UserStatusENUM.DELETED))
                .thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("duplicate@example.com")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(
                1L, new UpdateUserDTO(null, null, "duplicate@example.com", null, null, null, null), 1L));
        when(userRepository.existsByEmail("valid@example.com")).thenReturn(false);
        when(userRepository.existsByCpf("duplicate-cpf")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(
                1L, new UpdateUserDTO(null, null, "valid@example.com", null, "duplicate-cpf", null, null), 1L));

        when(userRepository.findByIdAndStatusNot(99L, UserStatusENUM.DELETED)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.findActiveUserById(99L));
    }

    @Test
    void deletesOwnAccountAndRequiresAdminToPromote() {
        when(userRepository.findByIdAndStatusNot(1L, UserStatusENUM.DELETED))
                .thenReturn(Optional.of(user));
        when(userRepository.findByIdAndStatusNot(2L, UserStatusENUM.DELETED))
                .thenReturn(Optional.of(admin));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.deleteUser(1L, 1L);
        assertEquals(UserStatusENUM.DELETED, user.getStatus());
        assertThrows(AccessDeniedException.class, () -> userService.promoteUserToAdmin(1L, 1L));
        assertEquals(UserRoleENUM.ADMIN, userService.promoteUserToAdmin(1L, 2L).role());
        assertThrows(IllegalArgumentException.class, () -> userService.promoteUserToAdmin(2L, 2L));
    }

    @Test
    void authenticatesCorrectPasswordAndRejectsBadCredentials() {
        when(userRepository.findByEmailAndStatusNot("user@example.com", UserStatusENUM.DELETED))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct", "hash")).thenReturn(true);
        user.setPassword("hash");

        assertSame(user, userService.authenticate("user@example.com", "correct"));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> userService.authenticate("user@example.com", "wrong"));
        when(userRepository.findByEmailAndStatusNot("missing@example.com", UserStatusENUM.DELETED))
                .thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.authenticate("missing@example.com", "secret"));
    }

    private UserEntity user(Long id, UserRoleENUM role) {
        UserEntity entity = new UserEntity();
        entity.setId(id);
        entity.setName("User " + id);
        entity.setEmail("user" + id + "@example.com");
        entity.setPassword("hash");
        entity.setRole(role);
        entity.setType(UserTypeENUM.ALUNO);
        entity.setStatus(UserStatusENUM.ACTIVE);
        return entity;
    }
}
