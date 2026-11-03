package com.maker.website.materials;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MaterialRepository extends JpaRepository<MaterialEntity, Long> {

    List<MaterialEntity> findAllByOrderByNameAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select material from MaterialEntity material where material.id = :id")
    Optional<MaterialEntity> findByIdForUpdate(@Param("id") Long id);
}
