package com.example.properties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;

@Configuration
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterCustomConfig {

	private Duration limitRefreshPeriod;
	private int limitForPeriod;
	private Duration timeoutDuration;
	
	@Bean
	@ConfigurationProperties(prefix = "rate-limiter")
	public RateLimiter getRl() {
		RateLimiterConfig rlConfig = RateLimiterConfig.custom()
				.limitRefreshPeriod(limitRefreshPeriod)
				.limitForPeriod(limitForPeriod)
				.timeoutDuration(timeoutDuration)
				.build();
		return RateLimiter.of("connectrl", rlConfig);
	}

	public Duration getLimitRefreshPeriod() {
		return limitRefreshPeriod;
	}

	public void setLimitRefreshPeriod(Duration limitRefreshPeriod) {
		this.limitRefreshPeriod = limitRefreshPeriod;
	}

	public int getLimitForPeriod() {
		return limitForPeriod;
	}

	public void setLimitForPeriod(int limitForPeriod) {
		this.limitForPeriod = limitForPeriod;
	}

	public Duration getTimeoutDuration() {
		return timeoutDuration;
	}

	public void setTimeoutDuration(Duration timeoutDuration) {
		this.timeoutDuration = timeoutDuration;
	}


	
	
}
