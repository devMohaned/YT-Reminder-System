package com.example.installmentreminder.notification;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.installmentreminder.common.util.MessageUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationRetryService {

	private final NotificationOutboxRepository notificationOutboxRepository;
	private final PushNotificationService pushNotificationService;

	@Value("${payment-reminder.jobs.notification-retry.max-retry-count}")
	private int maxRetryCount;

	@Value("${payment-reminder.jobs.notification-retry.batch-size}")
	private int batchSize;

	@Transactional
	public int retryFailedNotifications() {

		Page<NotificationOutbox> page = notificationOutboxRepository.findRetryableNotifications(
				List.of(NotificationStatus.PENDING, NotificationStatus.FAILED), maxRetryCount, LocalDateTime.now(),
				PageRequest.of(0, batchSize));

		int totalProcessed = 0;

		for (NotificationOutbox notification : page.getContent()) {

			try {
				if (notification.getUserId() == null) {
					log.warn("Notification {} skipped because userId was null", notification.getId());
					markFailed(notification, "Notification userId is missing");
					totalProcessed++;
					continue;
				}

				if (notification.getTitle() == null || notification.getBody() == null) {
					log.warn("Notification {} skipped because title/body data was missing", notification.getId());
					markFailed(notification, "Notification title/body is missing");
					totalProcessed++;
					continue;
				}

				pushNotificationService.sendPushNotification(notification.getUserId(), notification.getTitle(),
						notification.getBody());

				notification.setStatus(NotificationStatus.SENT);
				notification.setSentAt(LocalDateTime.now());
				notification.setFailureReason(null);
				notification.setNextAttemptAt(null);
			} catch (Exception ex) {
				log.error("Integration problem while retrying notificationId={}, userId={}, retryCount={}",
						notification.getId(), notification.getUserId(), notification.getRetryCount(), ex);
				markFailed(notification, ex.getMessage());
			}

			totalProcessed++;
		}

		return totalProcessed;
	}

	private long calculateBackoffMinutes(int retryCount) {
		long backoff = (long) Math.pow(2, retryCount);
		return Math.min(backoff, 60);
	}

	private void markFailed(NotificationOutbox notification, String reason) {
		int nextRetryCount = notification.getRetryCount() + 1;
		notification.setRetryCount(nextRetryCount);
		notification.setStatus(NotificationStatus.FAILED);
		notification.setFailureReason(MessageUtils.trimToMaxLength(reason));
		notification.setNextAttemptAt(LocalDateTime.now().plusMinutes(calculateBackoffMinutes(nextRetryCount)));
	}
}
