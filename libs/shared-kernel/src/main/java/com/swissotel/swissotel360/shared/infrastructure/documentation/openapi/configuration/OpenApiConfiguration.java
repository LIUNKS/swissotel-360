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
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * Configures the OpenAPI specification exposed by the platform.
 */
@AutoConfiguration
@ConditionalOnClass(OpenAPI.class)
public class OpenApiConfiguration {

	/**
	 * Builds the OpenAPI document used by Swagger UI and client generation tools.
	 *
	 * @return configured OpenAPI descriptor
	 */
	@Bean
	@ConditionalOnMissingBean(OpenAPI.class)
	public OpenAPI swissotelOpenApi(
			@Value("${spring.application.name:swissotel-360}") String applicationName,
			@Value("${documentation.application.description:Swissotel 360 REST API}") String applicationDescription,
			@Value("${documentation.application.version:1.0.0}") String applicationVersion,
			@Value("${documentation.application.server-url:http://localhost:8080}") String serverUrl) {
		var openApi = new OpenAPI();
		openApi
				.info(new Info()
						.title(applicationName)
						.description(applicationDescription)
						.version(applicationVersion)
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
						.url(serverUrl)
						.description("API Server")));

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
