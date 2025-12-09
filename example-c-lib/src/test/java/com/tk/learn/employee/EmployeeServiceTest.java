package com.tk.learn.employee;

import com.tk.learn.model.dao.Employee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository repository;

    @InjectMocks
    private EmployeeService service;

    @Test
    @DisplayName("create() should delegate to repository.save and return result")
    void create_shouldSave() {
        Employee input = new Employee("John", "Doe", "john@ex.com");
        Employee saved = new Employee(1L, "John", "Doe", "john@ex.com");
        given(repository.save(any(Employee.class))).willReturn(saved);

        Employee result = service.create(input);

        assertThat(result.getId()).isEqualTo(1L);
        verify(repository).save(any(Employee.class));
    }

    @Test
    @DisplayName("update() should update existing employee and return it")
    void update_shouldWork_whenFound() {
        Long id = 10L;
        Employee existing = new Employee(id, "Old", "Name", "old@ex.com");
        Employee changes = new Employee("New", "Name", "new@ex.com");
        Employee updated = new Employee(id, "New", "Name", "new@ex.com");

        given(repository.findById(id)).willReturn(Optional.of(existing));
        given(repository.save(any(Employee.class))).willReturn(updated);

        Employee result = service.update(id, changes);

        assertThat(result.getFirstName()).isEqualTo("New");
        assertThat(result.getEmail()).isEqualTo("new@ex.com");
        verify(repository).findById(eq(id));
        verify(repository).save(any(Employee.class));
    }

    @Test
    @DisplayName("update() should throw when employee not found")
    void update_shouldThrow_whenNotFound() {
        Long id = 99L;
        given(repository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, new Employee("A", "B", "a@b.com")))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Employee not found");
    }

    @Test
    @DisplayName("get() should return employee when found")
    void get_shouldReturn_whenFound() {
        Long id = 2L;
        Employee e = new Employee(id, "A", "B", "a@b.com");
        given(repository.findById(id)).willReturn(Optional.of(e));

        Employee result = service.get(id);

        assertThat(result).isSameAs(e);
        verify(repository).findById(id);
    }

    @Test
    @DisplayName("get() should throw when not found")
    void get_shouldThrow_whenNotFound() {
        given(repository.findById(3L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(3L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("list() should return all employees")
    void list_shouldReturnAll() {
        List<Employee> employees = List.of(
                new Employee(1L, "A", "B", "a@b.com"),
                new Employee(2L, "C", "D", "c@d.com")
        );
        given(repository.findAll()).willReturn(employees);

        List<Employee> result = service.list();

        assertThat(result).hasSize(2).extracting(Employee::getId).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("delete() should delete when exists, else throw")
    void delete_shouldBehave() {
        // exists -> delete
        given(repository.existsById(5L)).willReturn(true);
        service.delete(5L);
        verify(repository).deleteById(5L);

        // not exists -> throw, and delete not called
        given(repository.existsById(6L)).willReturn(false);
        assertThatThrownBy(() -> service.delete(6L))
                .isInstanceOf(NoSuchElementException.class);
        verify(repository, never()).deleteById(6L);
    }
}
