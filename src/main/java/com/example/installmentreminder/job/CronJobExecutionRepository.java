package com.example.installmentreminder.job;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CronJobExecutionRepository extends JpaRepository<CronJobExecution, Long> {

	List<CronJobExecution> findTop20ByOrderByStartedAtDesc();
}
