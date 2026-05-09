package com.example.installmentreminder.user;

import org.springframework.web.bind.annotation.*;

import com.example.installmentreminder.common.ApiResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping
	public ApiResponse<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
		AppUser user = userService.createUser(request);
		return ApiResponse.ok("User created", UserResponse.from(user));
	}

	@PostMapping("/{userId}/device-tokens")
	public ApiResponse<DeviceTokenResponse> registerDeviceToken(@PathVariable @NotNull Long userId,
			@Valid @RequestBody RegisterDeviceTokenRequest request) {
		DeviceToken token = userService.registerDeviceToken(userId, request);
		return ApiResponse.ok("Device token registered", DeviceTokenResponse.from(token));
	}
}
