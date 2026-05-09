package com.example.installmentreminder.common.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class MessageUtils {

	public static final int DEFAULT_MAX_LENGTH = 1000;

	public static String trimToMaxLength(String message) {
		return trimToMaxLength(message, DEFAULT_MAX_LENGTH);
	}

	public static String trimToMaxLength(String message, int maxLength) {
		if (maxLength <= 0) {
			log.error("Invalid maxLength for trimToMaxLength. maxLength={}", maxLength);
			throw new IllegalArgumentException("maxLength must be greater than zero");
		}

		if (message == null) {
			log.debug("trimToMaxLength called with null message");
			return null;
		}

		if (message.length() <= maxLength) {
			log.debug("Message length within limit. length={}, maxLength={}", message.length(), maxLength);
			return message;
		}

		log.warn("Message exceeded limit and was trimmed. originalLength={}, maxLength={}", message.length(),
				maxLength);
		return message.substring(0, maxLength);
	}
}
