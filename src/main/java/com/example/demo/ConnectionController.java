package com.example.demo;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URL;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import io.vavr.control.Try;

@Lazy
@RestController
public class ConnectionController {

	Logger logger = Logger.getLogger(ConnectionController.class);

	// one instance, reuse
	private final CloseableHttpClient httpClient = HttpClients.createDefault();

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
	

	public ModelAndView redirectWithUsingForwardPrefix(ModelMap model) {
		model.addAttribute("attribute", "forwardWithForwardPrefix");
		return new ModelAndView("forward:/redirectedUrl", model);
	}

	@RequestMapping(value = { "**" })
	public Object connection(HttpServletRequest req, HttpServletResponse resp,
			@RequestParam(value = "lastName", required = false) String lastName) {
		Callable<Object> callable = null;
		callable = () -> Dispatcher(req, resp, lastName);

		Callable<Object> decoratedCallable = Decorators.ofCallable(callable).withCircuitBreaker(cb).withBulkhead(bh)
				.withRateLimiter(rl).withRetry(rt).decorate();
		//Try<Object> result = Try.ofCallable(decoratedCallable);
		Object result = null;
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
		return result;
	}

	private String Dispatcher(HttpServletRequest req, HttpServletResponse resp, String lastName) {
		int port = 8082;
		String result = null;
		while (result == null) {
			if (req.getRequestURL().toString().contains(".png")) {
				result = graphics(req, resp, Integer.toString(port));
			} else {
				result = makeConnection(req, resp, Integer.toString(port), lastName);
			}
			port++;
		}
		System.out.println("connected to port " + (port-1));
		return result;
	}

	// accesses the other application
	private String makeConnection(HttpServletRequest req, HttpServletResponse resp, String port, String lastName) {
		System.out.println(port);

		HttpGet request = new HttpGet(req.getRequestURL().toString().replaceFirst("8081", port));
		if (lastName != null) {
			request = new HttpGet(req.getRequestURL().toString().replaceFirst("8081", port) + "?LastName=" + lastName);
		}

		try (CloseableHttpResponse response = httpClient.execute(request)) {

			// Get HttpResponse Status
			System.out.println(response.getStatusLine().toString());

			HttpEntity entity = response.getEntity();
			Header headers = entity.getContentType();
			System.out.println(headers);

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
