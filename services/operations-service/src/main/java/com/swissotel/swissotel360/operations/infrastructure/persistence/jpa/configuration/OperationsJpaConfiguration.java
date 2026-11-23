package com.swissotel.swissotel360.operations.infrastructure.persistence.jpa.configuration;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {
		"com.swissotel.swissotel360.stays",
		"com.swissotel.swissotel360.servicerequests"
})
@EntityScan(basePackages = {
		"com.swissotel.swissotel360.stays",
		"com.swissotel.swissotel360.servicerequests"
})
public class OperationsJpaConfiguration {
}
