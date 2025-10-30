# Project Guidelines

Generated: 2025-10-29 23:57 (local time)

## Coding Conventions
- Language/Frameworks: Java (Spring Boot, Spring MVC, Spring Data JPA), Gradle Kotlin DSL.
- Packaging:
  - Package names are all lowercase, dot-separated: `com.tk.learn`, `com.tk.learn.department`, `com.tk.learn.employee`, `com.tk.learn.model`.
  - Class names use UpperCamelCase (e.g., `DepartmentController`, `EmployeeService`).
  - Methods and fields use lowerCamelCase.
- Annotations:
  - Spring stereotypes on appropriate layers: `@RestController` (web), `@Service` (business), `@Repository` (persistence), JPA annotations on entities, `@Validated`/Bean Validation annotations on DTOs/entities.
  - Tests use `@WebMvcTest` for slice MVC tests and `@DataJpaTest` for repository slice tests. Integration tests use `@SpringBootTest` at the root module.
- Immutability and nullability:
  - Prefer constructor/setter usage consistent with JPA entities defined in `example-lib`.
  - Use Bean Validation (`@NotBlank`, etc.) to enforce invariants at the boundary.
- Logging & errors:
  - Centralized error handling lives in `src/main/java/com/tk/learn/web/GlobalExceptionHandler.java`.
- Assertions & mocking in tests:
  - Use AssertJ for fluent assertions (`assertThat`), Mockito for mocks/stubs (`@MockBean`, `Mockito.when(...)`).
- Import order & formatting:
  - Follow existing style in the repo: standard Java import grouping, no wildcard imports in production code.
  - Keep methods small and focused; controllers delegate to services, services delegate to repositories.

## Code Organization and Package Structure
- Multi-module Gradle project (settings in `settings.gradle.kts`, versions in `gradle/libs.versions.toml`).
- Root application module:
  - Main Spring Boot app class: `src/main/java/com/tk/learn/Application.java`.
  - Global exception handling: `src/main/java/com/tk/learn/web/GlobalExceptionHandler.java`.
  - Application configuration: `src/main/resources/application.yml`.
  - Root-level integration tests: `src/test/java/com/tk/learn/*IntegrationTest.java`.
- example-lib (shared domain model):
  - Entities: `example-lib/src/main/java/com/tk/learn/model/Department.java`, `Employee.java`.
  - Unit tests for model: `example-lib/src/test/java/com/tk/learn/model/DepartmentEqualsHashCodeTest.java`.
- example-b-lib (Department feature):
  - Controller: `.../department/DepartmentController.java`.
  - Service: `.../department/DepartmentService.java`.
  - Repository: `.../department/DepartmentRepository.java`.
  - Tests:
    - MVC slice: `DepartmentControllerTest` with `@WebMvcTest`.
    - JPA slice: `DepartmentRepositoryTest` with `@DataJpaTest` and `JpaTestConfig`.
    - Service unit tests: `DepartmentServiceTest`.
    - Test-only boot app for configuration discovery: `example-b-lib/src/test/java/com/tk/learn/TestBootApplication.java`.
- example-c-lib (Employee feature):
  - Controller: `.../employee/EmployeeController.java`.
  - Service: `.../employee/EmployeeService.java`.
  - Repository: `.../employee/EmployeeRepository.java`.
  - Tests:
    - MVC slice: `EmployeeControllerTest` with `@WebMvcTest` and `@MockBean`.
    - JPA slice: `EmployeeRepositoryTest` with `@DataJpaTest` and `JpaTestConfig`.
    - Service unit tests: `EmployeeServiceTest`.
    - Test-only boot app for configuration discovery: `example-c-lib/src/test/java/com/tk/learn/TestBootApplication.java`.

## Unit and Integration Testing Approaches
- Testing frameworks & libraries:
  - JUnit 5 (Jupiter), Spring Test, AssertJ, Mockito, Spring Boot Test starters.
- Unit tests:
  - Focus on logic within a single class in isolation.
  - Services: mock repositories and verify interactions and return values (see `DepartmentServiceTest`, `EmployeeServiceTest`).
  - Model: verify equals/hashCode and simple invariants (see `DepartmentEqualsHashCodeTest`).
- Web MVC slice tests (`@WebMvcTest`):
  - Purpose: exercise controller request mapping, validation, and JSON serialization without loading full application context.
  - Tools: `MockMvc` for HTTP requests, `@MockBean` to stub service layer.
  - Examples: `DepartmentControllerTest`, `EmployeeControllerTest`.
  - Notes: Provide valid request payloads that satisfy Bean Validation (`@NotBlank`, etc.). The test context auto-configures `ObjectMapper`.
- JPA slice tests (`@DataJpaTest`):
  - Purpose: test repositories with an embedded database and minimal context.
  - Config: `JpaTestConfig` enables entity scan for `com.tk.learn.model` and repository scanning for the module’s repositories.
  - Examples: `DepartmentRepositoryTest`, `EmployeeRepositoryTest`.
- Module-local Spring Boot configuration for tests:
  - Each feature module provides a minimal `@SpringBootApplication` class under test sources (`TestBootApplication`) so Spring Boot test bootstrapper can discover a `@SpringBootConfiguration` when needed.
- Integration tests (`@SpringBootTest` at root module):
  - End-to-end tests that boot the full application (controllers, services, repositories, configuration).
  - Files: `src/test/java/com/tk/learn/DepartmentIntegrationTest.java`, `EmployeeIntegrationTest.java`.
  - Use real HTTP layer (random port or MockMvc configured by Spring Boot), real DB configuration as provided by the test setup.
- General testing guidance:
  - Prefer slice tests for fast feedback and isolation; reserve `@SpringBootTest` for cross-layer scenarios.
  - Keep assertions expressive with AssertJ; verify HTTP status codes, headers (e.g., `Location`), and JSON paths in MVC tests.
  - Clearly separate concerns: controller tests don’t hit the DB; repository tests don’t involve the web layer.
  - If a module’s tests report “Unable to find a @SpringBootConfiguration”, ensure the test source set contains a minimal `@SpringBootApplication` (as already added).
