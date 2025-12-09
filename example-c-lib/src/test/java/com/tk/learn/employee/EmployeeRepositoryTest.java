package com.tk.learn.employee;

import com.tk.learn.model.dao.Employee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(org.springframework.test.context.junit.jupiter.SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository repository;

    @Test
    @DisplayName("save() should insert a new employee and return it with generated id")
    void save_shouldInsertAndReturnWithId() {
        Employee toSave = new Employee("John", "Doe", "john.doe@example.com");

        Employee saved = repository.save(toSave);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFirstName()).isEqualTo("John");
        assertThat(saved.getLastName()).isEqualTo("Doe");
        assertThat(saved.getEmail()).isEqualTo("john.doe@example.com");

        Optional<Employee> byId = repository.findById(saved.getId());
        assertThat(byId).isPresent();
        assertThat(byId.get().getFirstName()).isEqualTo("John");
    }

    @Test
    @DisplayName("save() with id should update existing employee")
    void save_shouldUpdateWhenIdPresent() {
        Employee saved = repository.save(new Employee("Jane", "Smith", "jane@acme.com"));

        saved.setFirstName("Janet");
        saved.setEmail("janet@acme.com");

        Employee updated = repository.save(saved);

        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getFirstName()).isEqualTo("Janet");
        assertThat(updated.getLastName()).isEqualTo("Smith");
        assertThat(updated.getEmail()).isEqualTo("janet@acme.com");
    }

    @Test
    @DisplayName("findAll() should return all employees ordered by id")
    void findAll_shouldReturnOrderedById() {
        Employee e1 = repository.save(new Employee("A", "One", "a1@ex.com"));
        Employee e2 = repository.save(new Employee("B", "Two", "b2@ex.com"));

        List<Employee> all = repository.findAll();

        assertThat(all).hasSize(2);
        assertThat(all.get(0).getId()).isLessThan(all.get(1).getId());
        assertThat(all).extracting(Employee::getFirstName).containsExactly("A", "B");
    }

    @Test
    @DisplayName("existsById() and deleteById() should behave correctly")
    void existsAndDelete_shouldWork() {
        Employee saved = repository.save(new Employee("X", "Y", "x@y.com"));
        Long id = saved.getId();

        assertThat(repository.existsById(id)).isTrue();

        repository.deleteById(id);

        assertThat(repository.existsById(id)).isFalse();
        assertThat(repository.findById(id)).isEmpty();
    }
}

@Configuration
class TestConfig {

    @Bean
    EmbeddedDatabase dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:schema.sql")
                .build();
    }

    @Bean
    JdbcClient jdbcClient(EmbeddedDatabase dataSource) {
        return JdbcClient.create(dataSource);
    }

    @Bean
    EmployeeRepository employeeRepository(JdbcClient jdbcClient) {
        return new EmployeeRepository(jdbcClient);
    }
}
