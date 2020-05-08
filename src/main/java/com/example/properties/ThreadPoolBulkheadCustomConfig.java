package com.example.properties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;

@Configuration
@ConfigurationProperties(prefix = "thread-pool-bulkhead")
public class ThreadPoolBulkheadCustomConfig {

	private int maxThreadPoolSize;
	private int coreThreadPoolSize;
	private int queueCapacity=5;
	private Duration keepAliveDuration;
	public int getMaxThreadPoolSize() {
		return maxThreadPoolSize;
	}
	public void setMaxThreadPoolSize(int maxThreadPoolSize) {
		this.maxThreadPoolSize = maxThreadPoolSize;
	}
	public int getCoreThreadPoolSize() {
		return coreThreadPoolSize;
	}
	public void setCoreThreadPoolSize(int coreThreadPoolSize) {
		this.coreThreadPoolSize = coreThreadPoolSize;
	}
	public int getQueueCapacity() {
		return queueCapacity;
	}
	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}
	public Duration getKeepAliveDuration() {
		return keepAliveDuration;
	}
	public void setKeepAliveDuration(Duration keepAliveDuration) {
		this.keepAliveDuration = keepAliveDuration;
	}
	
	@Bean
	@ConfigurationProperties(prefix = "thread-pool-bulkhead")
	public ThreadPoolBulkhead getThreadPoolBulkhead() {
		ThreadPoolBulkheadConfig tpbhConfig = ThreadPoolBulkheadConfig.custom()
				.maxThreadPoolSize(maxThreadPoolSize)
				.coreThreadPoolSize(coreThreadPoolSize)
				.queueCapacity(queueCapacity)
				.keepAliveDuration(keepAliveDuration)
				.build();
		return ThreadPoolBulkhead.of("connecttpbh", tpbhConfig);
	}

}
