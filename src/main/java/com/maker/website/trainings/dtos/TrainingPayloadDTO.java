package com.maker.website.trainings.dtos;

import com.maker.website.trainings.TrainingCategoryENUM;

public record TrainingPayloadDTO(
        String name,
        String description,
        TrainingCategoryENUM category,
        String date,
        String time,
        String startTime,
        String endTime,
        String location,
        String image
) {
}
