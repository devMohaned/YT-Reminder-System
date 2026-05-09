package com.example.installmentreminder.user;

public record DeviceTokenResponse(Long id, Long userId, DevicePlatform platform, boolean active) {
	static DeviceTokenResponse from(DeviceToken token) {
		return new DeviceTokenResponse(token.getId(), token.getUserId(), token.getPlatform(), token.isActive());
	}
}
