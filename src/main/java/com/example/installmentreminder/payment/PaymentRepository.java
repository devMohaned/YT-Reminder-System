package com.example.installmentreminder.payment;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Page<Payment> findByStatusAndDueDateBetween(PaymentStatus status, LocalDate startDate, LocalDate endDate,
			Pageable pageable);

	Page<Payment> findByStatusAndDueDateBefore(PaymentStatus status, LocalDate date, Pageable pageable);
}
