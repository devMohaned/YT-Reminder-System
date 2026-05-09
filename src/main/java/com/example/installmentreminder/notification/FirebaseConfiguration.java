package com.example.installmentreminder.notification;

import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@ConditionalOnProperty(name = "payment-reminder.push.provider", havingValue = "firebase")
public class FirebaseConfiguration {

	@Bean
	public FirebaseApp firebaseApp(
			@Value("${payment-reminder.firebase.service-account-path}") String serviceAccountPath) throws IOException {
		if (!FirebaseApp.getApps().isEmpty()) {
			log.debug("Reusing existing FirebaseApp instance");
			return FirebaseApp.getInstance();
		}

		if (serviceAccountPath == null || serviceAccountPath.isBlank()) {
			log.error("Firebase configuration invalid: service account path was blank");
			throw new IllegalArgumentException(
					"Firebase service account path is required when push provider is firebase");
		}

		try (FileInputStream serviceAccount = new FileInputStream(serviceAccountPath)) {
			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();

			return FirebaseApp.initializeApp(options);
		} catch (IOException ex) {
			log.error("Integration problem while initializing FirebaseApp. serviceAccountPath={}", serviceAccountPath,
					ex);
			throw ex;
		}
	}

	@Bean
	public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
		return FirebaseMessaging.getInstance(firebaseApp);
	}
}
