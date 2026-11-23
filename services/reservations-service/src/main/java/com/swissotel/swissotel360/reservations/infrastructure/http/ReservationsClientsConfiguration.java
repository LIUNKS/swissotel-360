package com.swissotel.swissotel360.reservations.infrastructure.http;

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
public class ReservationsClientsConfiguration {

	@Bean
	public GuestsInternalClient guestsInternalClient(
			@Value("${services.guests.base-url}") String guestsBaseUrl,
			BearerTokenForwardingInterceptor bearerTokenForwardingInterceptor) {
		return createClient(guestsBaseUrl, bearerTokenForwardingInterceptor, GuestsInternalClient.class);
	}

	@Bean
	public RoomsInternalClient roomsInternalClient(
			@Value("${services.rooms.base-url}") String roomsBaseUrl,
			BearerTokenForwardingInterceptor bearerTokenForwardingInterceptor) {
		return createClient(roomsBaseUrl, bearerTokenForwardingInterceptor, RoomsInternalClient.class);
	}

	private static <T> T createClient(
			String baseUrl,
			BearerTokenForwardingInterceptor interceptor,
			Class<T> clientType) {
		var requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofSeconds(3));
		requestFactory.setReadTimeout(Duration.ofSeconds(5));

		var restClient = RestClient.builder()
				.baseUrl(baseUrl)
				.requestFactory(requestFactory)
				.requestInterceptor(interceptor)
				.build();

		var conversionService = new DefaultFormattingConversionService();
		var dateTimeRegistrar = new DateTimeFormatterRegistrar();
		dateTimeRegistrar.setUseIsoFormat(true);
		dateTimeRegistrar.registerFormatters(conversionService);

		return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
				.conversionService(conversionService)
				.build()
				.createClient(clientType);
	}
}
