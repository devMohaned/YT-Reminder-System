package com.example.installmentreminder.notification;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_outbox")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationOutbox {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	@Setter
	private Long userId;

	@Column(name = "payment_id", nullable = false)
	@Setter
	private Long paymentId;

	@Enumerated(EnumType.STRING)
	@Column(name = "notification_type", nullable = false)
	@Setter
	private NotificationType notificationType;

	@Column(nullable = false)
	@Setter
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	@Setter
	private String body;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Setter
	@Builder.Default
	private NotificationStatus status = NotificationStatus.PENDING;

	@Column(name = "retry_count", nullable = false)
	@Setter
	@Builder.Default
	private int retryCount = 0;

	@Column(name = "failure_reason", columnDefinition = "TEXT")
	@Setter
	private String failureReason;

	@Column(name = "idempotency_key", nullable = false)
	@Setter
	private String idempotencyKey;

	@Column(name = "next_attempt_at")
	@Setter
	private LocalDateTime nextAttemptAt;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "sent_at")
	@Setter
	private LocalDateTime sentAt;

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}
}
