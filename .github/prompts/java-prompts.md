# Java Prompt for GitHub Copilot

This prompt is designed to guide GitHub Copilot in assisting an expert-level Java developer working on Spring Boot, Kotlin-interoperable projects, with clean architecture, domain-driven design, and testing best practices.

---

## Developer Profile

- **Expertise**: Java 17+, Kotlin, Spring Boot, Spring Data, Spring Security, REST APIs.
- **Focus**: Domain-driven design, modular architecture, clean and maintainable code.
- **Goals**: Produce production-quality code with proper testing, validation, and documentation.

---

## Coding Guidelines

1. **Project Structure**
    - Follow domain-based package structure:
      ```
      com.example.project.domain
          model
          repository
          service
          controller
          dto
          mapper
      ```
    - Modular approach: Controller → Service → Repository → DTO → Mapper.
    - Configuration in `application.yml` or `application.properties` supporting environment-aware profiles.

2. **Code Quality**
    - Use clean, readable, and maintainable idiomatic Java.
    - Include Javadoc for classes and public methods.
    - Use proper exception handling and logging.
    - Avoid anti-patterns such as God classes or excessive static utility usage.

3. **Spring Boot Best Practices**
    - Controllers: `@RestController`, `@RequestMapping`, `@Valid`, `@Validated`.
    - Services: Business logic separated from repositories; prefer interface-based design.
    - Repositories: Spring Data JPA, with query derivation and optional custom queries.
    - Configuration: Use `@ConfigurationProperties` for externalized config.
    - Security: Spring Security, JWT, OAuth2 where applicable.
    - Async operations: Kotlin coroutines or Java `CompletableFuture`.

4. **Kotlin Interoperability**
    - Prefer null-safety patterns, data classes, and extension functions.
    - Ensure Java-Kotlin interop is smooth with proper annotations (jspecify if possible).

---

## Testing Guidelines

1. **Unit Tests**
    - Focus on class-level logic.
    - Services: mock repositories, verify interactions and outputs.
    - Models: verify `equals`/`hashCode` and simple invariants.
    - Libraries: JUnit 5, AssertJ, Mockito.

2. **Web MVC Slice Tests (@WebMvcTest)**
    - Validate request mapping, JSON serialization, and Bean Validation.
    - Use `MockMvc` and `@MockBean` for service layer.
    - Assert HTTP status, headers, and JSON response paths.

3. **JPA Slice Tests (@DataJpaTest)**
    - Use embedded database for repository testing.
    - Minimal context, verify query correctness and entity persistence.
    - Use module-local Spring Boot configuration for test bootstrapping.

4. **Integration Tests (@SpringBootTest)**
    - Full application context: controllers, services, repositories.
    - Real HTTP layer (`MockMvc` or random port) and DB configuration.
    - End-to-end testing with assertions on data and HTTP responses.

---

## Prompt Instructions for Copilot

When generating code:

1. Follow domain-driven design principles.
2. Use modular, maintainable patterns and avoid large monolithic classes.
3. Prefer slice tests for fast feedback, reserve `@SpringBootTest` for cross-layer integration tests.
4. Ensure Kotlin interoperability when Kotlin modules exist.
5. Generate expressive AssertJ assertions in tests.
6. Include input validation, error handling, and logging in controllers and services.
7. Provide meaningful variable names, class names, and method names.
8. Suggest code that adheres to Java 17+ and Spring Boot idioms.

---

## Example Prompts

- "Create a Spring Boot REST API for managing `Order` entities with DTO mapping, validation, and unit tests."
- "Generate a service in Java that calculates payroll with dependency injection and unit tests."
- "Write a `@WebMvcTest` for `EmployeeController` with MockMvc and mock services."
- "Provide a repository test using `@DataJpaTest` for `DepartmentRepository`."
- "Implement Kotlin-friendly Java service with coroutines support and null-safety."

---

## Copilot Do's and Don'ts

**Do:**
- Suggest clean, idiomatic, and modular Java code.
- Include tests wherever applicable.
- Use Spring Boot conventions and modern Java features.
- Comment complex logic for clarity.

**Don't:**
- Suggest anti-patterns like God classes, tight coupling, or static-heavy utilities.
- Skip exception handling or validation in generated code.
- Mix layers (e.g., controller code in service or repository).
- Generate code ignoring Kotlin interoperability.

