package com.example.properties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;

//@RestController
@Configuration
//@Component
@ConfigurationProperties(prefix = "bulkhead")
public class BulkheadCustomConfig {

	private int maxConcurrentCalls;
	private Duration maxWaitDuration;
	
	public int getMaxConcurrentCalls() {
		return maxConcurrentCalls;
	}


	public void setMaxConcurrentCalls(int maxConcurrentCalls) {
		this.maxConcurrentCalls = maxConcurrentCalls;
	}


	public Duration getMaxWaitDuration() {
		return maxWaitDuration;
	}


	public void setMaxWaitDuration(Duration maxWaitDuration) {
		this.maxWaitDuration = maxWaitDuration;
	}


	@Bean
	@ConfigurationProperties(prefix = "bulkhead")
	public Bulkhead getBulkhead() {
		BulkheadConfig bhConfig = BulkheadConfig.custom()
				.maxConcurrentCalls(maxConcurrentCalls)
				.maxWaitDuration(maxWaitDuration)
				.build();
		return Bulkhead.of("connectbh", bhConfig);
	}
	
	
}
