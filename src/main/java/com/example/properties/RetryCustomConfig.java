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
	
	private String maxAttempts;
	private String waitDuration;
	
	@Bean
	@ConfigurationProperties(prefix = "retry")
	public Retry getRt() {
		RetryConfig rtConfig = RetryConfig.custom()
				.maxAttempts(Integer.parseInt(maxAttempts))
				.waitDuration(Duration.ofMillis(Long.parseLong(waitDuration)))
				.build();
		return Retry.of("connectrt", rtConfig);
	}

	public String getMaxAttempts() {
		return maxAttempts;
	}

	public void setMaxAttempts(String maxAttempts) {
		this.maxAttempts = maxAttempts;
	}

	public String getWaitDuration() {
		return waitDuration;
	}

	public void setWaitDuration(String waitDuration) {
		this.waitDuration = waitDuration;
	}
	
	

}
