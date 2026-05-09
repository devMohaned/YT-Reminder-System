package com.example.installmentreminder.notification;

import java.time.LocalDateTime;
import java.util.Collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

	boolean existsByIdempotencyKey(String idempotencyKey);

	@Query("""
			SELECT n
			FROM NotificationOutbox n
			WHERE n.status IN :statuses
			  AND n.retryCount < :maxRetryCount
			  AND (n.nextAttemptAt IS NULL OR n.nextAttemptAt <= :now)
			ORDER BY n.createdAt ASC
			""")
	Page<NotificationOutbox> findRetryableNotifications(@Param("statuses") Collection<NotificationStatus> statuses,
			@Param("maxRetryCount") int maxRetryCount, @Param("now") LocalDateTime now, Pageable pageable);
}
