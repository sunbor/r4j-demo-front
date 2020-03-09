package com.example.properties;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;

//@Component
//@Lazy
@Configuration
@ConfigurationProperties(prefix = "circuit-breaker")
public class CircuitBreakerCustomConfig {
		
//	circuitBreaker.failureRateThreshold=10
//			circuitBreaker.slidingWindowSize=50
//			circuitBreaker.waitDurationInOpenState=5000
			
//	@Value("${circuitBreaker.failureRateThreshold}")
	private float failureRateThreshold;
	
//	@Value("${circuitBreaker.slidingWindowSize}")
	private int slidingWindowSize;
	
//	@Value("${circuitBreaker.waitDurationInOpenState}")
	private Duration waitDurationInOpenState;
	

	//public final CircuitBreakerConfig cbConfig;
	
//	public final CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
//			.failureRateThreshold(Long.parseLong(failureRateThreshold))
//			.slidingWindowSize(Integer.parseInt(slidingWindowSize))
//			.waitDurationInOpenState(Duration.ofMillis(Long.parseLong(waitDurationInOpenState)))
//			.build();
	
	@Bean
	@ConfigurationProperties(prefix = "circuit-breaker")
	public CircuitBreaker getCb() {
		CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
				.failureRateThreshold(failureRateThreshold)
				.slidingWindowSize(slidingWindowSize)
				.waitDurationInOpenState(waitDurationInOpenState)
				.build();
		return CircuitBreaker.of("connectcb", cbConfig);
	}


	public float getFailureRateThreshold() {
		return failureRateThreshold;
	}


	public void setFailureRateThreshold(float failureRateThreshold) {
		this.failureRateThreshold = failureRateThreshold;
	}


	public int getSlidingWindowSize() {
		return slidingWindowSize;
	}


	public void setSlidingWindowSize(int slidingWindowSize) {
		this.slidingWindowSize = slidingWindowSize;
	}


	public Duration getWaitDurationInOpenState() {
		return waitDurationInOpenState;
	}


	public void setWaitDurationInOpenState(Duration waitDurationInOpenState) {
		this.waitDurationInOpenState = waitDurationInOpenState;
	}




	
}
