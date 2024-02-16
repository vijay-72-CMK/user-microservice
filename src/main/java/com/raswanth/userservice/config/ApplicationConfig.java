package com.raswanth.userservice.config;

import com.raswanth.userservice.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {
    private final UserRepository userRepository;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            try {
                return userRepository.findByUsername(username)
                        .orElseThrow(() -> new Exception("User not found"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
