package com.example.installmentreminder.job;

import java.time.LocalDateTime;

public record CronJobExecutionResponse(Long id, String jobName, CronJobStatus status, LocalDateTime startedAt,
		LocalDateTime finishedAt, int totalProcessed, String errorMessage) {
	public static CronJobExecutionResponse from(CronJobExecution execution) {
		return new CronJobExecutionResponse(execution.getId(), execution.getJobName(), execution.getStatus(),
				execution.getStartedAt(), execution.getFinishedAt(), execution.getTotalProcessed(),
				execution.getErrorMessage());
	}
}
