package com.maker.website.trainings;

import com.maker.website.reports.ReportAuditService;
import com.maker.website.trainings.dtos.TrainingPayloadDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock TrainingRepository trainingRepository;
    @Mock ReportAuditService reportAuditService;
    @InjectMocks TrainingService trainingService;

    @Test
    void createsTrainingAndNormalizesOptionalFields() {
        when(trainingRepository.save(any(TrainingEntity.class)))
                .thenAnswer(invocation -> {
                    TrainingEntity training = invocation.getArgument(0);
                    training.setId(8L);
                    return training;
                });
        TrainingPayloadDTO payload = new TrainingPayloadDTO(
                " Oficina de impressão 3D ",
                " Aprenda a preparar modelos. ",
                TrainingCategoryENUM.WORKSHOP,
                "",
                "09:30",
                "09:30",
                "12:00",
                " ",
                "data:image/gif;base64,R0lGODlh"
        );

        var result = trainingService.createTraining(payload, 12L);

        assertEquals(8L, result.id());
        assertEquals("Oficina de impressão 3D", result.name());
        assertEquals("Aprenda a preparar modelos.", result.description());
        assertEquals(TrainingCategoryENUM.WORKSHOP, result.category());
        assertNull(result.date());
        assertEquals("09:30", result.time());
        assertEquals("09:30", result.startTime());
        assertEquals("12:00", result.endTime());
        assertNull(result.location());
        assertEquals("data:image/gif;base64,R0lGODlh", result.image());
        verify(reportAuditService).record(
                "CAPACITACAO",
                "Capacitação cadastrada",
                "Oficina de impressão 3D",
                12L,
                "Categoria: WORKSHOP"
        );
    }

    @Test
    void rejectsInvalidDateBeforeSaving() {
        TrainingPayloadDTO payload = new TrainingPayloadDTO(
                "Curso",
                "Descrição",
                TrainingCategoryENUM.COURSE,
                "22/11/2026",
                "09:30",
                "09:30",
                "10:30",
                "Laboratório",
                null
        );

        assertThrows(IllegalArgumentException.class, () -> trainingService.createTraining(payload, 12L));
        verifyNoInteractions(trainingRepository, reportAuditService);
    }

    @Test
    void rejectsEndTimeThatIsNotAfterStartTime() {
        TrainingPayloadDTO payload = new TrainingPayloadDTO(
                "Curso",
                "Descrição",
                TrainingCategoryENUM.COURSE,
                "2026-11-22",
                null,
                "10:30",
                "10:30",
                "Laboratório",
                null
        );

        assertThrows(IllegalArgumentException.class, () -> trainingService.createTraining(payload, 12L));
        verifyNoInteractions(trainingRepository, reportAuditService);
    }

    @Test
    void rejectsInvalidImageDataBeforeSaving() {
        TrainingPayloadDTO payload = new TrainingPayloadDTO(
                "Curso",
                "Descrição",
                TrainingCategoryENUM.COURSE,
                "2026-11-22",
                null,
                null,
                null,
                "Laboratório",
                "data:image/png;base64,SGVsbG8="
        );

        assertThrows(IllegalArgumentException.class, () -> trainingService.createTraining(payload, 12L));
        verifyNoInteractions(trainingRepository, reportAuditService);
    }
}
