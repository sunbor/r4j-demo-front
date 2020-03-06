package com.example.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import io.vavr.control.Try;

@RestController
public class ConnectionController {

	Logger logger = Logger.getLogger(ConnectionController.class);

	CircuitBreaker cb = CircuitBreaker.ofDefaults("connect");
	Bulkhead bh = Bulkhead.ofDefaults("connect");
	RateLimiter rl = RateLimiter.ofDefaults("connect");
	Retry rt = Retry.ofDefaults("connect");

	public ModelAndView redirectWithUsingForwardPrefix(ModelMap model) {
		model.addAttribute("attribute", "forwardWithForwardPrefix");
		return new ModelAndView("forward:/redirectedUrl", model);
	}

	@RequestMapping(value = { "**" })
	public String connection(HttpServletRequest req, HttpServletResponse resp) {

		Callable<String> callable = () -> makeConnection(req, resp);

		Callable<String> decoratedCallable = Decorators.ofCallable(callable).withCircuitBreaker(cb).withBulkhead(bh)
				.withRateLimiter(rl).withRetry(rt).decorate();

		Try<String> result = Try.ofCallable(decoratedCallable);
		return result.get();

	}

	// accesses the other application
	private String makeConnection(HttpServletRequest req, HttpServletResponse resp) throws ConnectException {
		String inputLine = "accessProducer did not work";

		/*
		 * Redirecting: failed because it steps out of circuit breaker and changes URL
		 * Also cannot implement a fallback due to IllegalStateException try {
		 * resp.sendRedirect(req.getRequestURL().toString().replaceFirst("8081",
		 * "8082")); } catch (IOException e1) { System.out.println("IOException"); }
		 */

		/*
		 * Forwarding: failed because URL didn't start with a / Also probably has
		 * problem where IllegalStateException occurs like redirect
		 * 
		 * RequestDispatcher dispatcher = req.getServletContext()
		 * .getRequestDispatcher(req.getRequestURL().toString().replaceFirst("8081",
		 * "8082")); try { dispatcher.include(req, resp); } catch (ServletException |
		 * IOException e1) { e1.printStackTrace(); }
		 */

		/*
		 * Recreating source as String and then compiling: Failed because it cannot
		 * compile images and icons Also doesn't import js functionalities
		 * unfortunately, it's the best choice so far
		 */
		try {
			URL url = new URL(req.getRequestURL().toString().replaceFirst("8081", "8082"));
			System.out.println(req.getRequestURL().toString().replaceFirst("8081", "8082"));
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(req.getMethod());

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
		} catch (ConnectException e) {
			inputLine = Fallback(req, resp);
			logger.error("failed to connect to producer, entering fallback");
		} catch (IOException e) {
			logger.error("ioexception while trying to connect to producer");
			e.printStackTrace();
		}

		return inputLine;
	}

	private String Fallback(HttpServletRequest req, HttpServletResponse resp) {
		String inputLine = "accessProducer did not work";
		URL url;
		try {
			url = new URL(req.getRequestURL().toString().replaceFirst("8081", "8083"));

			System.out.println(req.getRequestURL().toString().replaceFirst("8081", "8083"));
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(req.getMethod());

			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			inputLine = content.toString();
			in.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return inputLine;
	}
}
