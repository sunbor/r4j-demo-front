package com.example.properties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

@Configuration
@ConfigurationProperties(prefix = "time-limiter")
public class TimeLimiterCustomConfig {

	private boolean cancelRunningFuture;
	private Duration timeoutDuration;
	
	@Bean
	@ConfigurationProperties(prefix = "time-limiter")
	public TimeLimiter getTl() {
		TimeLimiterConfig tlConfig = TimeLimiterConfig.custom()
				.cancelRunningFuture(cancelRunningFuture)
				.timeoutDuration(timeoutDuration)
				.build();
		return TimeLimiter.of("connecttl", tlConfig);
	}

	public boolean isCancelRunningFuture() {
		return cancelRunningFuture;
	}

	public void setCancelRunningFuture(boolean cancelRunningFuture) {
		this.cancelRunningFuture = cancelRunningFuture;
	}

	public Duration getTimeoutDuration() {
		return timeoutDuration;
	}

	public void setTimeoutDuration(Duration timeoutDuration) {
		this.timeoutDuration = timeoutDuration;
	}
	
	
}
