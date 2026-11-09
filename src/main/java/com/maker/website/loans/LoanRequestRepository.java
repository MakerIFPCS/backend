package com.maker.website.loans;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanRequestRepository extends JpaRepository<LoanRequestEntity, Long> {

    @EntityGraph(attributePaths = {"requester", "items", "items.machine", "items.material"})
    List<LoanRequestEntity> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"requester", "items", "items.machine", "items.material"})
    List<LoanRequestEntity> findByRequester_IdOrderByCreatedAtDesc(Long requesterId);

    @EntityGraph(attributePaths = {"requester", "items", "items.machine", "items.material"})
    Optional<LoanRequestEntity> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"items", "items.machine", "items.material"})
    List<LoanRequestEntity> findAllByStatusAndPickupDateLessThanEqualAndReturnDateGreaterThanEqual(
            LoanRequestStatusENUM status,
            java.time.LocalDate latestPickupDate,
            java.time.LocalDate earliestReturnDate
    );
}
