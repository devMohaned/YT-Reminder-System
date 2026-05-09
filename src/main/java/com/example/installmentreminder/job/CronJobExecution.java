package com.example.installmentreminder.job;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cron_job_executions")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CronJobExecution {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "job_name", nullable = false)
	@Setter
	private String jobName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Setter
	private CronJobStatus status;

	@Column(name = "started_at", nullable = false)
	@Setter
	private LocalDateTime startedAt;

	@Column(name = "finished_at")
	@Setter
	private LocalDateTime finishedAt;

	@Column(name = "total_processed")
	@Setter
	private int totalProcessed;

	@Column(name = "error_message", columnDefinition = "TEXT")
	@Setter
	private String errorMessage;
}
