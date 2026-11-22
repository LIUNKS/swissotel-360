package com.swissotel.swissotel360.billing.infrastructure.http;

import com.swissotel.swissotel360.shared.infrastructure.security.BearerTokenForwardingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

@Configuration
public class OperationsClientConfiguration {

	@Bean
	public OperationsInternalClient operationsInternalClient(
			@Value("${services.operations.base-url}") String operationsBaseUrl,
			BearerTokenForwardingInterceptor bearerTokenForwardingInterceptor) {
		var requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofSeconds(3));
		requestFactory.setReadTimeout(Duration.ofSeconds(5));

		var restClient = RestClient.builder()
				.baseUrl(operationsBaseUrl)
				.requestFactory(requestFactory)
				.requestInterceptor(bearerTokenForwardingInterceptor)
				.build();

		return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
				.build()
				.createClient(OperationsInternalClient.class);
	}
}
