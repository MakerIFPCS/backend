package com.maker.website.machines;

import com.maker.website.machines.enums.MachineStatusENUM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface MachineRepository extends JpaRepository<MachineEntity, Long> {
    List<MachineEntity> findAllByStatusNot(MachineStatusENUM statusENUM);
    Optional<MachineEntity> findByIdAndStatusNot(Long id, MachineStatusENUM StatusENUM);

    boolean existsByName(String name);

    boolean existsByCode(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select machine from MachineEntity machine where machine.id = :id")
    Optional<MachineEntity> findByIdForUpdate(@Param("id") Long id);
}
