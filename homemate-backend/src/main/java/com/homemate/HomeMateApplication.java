package com.homemate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@SpringBootApplication
@EnableJdbcRepositories 
public class HomeMateApplication {

	public static void main(String[] args) {
		SpringApplication.run(HomeMateApplication.class, args);
	}

}


