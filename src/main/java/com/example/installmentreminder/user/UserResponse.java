package com.example.installmentreminder.user;

import java.time.LocalDateTime;

public record UserResponse(Long id, String fullName, String phoneNumber, LocalDateTime createdAt) {
	static UserResponse from(AppUser user) {
		return new UserResponse(user.getId(), user.getFullName(), user.getPhoneNumber(), user.getCreatedAt());
	}
}
