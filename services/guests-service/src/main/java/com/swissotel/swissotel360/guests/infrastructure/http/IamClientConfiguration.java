package com.swissotel.swissotel360.guests.infrastructure.http;

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
public class IamClientConfiguration {

	@Bean
	public IamInternalClient iamInternalClient(
			@Value("${services.iam.base-url}") String iamBaseUrl,
			BearerTokenForwardingInterceptor bearerTokenForwardingInterceptor) {
		var requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofSeconds(3));
		requestFactory.setReadTimeout(Duration.ofSeconds(5));

		var restClient = RestClient.builder()
				.baseUrl(iamBaseUrl)
				.requestFactory(requestFactory)
				.requestInterceptor(bearerTokenForwardingInterceptor)
				.build();

		return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
				.build()
				.createClient(IamInternalClient.class);
	}
}
