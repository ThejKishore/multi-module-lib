package com.tk.learn;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.TimeZone;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        // Set JVM default timezone (use IANA zone to handle DST)
        TimeZone tz = TimeZone.getTimeZone("America/Chicago");
        TimeZone.setDefault(tz);
        // Also set the system property so libraries that read user.timezone pick it up
        System.setProperty("user.timezone", "America/Chicago");
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("SpringShop API")
                        .description("Spring shop sample application")
                        .version("v0.0.1")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("SpringShop Wiki Documentation")
                        .url("https://springshop.wiki.github.org/docs"));
    }
}
