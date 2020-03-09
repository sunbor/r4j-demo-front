package com.example.properties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

@Configuration
@ConfigurationProperties(prefix = "retry")
public class RetryCustomConfig {
	
	private int maxAttempts;
	private Duration waitDuration;
	
	@Bean
	@ConfigurationProperties(prefix = "retry")
	public Retry getRt() {
		RetryConfig rtConfig = RetryConfig.custom()
				.maxAttempts(maxAttempts)
				.waitDuration(waitDuration)
				.build();
		return Retry.of("connectrt", rtConfig);
	}

	public int getMaxAttempts() {
		return maxAttempts;
	}

	public void setMaxAttempts(int maxAttempts) {
		this.maxAttempts = maxAttempts;
	}

	public Duration getWaitDuration() {
		return waitDuration;
	}

	public void setWaitDuration(Duration waitDuration) {
		this.waitDuration = waitDuration;
	}

	
	

}
