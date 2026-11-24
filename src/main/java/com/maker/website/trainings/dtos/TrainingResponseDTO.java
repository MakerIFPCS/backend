package com.maker.website.trainings.dtos;

import com.maker.website.trainings.TrainingCategoryENUM;
import com.maker.website.trainings.TrainingEntity;

import java.time.LocalDateTime;

public record TrainingResponseDTO(
        Long id,
        String name,
        String description,
        TrainingCategoryENUM category,
        String date,
        String time,
        String startTime,
        String endTime,
        String location,
        String image,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public TrainingResponseDTO(TrainingEntity training) {
        this(
                training.getId(),
                training.getName(),
                training.getDescription(),
                training.getCategory(),
                training.getDate(),
                training.getStartTime() != null ? training.getStartTime() : training.getTime(),
                training.getStartTime() != null ? training.getStartTime() : training.getTime(),
                training.getEndTime(),
                training.getLocation(),
                training.getImage(),
                training.getCreatedAt(),
                training.getUpdatedAt()
        );
    }
}
