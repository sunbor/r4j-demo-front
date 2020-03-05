package com.example.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "bulkhead")
public class BulkheadCustomConfig {

	private String maxConcurrentCalls;
	private String maxWaitDuration;
	public String getMaxConcurrentCalls() {
		return maxConcurrentCalls;
	}
	public void setMaxConcurrentCalls(String maxConcurrentCalls) {
		this.maxConcurrentCalls = maxConcurrentCalls;
	}
	public String getMaxWaitDuration() {
		return maxWaitDuration;
	}
	public void setMaxWaitDuration(String maxWaitDuration) {
		this.maxWaitDuration = maxWaitDuration;
	}
	
	
}
