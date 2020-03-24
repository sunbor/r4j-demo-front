package com.example.properties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;

@Configuration
@ConfigurationProperties(prefix = "circuit-breaker")
@RestController
public class CircuitBreakerCustomConfig {
	
	@RequestMapping(value="test")
	public float testConfig() {
		System.out.println("failure rate threshold: " + failureRateThreshold);
		return failureRateThreshold;
	}
		
	private float failureRateThreshold;
	private float slowCallRateThreshold;
	private Duration slowCallDurationThreshold;
	private int permittedNumberOfCallsInHalfOpenState;
	private int slidingWindowSize;
	private int minimumNumberOfCalls;
	private Duration waitDurationInOpenState;
	private boolean automaticTransitionFromOpenToHalfOpenEnabled;
	
	@Bean
	@ConfigurationProperties(prefix = "circuit-breaker")
	public CircuitBreaker getCb() {
		CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
				.failureRateThreshold(failureRateThreshold)
				.slowCallRateThreshold(slowCallRateThreshold)
				.slowCallDurationThreshold(slowCallDurationThreshold)
				.permittedNumberOfCallsInHalfOpenState(permittedNumberOfCallsInHalfOpenState)
				.slidingWindowSize(slidingWindowSize)
				.minimumNumberOfCalls(minimumNumberOfCalls)
				.waitDurationInOpenState(waitDurationInOpenState)
				.automaticTransitionFromOpenToHalfOpenEnabled(automaticTransitionFromOpenToHalfOpenEnabled)
				.build();
		return CircuitBreaker.of("connectcb", cbConfig);
	}

	public float getFailureRateThreshold() {
		return failureRateThreshold;
	}

	public void setFailureRateThreshold(float failureRateThreshold) {
		this.failureRateThreshold = failureRateThreshold;
	}

	public float getSlowCallRateThreshold() {
		return slowCallRateThreshold;
	}

	public void setSlowCallRateThreshold(float slowCallRateThreshold) {
		this.slowCallRateThreshold = slowCallRateThreshold;
	}

	public Duration getSlowCallDurationThreshold() {
		return slowCallDurationThreshold;
	}

	public void setSlowCallDurationThreshold(Duration slowCallDurationThreshold) {
		this.slowCallDurationThreshold = slowCallDurationThreshold;
	}

	public int getPermittedNumberOfCallsInHalfOpenState() {
		return permittedNumberOfCallsInHalfOpenState;
	}

	public void setPermittedNumberOfCallsInHalfOpenState(int permittedNumberOfCallsInHalfOpenState) {
		this.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
	}

	public int getSlidingWindowSize() {
		return slidingWindowSize;
	}

	public void setSlidingWindowSize(int slidingWindowSize) {
		this.slidingWindowSize = slidingWindowSize;
	}

	public int getMinimumNumberOfCalls() {
		return minimumNumberOfCalls;
	}

	public void setMinimumNumberOfCalls(int minimumNumberOfCalls) {
		this.minimumNumberOfCalls = minimumNumberOfCalls;
	}

	public Duration getWaitDurationInOpenState() {
		return waitDurationInOpenState;
	}

	public void setWaitDurationInOpenState(Duration waitDurationInOpenState) {
		this.waitDurationInOpenState = waitDurationInOpenState;
	}

	public boolean isAutomaticTransitionFromOpenToHalfOpenEnabled() {
		return automaticTransitionFromOpenToHalfOpenEnabled;
	}

	public void setAutomaticTransitionFromOpenToHalfOpenEnabled(boolean automaticTransitionFromOpenToHalfOpenEnabled) {
		this.automaticTransitionFromOpenToHalfOpenEnabled = automaticTransitionFromOpenToHalfOpenEnabled;
	}




	
}
