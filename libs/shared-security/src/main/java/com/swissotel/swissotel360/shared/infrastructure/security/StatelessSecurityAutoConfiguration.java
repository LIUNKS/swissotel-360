package com.swissotel.swissotel360.shared.infrastructure.security;

import com.swissotel.swissotel360.shared.interfaces.rest.SecurityExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Auto-configuration providing stateless JWT authentication and method security for microservices.
 * Services like IAM or the monolith can supply their own SecurityFilterChain bean to override defaults.
 */
@AutoConfiguration(beforeName = "org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableWebSecurity
@EnableMethodSecurity
public class StatelessSecurityAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(StatelessJwtTokenParser.class)
	public StatelessJwtTokenParser statelessJwtTokenParser(@Value("${authorization.jwt.secret}") String secret) {
		return new StatelessJwtTokenParser(secret);
	}

	@Bean
	@ConditionalOnMissingBean(AuthenticationEntryPoint.class)
	public AuthenticationEntryPoint unauthorizedRequestHandlerEntryPoint() {
		return new UnauthorizedRequestHandlerEntryPoint();
	}

	@Bean
	@ConditionalOnMissingBean(SecurityExceptionHandler.class)
	public SecurityExceptionHandler securityExceptionHandler() {
		return new SecurityExceptionHandler();
	}

	@Bean
	@ConditionalOnMissingBean(BearerTokenForwardingInterceptor.class)
	public BearerTokenForwardingInterceptor bearerTokenForwardingInterceptor(StatelessJwtTokenParser tokenParser) {
		return new BearerTokenForwardingInterceptor(tokenParser);
	}

	@Bean
	@ConditionalOnMissingBean(SecurityFilterChain.class)
	public SecurityFilterChain statelessSecurityFilterChain(
			HttpSecurity http,
			StatelessJwtTokenParser tokenParser,
			AuthenticationEntryPoint unauthorizedRequestHandler) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
				.exceptionHandling(exceptionHandling -> exceptionHandling
						.authenticationEntryPoint(unauthorizedRequestHandler))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(
								"/v3/api-docs/**",
								"/swagger-ui.html",
								"/swagger-ui/**",
								"/swagger-resources/**",
								"/webjars/**",
								"/actuator/health",
								"/actuator/health/**")
						.permitAll()
						.anyRequest().authenticated());
		http.addFilterBefore(new StatelessBearerAuthorizationFilter(tokenParser),
				UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}
