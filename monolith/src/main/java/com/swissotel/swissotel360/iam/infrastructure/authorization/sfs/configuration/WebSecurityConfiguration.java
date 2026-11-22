package com.swissotel.swissotel360.iam.infrastructure.authorization.sfs.configuration;

import com.swissotel.swissotel360.iam.infrastructure.hashing.bcrypt.BCryptHashingService;
import com.swissotel.swissotel360.shared.infrastructure.security.StatelessBearerAuthorizationFilter;
import com.swissotel.swissotel360.shared.infrastructure.security.StatelessJwtTokenParser;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;

/**
 * Web Security Configuration.
 * <p>
 * This class is responsible for configuring the web security.
 * It enables the method security and configures the security filter chain.
 * It includes the authentication manager, the authentication provider, the
 * password encoder and the authentication entry point.
 * </p>
 */
@Configuration
@EnableMethodSecurity
public class WebSecurityConfiguration {

	private final UserDetailsService userDetailsService;

	private final StatelessJwtTokenParser tokenParser;

	private final BCryptHashingService hashingService;

	private final AuthenticationEntryPoint unauthorizedRequestHandler;

	private final List<String> allowedOriginPatterns;

	/**
	 * This method creates the authentication manager.
	 *
	 * @param authenticationConfiguration
	 *                                        The
	 *                                        {@link AuthenticationConfiguration}
	 *                                        object with the authentication
	 *                                        configuration
	 * @return The {@link AuthenticationManager} instance from the authentication
	 *         configuration
	 *
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	/**
	 * This method creates the authentication provider.
	 *
	 * @return The {@link DaoAuthenticationProvider} authentication provider with
	 *         the user details service and the password encoder
	 */
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		var authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
		authenticationProvider.setPasswordEncoder(hashingService);
		return authenticationProvider;
	}

	/**
	 * This method creates the password encoder.
	 *
	 * @return The {@link PasswordEncoder} instance with the hashing service
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return hashingService;
	}

	/**
	 * This method creates the security filter chain.
	 * It also configures the http security.
	 *
	 * @param http
	 *                 The {@link HttpSecurity} object to configure with the
	 *                 security
	 *                 filter chain
	 * @return The {@link SecurityFilterChain} instance with the application http
	 *         security configuration
	 */
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.cors(configurer -> configurer.configurationSource(_ -> {
			var cors = new CorsConfiguration();
			cors.setAllowedOriginPatterns(allowedOriginPatterns);
			cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
			cors.setAllowedHeaders(List.of("*"));
			cors.setAllowCredentials(true);
			return cors;
		}));
		http.csrf(csrfConfigurer -> csrfConfigurer.disable())
				.exceptionHandling(
						exceptionHandling -> exceptionHandling.authenticationEntryPoint(unauthorizedRequestHandler))
				.sessionManagement(customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorizeRequests -> authorizeRequests
						.requestMatchers(
								"/api/v1/authentication/sign-in",
								"/api/v1/authentication/sign-up",
								"/api/v1/authentication/sign-out",
								"/v3/api-docs/**",
								"/swagger-ui.html",
								"/swagger-ui/**",
								"/swagger-resources/**",
								"/webjars/**",
								"/actuator/health",
								"/actuator/health/**")
						.permitAll()
						.anyRequest().authenticated());
		http.authenticationProvider(authenticationProvider());
		http.addFilterBefore(new StatelessBearerAuthorizationFilter(tokenParser),
				UsernamePasswordAuthenticationFilter.class);
		return http.build();

	}

	/**
	 * This is the constructor of the class.
	 *
	 * @param userDetailsService
	 *                                     The user details service
	 * @param tokenParser
	 *                                     The stateless JWT token parser
	 * @param hashingService
	 *                                     The hashing service
	 * @param authenticationEntryPoint
	 *                                     The authentication entry point
	 * @param corsAllowedOrigins
	 *                                     Comma-separated allowed origin patterns for CORS
	 */
	public WebSecurityConfiguration(@Qualifier("defaultUserDetailsService") UserDetailsService userDetailsService,
			StatelessJwtTokenParser tokenParser, BCryptHashingService hashingService,
			AuthenticationEntryPoint authenticationEntryPoint,
			@Value("${cors.allowed-origins:http://localhost:4200,http://localhost:3000,http://localhost:8080}") String corsAllowedOrigins) {
		this.userDetailsService = userDetailsService;
		this.tokenParser = tokenParser;
		this.hashingService = hashingService;
		this.unauthorizedRequestHandler = authenticationEntryPoint;
		this.allowedOriginPatterns = Arrays.stream(corsAllowedOrigins.split(","))
				.map(String::trim)
				.filter(origin -> !origin.isBlank())
				.toList();
	}
}
