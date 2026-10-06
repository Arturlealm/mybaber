package com.mybarber;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MybarberApplication {

	public static void main(String[] args) {
		SpringApplication.run(MybarberApplication.class, args);
	}

}
