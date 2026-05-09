package com.example.installmentreminder.payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PaymentResponse(Long id, Long userId, BigDecimal amount, String currency, LocalDate dueDate,
		PaymentStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
	static PaymentResponse from(Payment payment) {
		return new PaymentResponse(payment.getId(), payment.getUserId(), payment.getAmount(), payment.getCurrency(),
				payment.getDueDate(), payment.getStatus(), payment.getCreatedAt(), payment.getUpdatedAt());
	}
}
