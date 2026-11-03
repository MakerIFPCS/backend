package com.maker.website.loans;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoanRequestRepository extends JpaRepository<LoanRequestEntity, Long> {

    List<LoanRequestEntity> findAllByOrderByCreatedAtDesc();

    List<LoanRequestEntity> findAllByRequesterIdOrderByCreatedAtDesc(Long requesterId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select request from LoanRequestEntity request where request.id = :id")
    Optional<LoanRequestEntity> findByIdForUpdate(@Param("id") Long id);
}
