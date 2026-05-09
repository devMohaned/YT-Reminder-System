package com.example.installmentreminder.admin;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.installmentreminder.common.ApiResponse;
import com.example.installmentreminder.notification.NotificationOutboxRepository;
import com.example.installmentreminder.notification.NotificationResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

	private final NotificationOutboxRepository notificationOutboxRepository;

	@GetMapping
	public ApiResponse<List<NotificationResponse>> findLatestNotifications() {
		List<NotificationResponse> response = notificationOutboxRepository.findAll(PageRequest.of(0, 50)).stream()
				.map(NotificationResponse::from).toList();

		return ApiResponse.ok("Notifications retrieved", response);
	}
}
