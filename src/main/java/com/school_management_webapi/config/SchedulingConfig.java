package com.school_management_webapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Turns on the nightly jobs in ScheduledJobs: billing renewal and academic rollover. */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
