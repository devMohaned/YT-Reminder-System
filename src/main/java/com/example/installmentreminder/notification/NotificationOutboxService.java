package com.example.installmentreminder.notification;

import java.time.LocalDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.example.installmentreminder.payment.Payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationOutboxService {

	private final NotificationOutboxRepository notificationOutboxRepository;

	public void createNotificationIfNotExists(Payment payment, NotificationType type) {
		if (payment == null) {
			log.error("Cannot create notification because payment was null");
			throw new IllegalArgumentException("payment must not be null");
		}

		if (type == null) {
			log.error("Cannot create notification because notification type was null. paymentId={}", payment.getId());
			throw new IllegalArgumentException("notification type must not be null");
		}

		if (payment.getId() == null || payment.getUserId() == null) {
			log.error("Cannot create notification because payment data is incomplete. paymentId={}, userId={}",
					payment.getId(), payment.getUserId());
			throw new IllegalArgumentException("payment id and userId must not be null");
		}

		String idempotencyKey = buildIdempotencyKey(payment, type);

		if (notificationOutboxRepository.existsByIdempotencyKey(idempotencyKey)) {
			log.debug("Notification already exists for idempotencyKey={}", idempotencyKey);
			return;
		}

		NotificationOutbox notification = NotificationOutbox.builder().userId(payment.getUserId())
				.paymentId(payment.getId()).notificationType(type).title(buildTitle(type))
				.body(buildBody(payment, type)).status(NotificationStatus.PENDING).retryCount(0)
				.idempotencyKey(idempotencyKey).nextAttemptAt(LocalDateTime.now()).build();

		try {
			notificationOutboxRepository.save(notification);
		} catch (DataIntegrityViolationException ex) {
			// Another app instance may have inserted the same idempotency key.
			// The unique index protects us from duplicate notifications.
			log.warn("Duplicate notification insertion prevented by unique idempotency key. idempotencyKey={}",
					idempotencyKey, ex);
		}
	}

	private String buildIdempotencyKey(Payment payment, NotificationType type) {
		if (type == NotificationType.UPCOMING_PAYMENT_REMINDER) {
			return type + ":" + payment.getId() + ":" + payment.getUserId() + ":" + payment.getDueDate();
		}

		return type + ":" + payment.getId() + ":" + payment.getUserId();
	}

	private String buildTitle(NotificationType type) {
		return switch (type) {
		case UPCOMING_PAYMENT_REMINDER -> "Upcoming installment reminder";
		case MISSED_PAYMENT_ALERT -> "Missed installment payment";
		};
	}

	private String buildBody(Payment payment, NotificationType type) {
		return switch (type) {
		case UPCOMING_PAYMENT_REMINDER -> "You have an installment of " + payment.getAmount() + " "
				+ payment.getCurrency() + " due on " + payment.getDueDate() + ".";

		case MISSED_PAYMENT_ALERT -> "You missed an installment of " + payment.getAmount() + " " + payment.getCurrency()
				+ ". Please complete it as soon as possible.";
		};
	}
}
