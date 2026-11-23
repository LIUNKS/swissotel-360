package com.swissotel.swissotel360.rooms.infrastructure.http;

import com.swissotel.swissotel360.shared.infrastructure.security.BearerTokenForwardingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

@Configuration
public class ReservationsClientConfiguration {

	@Bean
	public ReservationsInternalClient reservationsInternalClient(
			@Value("${services.reservations.base-url}") String reservationsBaseUrl,
			BearerTokenForwardingInterceptor bearerTokenForwardingInterceptor) {
		var requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofSeconds(3));
		requestFactory.setReadTimeout(Duration.ofSeconds(5));

		var restClient = RestClient.builder()
				.baseUrl(reservationsBaseUrl)
				.requestFactory(requestFactory)
				.requestInterceptor(bearerTokenForwardingInterceptor)
				.build();

		var conversionService = new DefaultFormattingConversionService();
		var dateTimeRegistrar = new DateTimeFormatterRegistrar();
		dateTimeRegistrar.setUseIsoFormat(true);
		dateTimeRegistrar.registerFormatters(conversionService);

		return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
				.conversionService(conversionService)
				.build()
				.createClient(ReservationsInternalClient.class);
	}
}
