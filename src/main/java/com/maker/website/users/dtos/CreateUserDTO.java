package com.maker.website.users.dtos;

import com.maker.website.users.enums.UserTypeENUM;

import java.time.LocalDate;

public record CreateUserDTO(
        String name,
        String password,
        String email,
        String cpf
) {
}
