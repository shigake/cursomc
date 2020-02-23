package com.erick.cursomc.config;

import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.erick.cursomc.services.DBService;
import com.erick.cursomc.services.EmailService;
import com.erick.cursomc.services.MockEmailService;

@Configuration
@Profile("test")
public class TestConfig {
	@Autowired
	private DBService dbService;
	@Bean
	public boolean instantiateDatabase() throws ParseException {
		dbService.InstantiateDatabase();
		return true;
	}
	
	@Bean
	public EmailService emailService() {
		return new MockEmailService();
		
	}
	
}
