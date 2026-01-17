package com.example.ecommerce;

import com.example.ecommerce.service.AppUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }

    @Bean
    CommandLineRunner createOwner(AppUserService appUserService,
                                  @Value("${app.owner.email:}") String email,
                                  @Value("${app.owner.password:}") String password) {
        return args -> {
            if (email != null && !email.isBlank() && password != null && !password.isBlank()) {
                appUserService.createOwnerIfMissing(email, password);
            }
        };
    }
}
