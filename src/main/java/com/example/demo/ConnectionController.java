package com.example.demo;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.example.utility.ThrowingConsumerWrapper;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreaker.StateTransition;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.vavr.control.Try;

@Lazy
@RestController
public class ConnectionController {

	Logger logger = Logger.getLogger(ConnectionController.class);

	// one instance, reuse
	RequestConfig config = RequestConfig.custom()
		.setConnectTimeout(500)
		.setConnectionRequestTimeout(500)
		.setSocketTimeout(300).build();
	private final CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();;

	static int port = 8082;
	static int port1 = 8082;
	static int port2 = 8083;

	@Autowired
	CircuitBreaker cb;

	@Autowired
	Bulkhead bh;
	
	@Autowired
	ThreadPoolBulkhead tpbh;

	@Autowired
	RateLimiter rl;

	@Autowired
	Retry rt;
	
	@Autowired
	TimeLimiter tl;

	public ModelAndView redirectWithUsingForwardPrefix(ModelMap model) {
		model.addAttribute("attribute", "forwardWithForwardPrefix");
		return new ModelAndView("forward:/redirectedUrl", model);
	}

	@RequestMapping(value = { "**" })
	public Object connection(HttpServletRequest req, HttpServletResponse resp,
			@RequestParam(value = "lastName", required = false) String lastName) //throws CallNotPermittedException, ConnectException, Exception
	{
		Callable<Object> callable = null;
		//callable = () -> Dispatcher(req, resp, lastName, port1);
		callable = () -> timeLimiterTest(req, resp, lastName, port1);

		Callable<Object> decoratedCallable = Decorators.ofCallable(callable)
				.withCircuitBreaker(cb)
				.withBulkhead(bh)
				//.withThreadPoolBulkhead(tpbh)
				.withRateLimiter(rl)
				.withRetry(rt)
				.withFallback(Exception.class, throwable -> {
					logger.trace("inside fallback");
					try {
						return timeLimiterTest(req, resp, lastName, port2);
						//return Dispatcher(req, resp, lastName, port2);
					} catch (ConnectException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return throwable;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return throwable;
					}
				})
				.decorate();
		//Try<Object> fallbackTest = Try.ofCallable(decoratedCallable).recover(f);


		Callable<Object> decoratedCallable = Decorators.ofCallable(callable).withCircuitBreaker(cb).withBulkhead(bh)
		.withRateLimiter(rl).withRetry(rt).decorate();
		// Try<Object> result = Try.ofCallable(decoratedCallable);
		Object result = null;

		cb.getEventPublisher().onStateTransition(event -> {
			logger.trace("state transition: " + event.getStateTransition());
			if (event.getStateTransition() == StateTransition.HALF_OPEN_TO_CLOSED) {
				port = 8082;
				logger.trace("circuit breaker has been closed");
			}
			if (event.getStateTransition() == StateTransition.CLOSED_TO_OPEN) {
				port = 8083;
				logger.trace("circuit breaker opened");
			}
		});
		Object result = null;
		try {
			result = decoratedCallable.call();
		} catch (CallNotPermittedException e) {
			logger.error("circuit breaker is open");
			resp.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			//throw e;
		} catch (ConnectException e) {
			logger.error("CBException: connection failed, from inside try catch");
			resp.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			//throw e;
		} catch (Exception e) {
			logger.error("CBException: some other exception occurred");
			e.printStackTrace();
			resp.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			//throw e;
		}
		return result;
	}

	//@GetMapping("/oups")
	private String timeLimiterTest(HttpServletRequest req, HttpServletResponse resp, String lastName, int port) throws Exception {
		
//		CompletableFuture<String> completableFuture = new CompletableFuture<>();
//		completableFuture.complete("test");
//		completableFuture = CompletableFuture.supplyAsync(supplier).exceptionally(exception -> "send help");

		//make supplier object that accesses dispatcher
//		Supplier<String> mostBasicSupplier = () -> testDispatcher("most basic supplier test");
//		Supplier<String> supplierTest = ThrowingConsumerWrapper.wrapFunction( () -> testDispatcher("supplier test"));
//		logger.trace("supplier contents: " + supplierTest.get());
		Supplier<String> supplier = ThrowingConsumerWrapper.wrapFunction( () -> Dispatcher(req, resp, lastName, port) );
				
		//wrap supplier in future
		Supplier<CompletableFuture<String>> supplierF = () -> CompletableFuture.supplyAsync(supplier);
		
//		String futureTest = completableFuture.get();
//		
//		logger.trace("hi the future test shows " + futureTest);
		
		//create time limiter object
		//TimeLimiter timeLimiter = TimeLimiter.of(Duration.ofSeconds(1));
//		
//		Callable<String> callable = () -> Dispatcher(req, resp, lastName, port1);
//		Supplier<CompletableFuture> supplier = () -> Dispatcher(req, resp, lastName, port1);
//
		
		//create result object with default result
		String result = "default time limiter result";
		//decorate a supplier with a time limiter
		Callable<String> decoratedSupplier = tl.decorateFutureSupplier(supplierF);
		//assign result to result object
		result = decoratedSupplier.call();
		logger.trace("result from time limiter: " + result);
		
		return result;
	}
	
//	public String testDispatcher(String testString) {
//		return testString;
//	}
	
	private String Dispatcher(HttpServletRequest req, HttpServletResponse resp, String lastName, int port)
			throws ConnectException {

		String result = null;
		if (req.getRequestURL().toString().contains(".png")) {
			result = graphics(req, resp, Integer.toString(port));
		} else {
			result = makeConnection(req, resp, Integer.toString(port), lastName);
		}
		if (result == null) {
			throw new ConnectException();
		}
		if (resp.getStatus() == 200)
			logger.trace("connected to port " + (port - 1));
		return result;
	}

	// accesses the other application
	private String makeConnection(HttpServletRequest req, HttpServletResponse resp, String port, String lastName) {
		logger.trace(port);

		HttpGet request = new HttpGet(req.getRequestURL().toString().replaceFirst("8081", port));
		if (lastName != null) {
			request = new HttpGet(req.getRequestURL().toString().replaceFirst("8081", port) + "?LastName=" + lastName);
		}
		
		try (CloseableHttpResponse response = httpClient.execute(request)) {

			// Get HttpResponse Status
			logger.trace(response.getStatusLine().toString());
			//System.out.println(1);
			HttpEntity entity = response.getEntity();
			Header headers = entity.getContentType();
			logger.trace(headers);

			if (entity != null) {
				String result = EntityUtils.toString(entity);
				if (req.getRequestURL().toString().contains(".css")) {
					result = result.replace("'../fonts/", "'localhost:8081/fonts/");
					result = result.replace("'../../webjars/bootstrap/fonts/", "'localhost:8081/fonts/");
					response.setEntity(EntityBuilder.create().setText(result)
							.setContentType(ContentType.APPLICATION_JSON).build());
				}
				return result;
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return null;
	}

	private String graphics(HttpServletRequest req, HttpServletResponse resp, String port) {
		try {
			BufferedImage image = ImageIO.read(new URL(req.getRequestURL().toString().replaceFirst("8081", port)));
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(image, "png", os);
			InputStream is = new ByteArrayInputStream(os.toByteArray());

			resp.setContentType(MediaType.IMAGE_PNG_VALUE);
			IOUtils.copy(is, resp.getOutputStream());
			return "done";
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return null;
	}
}
