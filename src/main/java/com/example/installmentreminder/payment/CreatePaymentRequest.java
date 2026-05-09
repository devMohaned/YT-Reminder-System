package com.example.installmentreminder.payment;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePaymentRequest(@NotNull Long userId, @NotNull @DecimalMin("0.01") BigDecimal amount,
		@NotBlank @Size(min = 3, max = 3) String currency, @NotNull LocalDate dueDate) {
}
