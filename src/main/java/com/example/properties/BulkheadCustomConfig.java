package com.example.properties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.BulkheadTestClass;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;

//@RestController
@Configuration
//@Component
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
	
//	@RequestMapping("/bh")
//	public String bhPath() {
//		return maxConcurrentCalls;
//	}
//	
//	@Bean
//	public BulkheadTestClass bhTestMaker() {
//		BulkheadTestClass bhTest = new BulkheadTestClass(maxConcurrentCalls, maxWaitDuration);
//		return bhTest;
//	}
	
//	@Configuration
//	public class ConfigProperties {
//	 
//	    @Bean
//	    @ConfigurationProperties(prefix = "item")
//	    public Item item() {
//	        return new Item();
//	    }
//	}
	@Bean
	@ConfigurationProperties(prefix = "bulkhead")
	public BulkheadTestClass bhTest() {
		BulkheadTestClass bhTest = new BulkheadTestClass(maxConcurrentCalls, maxWaitDuration);
		return bhTest;
	}
	
	@Bean
	@ConfigurationProperties(prefix = "bulkhead")
	public Bulkhead getBulkhead() {
		BulkheadConfig bhConfig = BulkheadConfig.custom()
				.maxConcurrentCalls(Integer.parseInt(maxConcurrentCalls))
				.maxWaitDuration(Duration.ofMillis(Long.parseLong(maxWaitDuration)))
				.build();
		return Bulkhead.of("connectbh", bhConfig);
	}
	
	
}
