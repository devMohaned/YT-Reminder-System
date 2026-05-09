package com.example.installmentreminder.payment;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.example.installmentreminder.common.ApiResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping
	public ApiResponse<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
		Payment payment = paymentService.createPayment(request);
		return ApiResponse.ok("Payment created", PaymentResponse.from(payment));
	}

	@GetMapping
	public ApiResponse<List<PaymentResponse>> findAll() {
		List<PaymentResponse> response = paymentService.findAll().stream().map(PaymentResponse::from).toList();

		return ApiResponse.ok("Payments retrieved", response);
	}

	@PostMapping("/{paymentId}/mark-paid")
	public ApiResponse<PaymentResponse> markAsPaid(@PathVariable @NotNull Long paymentId) {
		Payment payment = paymentService.markAsPaid(paymentId);
		return ApiResponse.ok("Payment marked as paid", PaymentResponse.from(payment));
	}
}
