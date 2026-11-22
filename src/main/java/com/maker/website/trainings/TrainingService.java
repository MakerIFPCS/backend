package com.maker.website.trainings;

import com.maker.website.reports.ReportAuditService;
import com.maker.website.trainings.dtos.TrainingPayloadDTO;
import com.maker.website.trainings.dtos.TrainingResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class TrainingService {

    private static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024;
    private static final Pattern DATA_IMAGE_PATTERN = Pattern.compile(
            "^data:image/(png|jpeg|webp|gif);base64,([A-Za-z0-9+/]*={0,2})$",
            Pattern.CASE_INSENSITIVE
    );

    private final TrainingRepository trainingRepository;
    private final ReportAuditService reportAuditService;

    @Transactional(readOnly = true)
    public List<TrainingResponseDTO> getAllTrainings() {
        return trainingRepository.findAllByOrderByNameAsc()
                .stream()
                .map(TrainingResponseDTO::new)
                .toList();
    }

    @Transactional
    public TrainingResponseDTO createTraining(TrainingPayloadDTO payload, Long actorId) {
        validate(payload);
        TrainingEntity training = new TrainingEntity();
        updateFields(training, payload);
        TrainingEntity savedTraining = trainingRepository.save(training);
        reportAuditService.record("CAPACITACAO", "Capacitação cadastrada", savedTraining.getName(), actorId,
                "Categoria: " + savedTraining.getCategory());
        return new TrainingResponseDTO(savedTraining);
    }

    @Transactional
    public TrainingResponseDTO updateTraining(Long id, TrainingPayloadDTO payload, Long actorId) {
        validate(payload);
        TrainingEntity training = trainingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Training not found."));
        updateFields(training, payload);
        TrainingEntity savedTraining = trainingRepository.save(training);
        reportAuditService.record("CAPACITACAO", "Capacitação atualizada", savedTraining.getName(), actorId,
                "Categoria: " + savedTraining.getCategory());
        return new TrainingResponseDTO(savedTraining);
    }

    @Transactional
    public void deleteTraining(Long id, Long actorId) {
        TrainingEntity training = trainingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Training not found."));
        trainingRepository.delete(training);
        reportAuditService.record("CAPACITACAO", "Capacitação excluída", training.getName(), actorId,
                "Categoria: " + training.getCategory());
    }

    private void updateFields(TrainingEntity training, TrainingPayloadDTO payload) {
        training.setName(payload.name().trim());
        training.setDescription(payload.description().trim());
        training.setCategory(payload.category());
        training.setDate(normalizeOptional(payload.date()));
        String startTime = normalizeTime(
                payload.startTime() == null || payload.startTime().isBlank()
                        ? payload.time()
                        : payload.startTime()
        );
        training.setStartTime(startTime);
        training.setEndTime(normalizeTime(payload.endTime()));
        training.setTime(startTime);
        training.setLocation(normalizeOptional(payload.location()));
        training.setImage(normalizeOptional(payload.image()));
    }

    private void validate(TrainingPayloadDTO payload) {
        if (payload == null) throw new IllegalArgumentException("Training data is required.");
        if (payload.name() == null || payload.name().isBlank()) {
            throw new IllegalArgumentException("Training name is required.");
        }
        if (payload.name().trim().length() > 150) {
            throw new IllegalArgumentException("Training name must contain at most 150 characters.");
        }
        if (payload.description() == null || payload.description().isBlank()) {
            throw new IllegalArgumentException("Training description is required.");
        }
        if (payload.description().length() > 2000) {
            throw new IllegalArgumentException("Training description must contain at most 2000 characters.");
        }
        if (payload.category() == null) {
            throw new IllegalArgumentException("Training category is required.");
        }
        validateDate(payload.date());
        String startTime = payload.startTime() == null || payload.startTime().isBlank()
                ? payload.time()
                : payload.startTime();
        validateTime(startTime);
        validateTime(payload.endTime());
        if ((startTime == null || startTime.isBlank()) != (payload.endTime() == null || payload.endTime().isBlank())) {
            throw new IllegalArgumentException("Training start and end times must both be provided.");
        }
        if (startTime != null && !startTime.isBlank() && payload.endTime() != null && !payload.endTime().isBlank()
                && !LocalTime.parse(payload.endTime().trim()).isAfter(LocalTime.parse(startTime.trim()))) {
            throw new IllegalArgumentException("Training end time must be after its start time.");
        }
        if (payload.location() != null && payload.location().trim().length() > 200) {
            throw new IllegalArgumentException("Training location must contain at most 200 characters.");
        }
        validateImage(payload.image());
    }

    private void validateDate(String value) {
        if (value == null || value.isBlank()) return;
        try {
            LocalDate.parse(value.trim());
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Training date must use YYYY-MM-DD format.");
        }
    }

    private void validateTime(String value) {
        if (value == null || value.isBlank()) return;
        try {
            LocalTime.parse(value.trim());
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Training time must use HH:mm format.");
        }
    }

    private String normalizeTime(String value) {
        String normalized = normalizeOptional(value);
        return normalized == null ? null : LocalTime.parse(normalized).toString().substring(0, 5);
    }

    private void validateImage(String value) {
        if (value == null || value.isBlank()) return;
        String image = value.trim();
        if (image.startsWith("data:")) {
            Matcher matcher = DATA_IMAGE_PATTERN.matcher(image);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Training image must be an HTTP(S) URL or a supported image under 5 MB.");
            }
            validateDataImage(matcher);
            return;
        }
        if (image.length() > 2048) {
            throw new IllegalArgumentException("Training image URL must contain at most 2048 characters.");
        }

        if (image.startsWith("/")) {
            if (image.startsWith("//")) throw new IllegalArgumentException("Training image URL must be local or use HTTP(S).");
            return;
        }
        try {
            URI uri = URI.create(image);
            String scheme = uri.getScheme();
            if (uri.getHost() == null || scheme == null
                    || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("Training image URL must use HTTP or HTTPS.");
            }
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Training image must be a valid HTTP(S) URL or supported image under 5 MB.");
        }
    }

    private void validateDataImage(Matcher matcher) {
        String encodedImage = matcher.group(2);
        if (encodedImage.length() > ((MAX_IMAGE_BYTES + 2) / 3) * 4) {
            throw new IllegalArgumentException("Training image must not exceed 5 MB.");
        }
        try {
            byte[] imageBytes = Base64.getDecoder().decode(encodedImage);
            if (imageBytes.length == 0 || imageBytes.length > MAX_IMAGE_BYTES || !hasExpectedSignature(matcher.group(1), imageBytes)) {
                throw new IllegalArgumentException("Training image data is invalid or does not match its format.");
            }
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Training image data is invalid or exceeds the 5 MB limit.");
        }
    }

    private boolean hasExpectedSignature(String format, byte[] bytes) {
        return switch (format.toLowerCase(Locale.ROOT)) {
            case "png" -> bytes.length >= 8
                    && (bytes[0] & 0xFF) == 0x89
                    && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G'
                    && bytes[4] == 0x0D && bytes[5] == 0x0A && bytes[6] == 0x1A && bytes[7] == 0x0A;
            case "jpeg" -> bytes.length >= 3
                    && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF;
            case "gif" -> bytes.length >= 6
                    && bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F'
                    && bytes[3] == '8' && (bytes[4] == '7' || bytes[4] == '9') && bytes[5] == 'a';
            case "webp" -> bytes.length >= 12
                    && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                    && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P';
            default -> false;
        };
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }
}
