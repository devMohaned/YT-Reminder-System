package com.example.installmentreminder.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

	private final AppUserRepository appUserRepository;
	private final DeviceTokenRepository deviceTokenRepository;

	@Transactional
	public AppUser createUser(CreateUserRequest request) {
		AppUser user = AppUser.builder().fullName(request.fullName()).phoneNumber(request.phoneNumber()).build();
		return appUserRepository.save(user);
	}

	@Transactional
	public DeviceToken registerDeviceToken(Long userId, RegisterDeviceTokenRequest request) {
		appUserRepository.findById(userId).orElseThrow(() -> {
			log.warn("Attempt to register device token for non-existent userId={}", userId);
			return new IllegalArgumentException("User does not exist: " + userId);
		});

		deviceTokenRepository.findByToken(request.token()).ifPresent(existing -> {
			log.debug("Deactivating existing device token id={} before re-registering", existing.getId());
			existing.setActive(false);
			deviceTokenRepository.save(existing);
		});

		DeviceToken token = DeviceToken.builder().userId(userId).token(request.token()).platform(request.platform())
				.active(true).build();

		return deviceTokenRepository.save(token);
	}
}
