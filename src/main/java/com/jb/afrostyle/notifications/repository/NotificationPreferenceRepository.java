package com.jb.afrostyle.notifications.repository;

import com.jb.afrostyle.notifications.domain.enums.NotificationType;
import com.jb.afrostyle.notifications.domain.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    Optional<NotificationPreference> findByUserId(Long userId);

    @Query("SELECT np FROM NotificationPreference np " +
           "WHERE :notificationType MEMBER OF np.subscribedEvents")
    List<NotificationPreference> findBySubscribedEventsContaining(@Param("notificationType") NotificationType notificationType);

    @Query("SELECT np FROM NotificationPreference np " +
           "WHERE np.emailEnabled = true AND np.emailAddress IS NOT NULL " +
           "AND :notificationType MEMBER OF np.subscribedEvents")
    List<NotificationPreference> findEmailSubscribers(@Param("notificationType") NotificationType notificationType);

    @Query("SELECT np FROM NotificationPreference np " +
           "WHERE np.smsEnabled = true AND np.phoneNumber IS NOT NULL " +
           "AND :notificationType MEMBER OF np.subscribedEvents")
    List<NotificationPreference> findSmsSubscribers(@Param("notificationType") NotificationType notificationType);

    @Query("SELECT np FROM NotificationPreference np " +
           "WHERE np.webPushEnabled = true " +
           "AND :notificationType MEMBER OF np.subscribedEvents")
    List<NotificationPreference> findWebPushSubscribers(@Param("notificationType") NotificationType notificationType);

    List<NotificationPreference> findByEmailEnabledTrue();
    List<NotificationPreference> findBySmsEnabledTrue();
    List<NotificationPreference> findByWebPushEnabledTrue();
}