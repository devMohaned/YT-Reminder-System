package com.example.installmentreminder.admin;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.installmentreminder.common.ApiResponse;
import com.example.installmentreminder.job.CronJobExecutionResponse;
import com.example.installmentreminder.job.CronJobExecutionService;
import com.example.installmentreminder.notification.NotificationRetryService;
import com.example.installmentreminder.payment.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/jobs")
@RequiredArgsConstructor
public class AdminJobController {

	private final PaymentService paymentService;
	private final NotificationRetryService notificationRetryService;
	private final CronJobExecutionService cronJobExecutionService;

	@PostMapping("/upcoming-payment-reminders/run")
	public ApiResponse<Integer> runUpcomingPaymentReminderJob() {
		int processed = cronJobExecutionService.runJob("MANUAL_UPCOMING_PAYMENT_REMINDER",
				paymentService::processUpcomingPaymentReminders);

		return ApiResponse.ok("Upcoming payment reminder job triggered", processed);
	}

	@PostMapping("/missed-payments/run")
	public ApiResponse<Integer> runMissedPaymentJob() {
		int processed = cronJobExecutionService.runJob("MANUAL_MISSED_PAYMENT", paymentService::processMissedPayments);

		return ApiResponse.ok("Missed payment job triggered", processed);
	}

	@PostMapping("/notifications/retry")
	public ApiResponse<Integer> runNotificationRetryJob() {
		int processed = cronJobExecutionService.runJob("MANUAL_NOTIFICATION_RETRY",
				notificationRetryService::retryFailedNotifications);

		return ApiResponse.ok("Notification retry job triggered", processed);
	}

	@GetMapping("/executions")
	public ApiResponse<List<CronJobExecutionResponse>> findLatestExecutions() {
		List<CronJobExecutionResponse> response = cronJobExecutionService.findLatestExecutions().stream()
				.map(CronJobExecutionResponse::from).toList();

		return ApiResponse.ok("Latest job executions retrieved", response);
	}
}
