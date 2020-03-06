package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Component
@Setter
@Getter
@NoArgsConstructor
public class Config {

	@Value("${config.primary.endpoint}")
	private String primaryEndpoint;

	@Value("${config.secondary.endpoint}")
	private String secondaryEndpoint;

	public String getServerPort() {
		return System.getProperty("server.port");
	}
}
