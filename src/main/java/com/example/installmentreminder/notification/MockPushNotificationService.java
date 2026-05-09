package com.example.installmentreminder.notification;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@ConditionalOnProperty(name = "payment-reminder.push.provider", havingValue = "mock", matchIfMissing = true)
@Slf4j
public class MockPushNotificationService implements PushNotificationService {

	@Override
	public void sendPushNotification(Long userId, String title, String body) {
		if (userId == null) {
			log.warn("Mock push notification skipped because userId was null");
			return;
		}

		log.info("Mock push notification sent to userId={}, title={}, body={}", userId, title, body);
	}
}
