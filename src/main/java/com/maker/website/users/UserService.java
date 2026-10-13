package com.maker.website.users;

import com.maker.website.users.dtos.CreateUserDTO;
import com.maker.website.users.dtos.UpdateUserDTO;
import com.maker.website.users.dtos.UserResponseDTO;
import com.maker.website.users.enums.UserRoleENUM;
import com.maker.website.users.enums.UserStatusENUM;
import com.maker.website.users.enums.UserTypeENUM;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAllByStatusNot(UserStatusENUM.DELETED)
                .stream().map(UserResponseDTO::new).toList();
    }

    public UserResponseDTO getUserById(Long requestedId, Long authenticatedId) {
        UserEntity authenticatedUser = findActiveUserById(authenticatedId);
        requireOwnerOrAdmin(authenticatedUser, requestedId);
        return new UserResponseDTO(findActiveUserById(requestedId));
    }

    public UserResponseDTO createUser(CreateUserDTO dto) {
        validateEmail(dto.email());
        validateCpf(dto.cpf());

        UserEntity user = new UserEntity();
        user.setName(dto.name());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setEmail(dto.email());
        user.setCellphone("+5535992202021");
        user.setCpf(dto.cpf());
        user.setType(UserTypeENUM.ALUNO);
        user.setBirthDate(LocalDate.now());
        user.setRole(UserRoleENUM.USER);
        user.setStatus(UserStatusENUM.ACTIVE);
        return new UserResponseDTO(userRepository.save(user));
    }

    public UserResponseDTO updateUser(Long id, UpdateUserDTO dto, Long authenticatedId) {
        UserEntity authenticatedUser = findActiveUserById(authenticatedId);
        requireOwnerOrAdmin(authenticatedUser, id);
        UserEntity user = findActiveUserById(id);

        if (dto.email() != null && !dto.email().equals(user.getEmail())) {
            validateEmail(dto.email());
            user.setEmail(dto.email());
        }
        if (dto.cpf() != null && !dto.cpf().equals(user.getCpf())) {
            validateCpf(dto.cpf());
            user.setCpf(dto.cpf());
        }
        if (dto.name() != null) user.setName(dto.name());
        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.password()));
        }
        if (dto.cellphone() != null) user.setCellphone(dto.cellphone());
        if (dto.type() != null) user.setType(dto.type());
        if (dto.birthDate() != null) user.setBirthDate(dto.birthDate());

        return new UserResponseDTO(userRepository.save(user));
    }

    public void deleteUser(Long id, Long authenticatedId) {
        UserEntity authenticatedUser = findActiveUserById(authenticatedId);
        requireOwnerOrAdmin(authenticatedUser, id);
        UserEntity user = findActiveUserById(id);
        user.setStatus(UserStatusENUM.DELETED);
        userRepository.save(user);
    }

    public UserResponseDTO promoteUserToAdmin(Long userId, Long authenticatedId) {
        UserEntity adminUser = findActiveUserById(authenticatedId);
        requireAdmin(adminUser);
        UserEntity userToPromote = findActiveUserById(userId);
        if (userToPromote.getRole() == UserRoleENUM.ADMIN) {
            throw new IllegalArgumentException("User is already an ADMIN.");
        }
        userToPromote.setRole(UserRoleENUM.ADMIN);
        return new UserResponseDTO(userRepository.save(userToPromote));
    }

    public UserEntity authenticate(String email, String rawPassword) {
        UserEntity user = userRepository.findByEmailAndStatusNot(email, UserStatusENUM.DELETED)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }
        return user;
    }

    public UserEntity findActiveUserById(Long id) {
        return userRepository.findByIdAndStatusNot(id, UserStatusENUM.DELETED)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    private void requireOwnerOrAdmin(UserEntity authenticatedUser, Long requestedId) {
        if (!authenticatedUser.getId().equals(requestedId) && authenticatedUser.getRole() != UserRoleENUM.ADMIN) {
            throw new AccessDeniedException("You can only access your own user data.");
        }
    }

    private void requireAdmin(UserEntity user) {
        if (user.getRole() != UserRoleENUM.ADMIN) {
            throw new AccessDeniedException("Only ADMIN users can perform this action.");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email is required.");
        if (userRepository.existsByEmail(email)) throw new IllegalArgumentException("Email is already in use.");
    }

    private void validateCpf(String cpf) {
        if (cpf != null && !cpf.isBlank() && userRepository.existsByCpf(cpf)) {
            throw new IllegalArgumentException("CPF is already in use.");
        }
    }
}