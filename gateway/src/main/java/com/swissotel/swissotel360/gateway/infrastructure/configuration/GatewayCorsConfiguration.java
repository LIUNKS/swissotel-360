package com.swissotel.swissotel360.gateway.infrastructure.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * Centralized CORS handling at the API Gateway edge.
 */
@Configuration
public class GatewayCorsConfiguration {

	@Bean
	public FilterRegistrationBean<CorsFilter> corsFilterRegistration(
			@Value("${cors.allowed-origins:http://localhost:4200,http://localhost:3000,http://localhost:8080}") String corsAllowedOrigins) {
		var allowedOriginPatterns = Arrays.stream(corsAllowedOrigins.split(","))
				.map(String::trim)
				.filter(origin -> !origin.isBlank())
				.toList();

		var cors = new CorsConfiguration();
		cors.setAllowedOriginPatterns(allowedOriginPatterns);
		cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
		cors.setAllowedHeaders(List.of("*"));
		cors.setAllowCredentials(true);
		cors.setMaxAge(3600L);

		var source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", cors);

		var registration = new FilterRegistrationBean<>(new CorsFilter(source));
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return registration;
	}
}
