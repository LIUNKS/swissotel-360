package com.swissotel.swissotel360.operations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
		"com.swissotel.swissotel360.operations",
		"com.swissotel.swissotel360.stays",
		"com.swissotel.swissotel360.servicerequests"
})
public class OperationsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OperationsServiceApplication.class, args);
	}
}
