package com.maker.website.reports;

import com.maker.website.users.UserEntity;
import com.maker.website.users.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportAuditServiceTest {

    @Mock ReportEventRepository eventRepository;
    @Mock UserRepository userRepository;
    @InjectMocks ReportAuditService service;

    @Test
    void recordsSystemAndKnownOrUnknownActors() {
        service.record("TEST", "Action", "Subject", null, "Details");
        verify(eventRepository).save(argThat(event -> event.getActor().equals("Sistema")));

        UserEntity user = new UserEntity();
        user.setName("Ana");
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        service.record("TEST", "Action", "Subject", 2L, "Details");
        verify(eventRepository).save(argThat(event -> event.getActor().equals("Ana")));

        when(userRepository.findById(3L)).thenReturn(Optional.empty());
        service.record("TEST", "Action", "Subject", 3L, "Details");
        verify(eventRepository, times(3)).save(any(ReportEventEntity.class));
    }
}
