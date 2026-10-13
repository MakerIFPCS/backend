package com.maker.website.users.dtos;

import com.maker.website.users.enums.UserTypeENUM;

import java.time.LocalDate;

public record UpdateUserDTO(
        String name,
        String password,
        String email,
        String cellphone,
        String cpf,
        UserTypeENUM type,
        LocalDate birthDate
) {
}
