package com.maker.website.orders;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductionOrderRepository extends JpaRepository<ProductionOrderEntity, Long> {

    @Query("select order from ProductionOrderEntity order where order.requester.id = :requesterId order by order.createdAt desc")
    List<ProductionOrderEntity> findAllByRequesterIdOrderByCreatedAtDesc(@Param("requesterId") Long requesterId);

    List<ProductionOrderEntity> findAllByOrderByCreatedAtDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select order from ProductionOrderEntity order where order.id = :id")
    Optional<ProductionOrderEntity> findByIdForUpdate(@Param("id") Long id);
}
