package com.y0u1me2.visitbundang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class VisitBundangApplication {

	public static void main(String[] args) {
		SpringApplication.run(VisitBundangApplication.class, args);
	}

}
