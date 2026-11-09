package com.maker.website.reports;

import com.maker.website.users.UserEntity;
import com.maker.website.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportAuditService {

    private final ReportEventRepository reportEventRepository;
    private final UserRepository userRepository;

    public void record(
            String category,
            String action,
            String subject,
            Long actorId,
            String details
    ) {
        ReportEventEntity event = new ReportEventEntity();
        event.setCategory(category);
        event.setAction(action);
        event.setSubject(subject);
        event.setActor(resolveActor(actorId));
        event.setDetails(details);
        reportEventRepository.save(event);
    }

    private String resolveActor(Long actorId) {
        if (actorId == null) return "Sistema";
        return userRepository.findById(actorId)
                .map(UserEntity::getName)
                .orElse("Usuário #" + actorId);
    }
}
