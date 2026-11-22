package br.com.itau.investments.suitability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan(basePackages = "br.com.itau.investments.suitability.domain.model")
@EnableJpaRepositories(basePackages = "br.com.itau.investments.suitability.infrastructure.persistence.jpa")
@EnableScheduling
public class SuitabilityEngineApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SuitabilityEngineApplication.class, args);
    }
}