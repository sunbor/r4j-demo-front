package com.example.demo;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;

@Component
@Lazy
public class CircuitBreakerCustomConfig {
		
//	circuitBreaker.failureRateThreshold=10
//			circuitBreaker.slidingWindowSize=50
//			circuitBreaker.waitDurationInOpenState=5000
			
	@Value("${circuitBreaker.failureRateThreshold}")
	private String failureRateThreshold;
	
	@Value("${circuitBreaker.slidingWindowSize}")
	private String slidingWindowSize;
	
	@Value("${circuitBreaker.waitDurationInOpenState}")
	private String waitDurationInOpenState;


	//public final CircuitBreakerConfig cbConfig;
	
//	public final CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
//			.failureRateThreshold(Long.parseLong(failureRateThreshold))
//			.slidingWindowSize(Integer.parseInt(slidingWindowSize))
//			.waitDurationInOpenState(Duration.ofMillis(Long.parseLong(waitDurationInOpenState)))
//			.build();

	@Bean
	public CircuitBreaker getCb() {
		CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
				.failureRateThreshold(Long.parseLong(failureRateThreshold))
				.slidingWindowSize(Integer.parseInt(slidingWindowSize))
				.waitDurationInOpenState(Duration.ofMillis(Long.parseLong(waitDurationInOpenState)))
				.build();
		return CircuitBreaker.of("connectcb", cbConfig);
	}

	public String getFailureRateThreshold() {
		return failureRateThreshold;
	}

	public void setFailureRateThreshold(String failureRateThreshold) {
		this.failureRateThreshold = failureRateThreshold;
	}

	public String getSlidingWindowSize() {
		return slidingWindowSize;
	}

	public void setSlidingWindowSize(String slidingWindowSize) {
		this.slidingWindowSize = slidingWindowSize;
	}

	public String getWaitDurationInOpenState() {
		return waitDurationInOpenState;
	}

	public void setWaitDurationInOpenState(String waitDurationInOpenState) {
		this.waitDurationInOpenState = waitDurationInOpenState;
	}


	
}
