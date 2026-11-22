package com.swissotel.swissotel360.shared.infrastructure.i18n.configuration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

/**
 * Configures locale resolution for REST requests.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class LocaleConfiguration {

	@Bean
	@ConditionalOnMissingBean(LocaleResolver.class)
	public LocaleResolver localeResolver() {
		var resolver = new AcceptHeaderLocaleResolver();
		resolver.setDefaultLocale(Locale.ENGLISH);
		resolver.setSupportedLocales(List.of(Locale.ENGLISH, Locale.forLanguageTag("es")));
		return resolver;
	}
}
