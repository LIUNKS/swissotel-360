package com.swissotel.swissotel360.shared.infrastructure.documentation.openapi.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configures the OpenAPI specification exposed by the platform.
 */
@Configuration
public class OpenApiConfiguration {

	private final String applicationName;
	private final String applicationDescription;
	private final String applicationVersion;

	public OpenApiConfiguration(
			@Value("${spring.application.name}") String applicationName,
			@Value("${documentation.application.description}") String applicationDescription,
			@Value("${documentation.application.version}") String applicationVersion) {
		this.applicationName = applicationName;
		this.applicationDescription = applicationDescription;
		this.applicationVersion = applicationVersion;
	}

	/**
	 * Builds the OpenAPI document used by Swagger UI and client generation tools.
	 *
	 * @return configured OpenAPI descriptor
	 */
	@Bean
	public OpenAPI swissotelOpenApi() {
		var openApi = new OpenAPI();
		openApi
				.info(new Info()
						.title(this.applicationName)
						.description(this.applicationDescription)
						.version(this.applicationVersion)
						.contact(new Contact()
								.name("Swissotel360 Support")
								.email("support@swissotel360.com")
								.url("https://swissotel360.com/support"))
						.license(new License()
								.name("Apache 2.0")
								.url("https://www.apache.org/licenses/LICENSE-2.0.html")))
				.externalDocs(new ExternalDocumentation()
						.description("Swissotel360 Documentation")
						.url("https://swissotel360.com/docs"));

		// Add server configurations
		openApi.servers(List.of(
				new Server()
						.url("http://localhost:8080")
						.description("Local Development Environment")));

		// Add a security scheme
		final String securitySchemeName = "bearerAuth";

		openApi.addSecurityItem(new SecurityRequirement()
				.addList(securitySchemeName))
				.components(new Components()
						.addSecuritySchemes(securitySchemeName,
								new SecurityScheme()
										.name(securitySchemeName)
										.type(SecurityScheme.Type.HTTP)
										.scheme("bearer")
										.bearerFormat("JWT")
										.description("JWT Bearer token for API authentication")));

		return openApi;
	}
}