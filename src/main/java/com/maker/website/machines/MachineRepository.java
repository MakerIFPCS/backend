package com.maker.website.machines;

import com.maker.website.machines.enums.MachineStatusENUM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MachineRepository extends JpaRepository<MachineEntity, Long> {
    List<MachineEntity> findAllByStatusNot(MachineStatusENUM statusENUM);
    Optional<MachineEntity> findByIdAndStatusNot(Long id, MachineStatusENUM StatusENUM);

    boolean existsByName(String name);

    boolean existsByCode(String code);
}
