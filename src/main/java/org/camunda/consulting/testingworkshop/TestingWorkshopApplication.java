package org.camunda.consulting.testingworkshop;

import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Deployment(resources = "classpath:camunda/*")
public class TestingWorkshopApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestingWorkshopApplication.class, args);
	}

}
