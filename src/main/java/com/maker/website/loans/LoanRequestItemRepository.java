package com.maker.website.loans;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface LoanRequestItemRepository extends JpaRepository<LoanRequestItemEntity, Long> {

    @Query("""
            select count(item)
            from LoanRequestItemEntity item
            where item.type = :type
              and item.resourceId = :resourceId
              and item.loanRequest.status = :status
              and item.loanRequest.pickupDate <= :returnDate
              and item.loanRequest.returnDate >= :pickupDate
            """)
    long countApprovedMachineConflicts(
            @Param("type") LoanResourceTypeENUM type,
            @Param("resourceId") Long resourceId,
            @Param("status") LoanRequestStatusENUM status,
            @Param("pickupDate") LocalDate pickupDate,
            @Param("returnDate") LocalDate returnDate
    );
}
