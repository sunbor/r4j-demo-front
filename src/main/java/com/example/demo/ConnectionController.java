package com.example.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;

@Lazy
@RestController

public class ConnectionController {
	
	Logger logger = Logger.getLogger(ConnectionController.class);
	
//	@Lazy
//	@Autowired
//	private CircuitBreakerCustomConfig cbcc;
	
//	@Lazy
//	@Autowired
//	private BulkheadCustomConfig bhcc;
		
	@Lazy
	@Autowired
	CircuitBreaker cb;
	
	@Lazy
	@Autowired
	Bulkhead bh;
	
	@Lazy
	@Autowired
	RateLimiter rl;
	
	@Lazy
	@Autowired
	Retry rt;


	@Value("${test.test}")
	private String test;
	
//	@Value("${circuitBreaker.failureRateThreshold}")
//	private String cbfrt;
//	
//	@Value("${circuitBreaker.waitDurationInOpenState}")
//	private String cbwfios;
	
	@RequestMapping("/connect")

	public String connection() {
		
		Callable<String> callable = () -> makeConnection();
		
		Callable<String> decoratedCallable = Decorators.ofCallable(callable)
				.withCircuitBreaker(cb)
//				.withCircuitBreaker(cbcc.getCb())
				.withBulkhead(bh)
				.withRateLimiter(rl)
				.withRetry(rt)
				.decorate();
		
//		cbcc.getCb().getEventPublisher()
//			.onSuccess(event -> logger.trace("circuit breaker successful call"))
//			.onError(event -> logger.trace("circuit breaker error"))
//			.onIgnoredError(event -> logger.trace("ignored event, not completely sure what this is"))
//			.onReset(event -> logger.trace("circuit breaker reset"))
//			.onStateTransition(event -> logger.trace("circuit breaker state transition occurred"));
		
//		cbcc.getCb().getEventPublisher().onEvent(event -> logger.info("event occurred: " + event.getClass()));
					
		
//		logger.trace("bulkhead: " + bhcc.getMaxConcurrentCalls());
//		logger.trace("test: " + test);
//		logger.trace("cbfrt: " + Long.parseLong(cbfrt));
//		logger.trace("cbwfios: " + Long.parseLong(cbwfios));
//		logger.trace("failure rate threshold: " + cbcc.getFailureRateThreshold());

		String result = "this FAILED";
		try {
			result = decoratedCallable.call();
		}
		catch(ConnectException e) {
			logger.error("connection failed, from inside try catch");
		}
		catch(CallNotPermittedException e) {
			logger.error("circuit breaker opened");
		}
		catch(Exception e) {
			logger.error("some other exception occurred");
			e.printStackTrace();
		}
		return "this is the front end app: " + result;
		
//		Try<String> result = Try.ofCallable(decoratedCallable).recover(throwable -> "Hello from Recovery");
//
//		return result.get();
								
	}
	
	// accesses the other application
	private String makeConnection() throws ConnectException {

		String inputLine = "accessProducer did not work";
		try {URL url = new URL("http://localhost:8082/test");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			inputLine = content.toString();
			in.close();

		} catch (MalformedURLException e) {
			logger.error("url format error while trying to connect to producer");
			e.printStackTrace();
		}
		catch (ConnectException e) {
			logger.error("failed to connect to producer");
			throw e;
			// e.printStackTrace();
		} catch (IOException e) {
			logger.error("ioexception while trying to connect to producer");
			e.printStackTrace();
		}

		//logger.trace("producer output: " + inputLine);
		return inputLine;
	}
}
