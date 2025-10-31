# GitHub Copilot Instructions for Expert Spring Boot, Java, Kotlin Developer

These instructions are meant to guide GitHub Copilot in assisting a developer working with Spring Boot, Java, and Kotlin at an expert level.

---

## General Principles

- **Code Quality**: Prioritize clean, readable, maintainable, and idiomatic code. Follow best practices for Java, Kotlin, and Spring Boot.
- **Performance**: Suggest efficient algorithms and data structures.
- **Error Handling**: Include proper exception handling, validation, and logging.
- **Testing**: Provide unit tests, integration tests, and mock examples where appropriate.
- **Security**: Follow secure coding standards, including input validation, authentication, authorization, and safe database handling.
- **Documentation**: Suggest meaningful Javadoc/KDoc and inline comments.
- **Patterns & Architecture**: Use proven design patterns (Factory, Builder, Singleton, Observer, Strategy, etc.) and Spring idioms (DI, Bean configuration, Profiles).
- **Modern Language Features**: Leverage Kotlin idioms, null-safety, data classes, coroutines, extension functions, and functional programming features.

---

## Project Structure

- Assume Maven or Gradle multi-module projects.
- Follow standard Spring Boot project conventions:  
```shell
src/main/java or kotlin
src/main/resources
src/test/java or kotlin
```

- Use **domain-based package structure**:

```shell
com.tk.employee
      -> EmployeeDto.java
      -> Employee.java
      -> EmployeeRepository.java
      -> EmployeeService.java
      -> EmployeeResource.java
com.tk.department
      -> DepartmentDto.java
      -> Department.java
      -> DepartmentRepository.java
      -> DepartmentService.java
      -> DepartmentReource.java
```

- Modular code organization: Controller → Service → Repository → DTO → Mapper.
- Configuration files: `application.yml` or `application.properties`, supporting environment-aware profiles.

---

## Spring Boot Recommendations

- **REST APIs**
- Use `@RestController`, `@RequestMapping`, `@GetMapping`, `@PostMapping`.
- Provide clear request/response DTOs.
- Handle validation using `@Valid` and `@Validated`.
- Provide exception handling with `@ControllerAdvice`.
- **Data Access**
- Prefer Spring Data JPA or Spring JDBC templates.
- Use repository interfaces with query derivation.
- Include pagination, filtering, and sorting examples.
- **Security**
- Suggest Spring Security configuration for authentication/authorization.
- Use OAuth2, JWT, or other modern mechanisms.
- **Configuration**
- Use `@ConfigurationProperties` for externalized configuration.
- **Testing**
- Provide unit, slice, and integration test examples.
- Use JUnit 5, Spring Test, AssertJ, Mockito, Spring Boot Test starters.

---

## Java/Kotlin Guidelines

- **Java**
- Target Java 17+.
- Use streams, optionals, and records where appropriate.
- **Kotlin**
- Use idiomatic Kotlin features: data classes, sealed classes, extension functions.
- Use coroutines for async processing.
- Prefer `val` over `var` and use null-safety patterns.
- **Interoperability**
- Use Kotlin-friendly Spring Boot conventions.
- Suggest Java interop helpers if required.
- Include jspecify annotations for null-safety when possible.

---

## Testing Approaches

### Testing Frameworks & Libraries

- JUnit 5 (Jupiter), Spring Test, AssertJ, Mockito, Spring Boot Test starters.

### Unit Tests

- **Focus**: logic within a single class in isolation.
- **Services**: mock repositories, verify interactions and return values.  
  Examples: `DepartmentServiceTest`, `EmployeeServiceTest`.
- **Model**: verify `equals`/`hashCode` and simple invariants.  
  Example: `DepartmentEqualsHashCodeTest`.

### Web MVC Slice Tests (@WebMvcTest)

- **Purpose**: exercise controller request mapping, validation, JSON serialization without full context.
- **Tools**: `MockMvc` for HTTP requests, `@MockBean` to stub service layer.
- **Examples**: `DepartmentControllerTest`, `EmployeeControllerTest`.
- **Notes**: Provide valid request payloads that satisfy Bean Validation (`@NotBlank`, etc.). Test context auto-configures `ObjectMapper`.

### JPA Slice Tests (@DataJpaTest)

- **Purpose**: test repositories with an embedded DB and minimal context.
- **Config**: `JpaTestConfig` enables entity scanning for `com.tk.learn.model` and repository scanning for the module’s repositories.
- **Examples**: `DepartmentRepositoryTest`, `EmployeeRepositoryTest`.

### Module-local Spring Boot Configuration for Tests

- Each feature module provides a minimal `@SpringBootApplication` class under test sources (`TestBootApplication`) so the Spring Boot test bootstrapper can discover a `@SpringBootConfiguration`.

### Integration Tests (@SpringBootTest at root module)

- **Purpose**: end-to-end tests booting the full application (controllers, services, repositories, configuration).
- **Files**: `DepartmentIntegrationTest.java`, `EmployeeIntegrationTest.java`.
- Use real HTTP layer (random port or `MockMvc`) and real DB configuration as provided by the test setup.

### General Testing Guidance

- Prefer slice tests for fast feedback; reserve `@SpringBootTest` for cross-layer scenarios.
- Keep assertions expressive with AssertJ; verify HTTP status codes, headers (e.g., Location), and JSON paths in MVC tests.
- Clearly separate concerns: controller tests don’t hit DB; repository tests don’t involve the web layer.
- If a module’s tests report “Unable to find a @SpringBootConfiguration,” ensure the test source set contains a minimal `@SpringBootApplication`.

---

## Code Style & Linting

- Follow Palantir Java Style or Kotlin Coding Conventions.
- Proper formatting, spacing, import ordering, minimal unused imports.
- Detect code smells: long methods, large classes, duplicate code.
- Make sure the function is not more that 10 lines. Break a function into smaller ones if necessary. Use default identifier for functions so that it is easily unit testable.
- Use descriptive variable names.
- Use descriptive method names.
- Use descriptive class names.
- Use descriptive package names.
- Use descriptive constants.
- Use descriptive enum values.
- Use descriptive exception messages.
- Use descriptive Javadoc/KDoc.
- Use descriptive inline comments.
---
