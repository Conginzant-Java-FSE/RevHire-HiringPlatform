package com.example.revhirehiringplatform.service;



import com.example.revhirehiringplatform.model.Notification;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private Notification notification;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        notification = new Notification();
        notification.setId(10L);
        notification.setUser(user);
        notification.setMessage("Test Message");
        notification.setRead(false);
    }

    @Test
    void testCreateNotification() {
        notificationService.createNotification(user, "Hello");
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testGetUnreadNotifications() {
        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification));

        List<Notification> results = notificationService.getUnreadNotifications(user);

        assertNotNull(results);
    }

    @Test
    void testMarkAsRead() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(10L, user);

        assertTrue(notification.isRead());
        verify(notificationRepository).save(notification);
    }
}