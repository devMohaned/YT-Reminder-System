package com.example.installmentreminder.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

	List<DeviceToken> findByUserIdAndActiveTrue(Long userId);

	Optional<DeviceToken> findByToken(String token);
}
