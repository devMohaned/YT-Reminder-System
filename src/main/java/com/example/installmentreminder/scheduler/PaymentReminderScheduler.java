package com.example.installmentreminder.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.installmentreminder.job.CronJobExecutionService;
import com.example.installmentreminder.notification.NotificationRetryService;
import com.example.installmentreminder.payment.PaymentService;

import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Component
@RequiredArgsConstructor
public class PaymentReminderScheduler {

	private final PaymentService paymentService;
	private final NotificationRetryService notificationRetryService;
	private final CronJobExecutionService cronJobExecutionService;

	@Scheduled(cron = "${payment-reminder.jobs.upcoming-payment-reminder.cron}", zone = "${payment-reminder.jobs.zone}")
	@SchedulerLock(name = "upcomingPaymentReminderJob", lockAtMostFor = "15m", lockAtLeastFor = "1m")
	public void sendUpcomingPaymentReminders() {
		cronJobExecutionService.runJob("UPCOMING_PAYMENT_REMINDER", paymentService::processUpcomingPaymentReminders);
	}

	@Scheduled(cron = "${payment-reminder.jobs.missed-payment.cron}", zone = "${payment-reminder.jobs.zone}")
	@SchedulerLock(name = "missedPaymentJob", lockAtMostFor = "15m", lockAtLeastFor = "1m")
	public void processMissedPayments() {
		cronJobExecutionService.runJob("MISSED_PAYMENT", paymentService::processMissedPayments);
	}

	@Scheduled(cron = "${payment-reminder.jobs.notification-retry.cron}", zone = "${payment-reminder.jobs.zone}")
	@SchedulerLock(name = "notificationRetryJob", lockAtMostFor = "10m", lockAtLeastFor = "30s")
	public void retryNotifications() {
		cronJobExecutionService.runJob("NOTIFICATION_RETRY", notificationRetryService::retryFailedNotifications);
	}
}
