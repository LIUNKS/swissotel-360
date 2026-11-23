package com.swissotel.swissotel360.shared.infrastructure.autoconfigure;

import com.swissotel.swissotel360.shared.interfaces.rest.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for shared kernel web primitives.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SharedKernelAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(GlobalExceptionHandler.class)
	public GlobalExceptionHandler globalExceptionHandler() {
		return new GlobalExceptionHandler();
	}
}
