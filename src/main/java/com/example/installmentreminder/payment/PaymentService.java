package com.example.installmentreminder.payment;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.installmentreminder.notification.NotificationOutboxService;
import com.example.installmentreminder.notification.NotificationType;
import com.example.installmentreminder.user.AppUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final AppUserRepository appUserRepository;
	private final NotificationOutboxService notificationOutboxService;

	@Value("${payment-reminder.jobs.upcoming-payment-reminder.days-before-due-date}")
	private int reminderDaysBeforeDueDate;

	@Value("${payment-reminder.jobs.upcoming-payment-reminder.batch-size}")
	private int upcomingPaymentBatchSize;

	@Value("${payment-reminder.jobs.missed-payment.batch-size}")
	private int missedPaymentBatchSize;

	@Transactional
	public Payment createPayment(CreatePaymentRequest request) {
		appUserRepository.findById(request.userId())
				.orElseThrow(() -> new IllegalArgumentException("User does not exist: " + request.userId()));

		Payment payment = Payment.builder().userId(request.userId()).amount(request.amount())
				.currency(request.currency().toUpperCase()).dueDate(request.dueDate()).status(PaymentStatus.PENDING)
				.build();

		return paymentRepository.save(payment);
	}

	@Transactional(readOnly = true)
	public List<Payment> findAll() {
		return paymentRepository.findAll();
	}

	@Transactional
	public Payment markAsPaid(Long paymentId) {
		Payment payment = paymentRepository.findById(paymentId)
				.orElseThrow(() -> new IllegalArgumentException("Payment does not exist: " + paymentId));

		if (payment.getStatus() == PaymentStatus.PAID) {
			log.info("Payment {} is already marked as paid", paymentId);
			return payment;
		}

		if (payment.getStatus() == PaymentStatus.CANCELLED) {
			log.warn("Rejected mark-as-paid for cancelled paymentId={}", paymentId);
			throw new IllegalArgumentException("Cancelled payment cannot be marked as paid");
		}

		payment.setStatus(PaymentStatus.PAID);
		return payment;
	}

	@Transactional
	public int processUpcomingPaymentReminders() {
		LocalDate today = LocalDate.now();
		LocalDate endDate = today.plusDays(reminderDaysBeforeDueDate);

		int pageNumber = 0;
		int totalProcessed = 0;

		while (true) {
			Page<Payment> page = paymentRepository.findByStatusAndDueDateBetween(PaymentStatus.PENDING, today, endDate,
					PageRequest.of(pageNumber, upcomingPaymentBatchSize));

			if (page.isEmpty()) {
				break;
			}

			for (Payment payment : page.getContent()) {

				notificationOutboxService.createNotificationIfNotExists(payment,
						NotificationType.UPCOMING_PAYMENT_REMINDER);
				totalProcessed++;
			}

			if (!page.hasNext()) {
				break;
			}

			pageNumber++;
		}

		return totalProcessed;
	}

	@Transactional
	public int processMissedPayments() {
		LocalDate today = LocalDate.now();
		int totalProcessed = 0;

		while (true) {
			Page<Payment> page = paymentRepository.findByStatusAndDueDateBefore(PaymentStatus.PENDING, today,
					PageRequest.of(0, missedPaymentBatchSize));

			if (page.isEmpty()) {
				break;
			}

			for (Payment payment : page.getContent()) {

				payment.setStatus(PaymentStatus.OVERDUE);
				notificationOutboxService.createNotificationIfNotExists(payment, NotificationType.MISSED_PAYMENT_ALERT);
				totalProcessed++;
			}
		}

		return totalProcessed;
	}
}
