package com.maker.website.users.dtos;

import com.maker.website.users.UserEntity;
import com.maker.website.users.enums.UserRoleENUM;
import com.maker.website.users.enums.UserStatusENUM;
import com.maker.website.users.enums.UserTypeENUM;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserResponseDTO(
        Long id,
        String name,
        String email,
        String cellphone,
        String cpf,
        UserTypeENUM type,
        LocalDate birthDate,
        UserRoleENUM role,
        UserStatusENUM status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public UserResponseDTO(UserEntity user) {
        this(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCellphone(),
                user.getCpf(),
                user.getType(),
                user.getBirthDate(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
