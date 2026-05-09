package com.example.installmentreminder.notification;

public interface PushNotificationService {

	void sendPushNotification(Long userId, String title, String body);
}
