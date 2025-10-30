package com.tk.learn.department;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = "com.tk.learn.model")
@EnableJpaRepositories(basePackageClasses = DepartmentRepository.class)
public class JpaTestConfig {
}
