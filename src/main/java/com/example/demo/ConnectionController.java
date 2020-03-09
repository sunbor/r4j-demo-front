package com.example.demo;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import io.github.resilience4j.bulkhead.Bulkhead;
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
	public Object connection(HttpServletRequest req, HttpServletResponse resp) {
		Callable<Object> callable = null;
		if (req.getRequestURL().toString().contains(".png")) {
			callable = () -> graphics(req, resp, "8082");
		} else {
			callable = () -> makeConnection(req, resp, "8082");
		}

		Callable<Object> decoratedCallable = Decorators.ofCallable(callable).withCircuitBreaker(cb).withBulkhead(bh)
				.withRateLimiter(rl).withRetry(rt).decorate();

		Try<Object> result = Try.ofCallable(decoratedCallable);
		return result.get();
	}

	// accesses the other application
	private String makeConnection(HttpServletRequest req, HttpServletResponse resp, String port) {

		HttpGet request = new HttpGet(req.getRequestURL().toString().replaceFirst("8081", port));

		try (CloseableHttpResponse response = httpClient.execute(request)) {

			// Get HttpResponse Status
			System.out.println(response.getStatusLine().toString());

			HttpEntity entity = response.getEntity();
			Header headers = entity.getContentType();
			System.out.println(headers);

			if (entity != null) {
				String result = EntityUtils.toString(entity);
				if (req.getRequestURL().toString().contains(".css")) {
					result = result.replace("'../fonts/",
							"'/home/cdelabs/git/spring-petclinic/src/main/resources/static/resources/fonts/");
					result = result.replace("'../../webjars/bootstrap/fonts/",
							"'/home/cdelabs/git/spring-petclinic/src/main/resources/static/resources/fonts/");
					response.setEntity(EntityBuilder.create().setText(result)
							.setContentType(ContentType.APPLICATION_JSON).build());
				}
				return result;
			}
		} catch (Exception e) {
			makeConnection(req, resp, Integer.toString(Integer.parseInt(port) + 1));
			e.printStackTrace();
		}
		return null;
	}

	private Graphics graphics(HttpServletRequest req, HttpServletResponse resp, String port) throws Exception {

		BufferedImage image = ImageIO.read(new URL(req.getRequestURL().toString().replaceFirst("8081", port)));
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageIO.write(image, "png", os);
		InputStream is = new ByteArrayInputStream(os.toByteArray());

		resp.setContentType(MediaType.IMAGE_PNG_VALUE);
		IOUtils.copy(is, resp.getOutputStream());

		return null;

	}
}
