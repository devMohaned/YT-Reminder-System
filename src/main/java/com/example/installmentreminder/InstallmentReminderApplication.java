package com.example.installmentreminder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;

@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
@SpringBootApplication
public class InstallmentReminderApplication {

	public static void main(String[] args) {
		SpringApplication.run(InstallmentReminderApplication.class, args);
	}
}
