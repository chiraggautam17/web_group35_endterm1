package com.ips;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class IntelligentParkingSystemApplication {
	
	private static final Logger log = LoggerFactory.getLogger(IntelligentParkingSystemApplication.class);

	public static void main(String[] args) {
		log.info("Starting Intelligent Parking System application...");
		SpringApplication.run(IntelligentParkingSystemApplication.class, args);
		log.info("Intelligent Parking System application is running.");
	}

}
