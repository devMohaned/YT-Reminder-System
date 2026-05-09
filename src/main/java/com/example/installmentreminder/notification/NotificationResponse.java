package com.example.installmentreminder.notification;

import java.time.LocalDateTime;

public record NotificationResponse(Long id, Long userId, Long paymentId, NotificationType notificationType,
		String title, String body, NotificationStatus status, int retryCount, String failureReason,
		String idempotencyKey, LocalDateTime nextAttemptAt, LocalDateTime createdAt, LocalDateTime sentAt) {
	public static NotificationResponse from(NotificationOutbox notification) {
		return new NotificationResponse(notification.getId(), notification.getUserId(), notification.getPaymentId(),
				notification.getNotificationType(), notification.getTitle(), notification.getBody(),
				notification.getStatus(), notification.getRetryCount(), notification.getFailureReason(),
				notification.getIdempotencyKey(), notification.getNextAttemptAt(), notification.getCreatedAt(),
				notification.getSentAt());
	}
}
