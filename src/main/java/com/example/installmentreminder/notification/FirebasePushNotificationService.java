package com.example.installmentreminder.notification;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.example.installmentreminder.user.DeviceToken;
import com.example.installmentreminder.user.DeviceTokenRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "payment-reminder.push.provider", havingValue = "firebase")
public class FirebasePushNotificationService implements PushNotificationService {

	private final DeviceTokenRepository deviceTokenRepository;
	private final FirebaseMessaging firebaseMessaging;

	@Override
	public void sendPushNotification(Long userId, String title, String body) {
		if (userId == null) {
			log.error("Cannot send Firebase push notification because userId was null");
			throw new IllegalArgumentException("userId must not be null");
		}

		if (title == null || title.isBlank()) {
			log.error("Cannot send Firebase push notification because title was blank. userId={}", userId);
			throw new IllegalArgumentException("title must not be blank");
		}

		if (body == null || body.isBlank()) {
			log.error("Cannot send Firebase push notification because body was blank. userId={}", userId);
			throw new IllegalArgumentException("body must not be blank");
		}

		List<DeviceToken> activeTokens = deviceTokenRepository.findByUserIdAndActiveTrue(userId);

		if (activeTokens.isEmpty()) {
			log.warn("No active device tokens found for userId={}", userId);
			throw new IllegalStateException("No active device tokens for userId=" + userId);
		}

		int attempted = 0;

		for (DeviceToken deviceToken : activeTokens) {
			if (deviceToken == null || deviceToken.getToken() == null || deviceToken.getToken().isBlank()) {
				log.warn("Skipping empty device token entry for userId={}", userId);
				continue;
			}

			Message message = Message.builder().setToken(deviceToken.getToken())
					.setNotification(Notification.builder().setTitle(title).setBody(body).build()).build();

			try {
				firebaseMessaging.send(message);
				attempted++;
			} catch (Exception ex) {
				log.error("Integration problem while sending Firebase push notification. userId={}, tokenId={}", userId,
						deviceToken.getId(), ex);
				throw new IllegalStateException("Failed to send Firebase push notification", ex);
			}
		}

		if (attempted == 0) {
			log.warn("No valid active tokens could be used for userId={}", userId);
			throw new IllegalStateException("No valid active device tokens for userId=" + userId);
		}
	}
}
