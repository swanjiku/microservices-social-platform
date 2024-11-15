package com.blog.user_service;

import com.blog.user_service.auth.AuthenticationService;
import com.blog.user_service.auth.DTO.RegisterRequest;
import com.blog.user_service.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;

@SpringBootApplication
@Slf4j
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	@Bean
	public CommandLineRunner commandLineRunner(AuthenticationService service, UserRepository userRepository) {
		return args -> {
			createAdminUser(service, userRepository);

		};
	}

	@Bean
	public CommandLineRunner createAdminUser(AuthenticationService service, UserRepository userRepository) {
		return args -> {
			String adminEmail = "admin@mail.com";

			log.info("Checking for existing admin user...");

			if (userRepository.findByEmail(adminEmail).isEmpty()) {
				log.info("Admin user not found. Attempting to create...");

				var adminRequest = RegisterRequest.builder()
						.username("Admin")
						.email(adminEmail)
						.role("ADMIN")
						.password("password")
						.build();

				try {
					var response = service.register(adminRequest);
					if (response.getStatusCode() == HttpStatus.CREATED.value()) {
						log.info("Admin user created successfully");
						// Uncomment the next line if you want to see the admin token in the logs
						// log.info("Admin token: {}", response.getData().getAccessToken());
					} else {
						log.warn("Admin user creation failed. Status: {}, Message: {}",
								response.getStatusCode(), response.getMessage());
					}
				} catch (Exception e) {
					log.error("Error creating admin user", e);
				}
			} else {
				log.info("Admin user already exists.");
			}
		};
	}

}
