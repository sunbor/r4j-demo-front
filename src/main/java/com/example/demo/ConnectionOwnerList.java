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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import io.vavr.control.Try;

@RestController
public class ConnectionOwnerList {
	
	Logger logger = Logger.getLogger(ConnectionOwnerList.class);
	
	CircuitBreaker cb = CircuitBreaker.ofDefaults("connect");
	Bulkhead bh = Bulkhead.ofDefaults("connect");
	RateLimiter rl = RateLimiter.ofDefaults("connect");
	Retry rt = Retry.ofDefaults("connect");

	@RequestMapping("/owners/find")
	public String connection() {
		
		Callable<String> callable = () -> makeConnection();
		
		Callable<String> decoratedCallable = Decorators.ofCallable(callable)
				.withCircuitBreaker(cb)
				.withBulkhead(bh)
				.withRateLimiter(rl)
				.withRetry(rt)
				.decorate();
		
		Try<String> result = Try.ofCallable(decoratedCallable);
		return result.get();
	}
	
	// accesses the other application
	private String makeConnection() throws ConnectException {

		String inputLine = "accessProducer did not work";
		try {URL url = new URL("http://localhost:8082/owners/find");
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

		logger.trace("producer output: " + inputLine);
		return inputLine;
	}
}
