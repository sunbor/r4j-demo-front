package com.example.demo;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;

@Configuration
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterCustomConfig {

	private String limitRefreshPeriod;
	private String limitForPeriod;
	private String timeoutDuration;
	
	@Bean
	@ConfigurationProperties(prefix = "rate-limiter")
	public RateLimiter getRl() {
		RateLimiterConfig rlConfig = RateLimiterConfig.custom()
				.limitRefreshPeriod(Duration.ofMillis(Long.parseLong(limitRefreshPeriod)))
				.limitForPeriod(Integer.parseInt(limitForPeriod))
				.timeoutDuration(Duration.ofMillis(Long.parseLong(timeoutDuration)))
				.build();
		return RateLimiter.of("connectrl", rlConfig);
	}

	public String getLimitRefreshPeriod() {
		return limitRefreshPeriod;
	}

	public void setLimitRefreshPeriod(String limitRefreshPeriod) {
		this.limitRefreshPeriod = limitRefreshPeriod;
	}

	public String getLimitForPeriod() {
		return limitForPeriod;
	}

	public void setLimitForPeriod(String limitForPeriod) {
		this.limitForPeriod = limitForPeriod;
	}

	public String getTimeoutDuration() {
		return timeoutDuration;
	}

	public void setTimeoutDuration(String timeoutDuration) {
		this.timeoutDuration = timeoutDuration;
	}
	
	
}
