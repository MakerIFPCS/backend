package com.maker.website.trainings;

import com.maker.website.trainings.dtos.TrainingPayloadDTO;
import com.maker.website.trainings.dtos.TrainingResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;

    @GetMapping
    public ResponseEntity<List<TrainingResponseDTO>> getTrainings() {
        return ResponseEntity.ok(trainingService.getAllTrainings());
    }

    @PostMapping
    public ResponseEntity<TrainingResponseDTO> createTraining(
            @RequestBody TrainingPayloadDTO payload,
            Authentication authentication
    ) {
        Long actorId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(trainingService.createTraining(payload, actorId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainingResponseDTO> updateTraining(
            @PathVariable Long id,
            @RequestBody TrainingPayloadDTO payload,
            Authentication authentication
    ) {
        Long actorId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(trainingService.updateTraining(id, payload, actorId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTraining(@PathVariable Long id, Authentication authentication) {
        Long actorId = (Long) authentication.getPrincipal();
        trainingService.deleteTraining(id, actorId);
        return ResponseEntity.noContent().build();
    }
}
