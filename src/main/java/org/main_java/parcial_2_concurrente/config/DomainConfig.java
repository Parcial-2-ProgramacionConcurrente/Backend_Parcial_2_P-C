package org.main_java.parcial_2_concurrente.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

import java.time.OffsetDateTime;
import java.util.Optional;

@Configuration
@EnableReactiveMongoRepositories("org.main_java.parcial_2_concurrente.repos")
@EnableReactiveMongoAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class DomainConfig {

    @Bean(name = "auditingDateTimeProvider")
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }
}


