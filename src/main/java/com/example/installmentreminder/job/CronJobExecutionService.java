package com.example.installmentreminder.job;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.installmentreminder.common.util.MessageUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CronJobExecutionService {

	private final CronJobExecutionRepository cronJobExecutionRepository;

	@Transactional
	public int runJob(String jobName, Supplier<Integer> task) {
		if (jobName == null || jobName.isBlank()) {
			log.error("Rejected cron job run because jobName was blank");
			throw new IllegalArgumentException("jobName must not be blank");
		}

		if (task == null) {
			log.error("Rejected cron job {} because task supplier was null", jobName);
			throw new IllegalArgumentException("task must not be null");
		}

		CronJobExecution execution = CronJobExecution.builder().jobName(jobName).status(CronJobStatus.RUNNING)
				.startedAt(LocalDateTime.now()).build();
		cronJobExecutionRepository.save(execution);

		try {
			int totalProcessed = task.get();

			execution.setStatus(CronJobStatus.SUCCESS);
			execution.setTotalProcessed(totalProcessed);
			execution.setFinishedAt(LocalDateTime.now());

			log.info("Cron job {} finished successfully. totalProcessed={}", jobName, totalProcessed);

			return totalProcessed;
		} catch (Exception ex) {
			execution.setStatus(CronJobStatus.FAILED);
			execution.setFinishedAt(LocalDateTime.now());
			execution.setErrorMessage(MessageUtils.trimToMaxLength(ex.getMessage()));

			log.error("Cron job {} failed during execution", jobName, ex);

			return 0;
		}
	}

	@Transactional(readOnly = true)
	public List<CronJobExecution> findLatestExecutions() {
		return cronJobExecutionRepository.findTop20ByOrderByStartedAtDesc();
	}
}
