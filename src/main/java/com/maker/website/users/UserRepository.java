package com.maker.website.users;

import com.maker.website.users.enums.UserStatusENUM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    List<UserEntity> findAllByStatusNot(UserStatusENUM status);

    List<UserEntity> findAllByStatus(UserStatusENUM status);

    Optional<UserEntity> findByIdAndStatusNot(Long id, UserStatusENUM status);

    Optional<UserEntity> findByIdAndStatus(Long id, UserStatusENUM status);

    Optional<UserEntity> findByEmailAndStatusNot(String email, UserStatusENUM status);

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByCpf(String cpf);
}