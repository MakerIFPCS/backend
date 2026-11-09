package com.maker.website.reports;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportEventRepository extends JpaRepository<ReportEventEntity, Long> {

    List<ReportEventEntity> findByOccurredAtGreaterThanEqualAndOccurredAtLessThanOrderByOccurredAtDescIdDesc(
            LocalDateTime fromDate,
            LocalDateTime untilDate
    );

    List<ReportEventEntity> findByOccurredAtGreaterThanEqualOrderByOccurredAtDescIdDesc(LocalDateTime fromDate);

    List<ReportEventEntity> findByOccurredAtLessThanOrderByOccurredAtDescIdDesc(LocalDateTime untilDate);

    List<ReportEventEntity> findAllByOrderByOccurredAtDescIdDesc();
}
