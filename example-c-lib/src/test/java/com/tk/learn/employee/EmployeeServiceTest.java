package com.tk.learn.employee;

import com.tk.learn.model.Department;
import com.tk.learn.model.Employee;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {

    @Mock
    private EmployeeRepository repository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private EmployeeService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_setsDepartmentReference_whenDepartmentIdProvided() {
        Employee input = new Employee();
        input.setFirstName("John");
        input.setLastName("Doe");
        input.setEmail("john.doe@example.com");

        Department deptRef = new Department("Engineering");
        // stub entity manager reference behavior
        when(entityManager.getReference(Department.class, 5L)).thenReturn(deptRef);
        // stub save result (echo back)
        when(repository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));

        Employee saved = service.create(input, 5L);

        assertThat(saved.getDepartment()).isSameAs(deptRef);
        verify(repository).save(any(Employee.class));
    }

    @Test
    void update_changesFields_andDepartment() {
        Employee existing = new Employee();
        existing.setFirstName("Old");
        existing.setLastName("Name");
        existing.setEmail("old@example.com");

        when(repository.findById(10L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));
        Department newDept = new Department("QA");
        when(entityManager.getReference(Department.class, 2L)).thenReturn(newDept);

        Employee patch = new Employee();
        patch.setFirstName("Jane");
        patch.setLastName("Doe");
        patch.setEmail("jane.doe@example.com");

        Employee result = service.update(10L, patch, 2L);

        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getEmail()).isEqualTo("jane.doe@example.com");
        assertThat(result.getDepartment()).isSameAs(newDept);
        verify(repository).save(existing);
    }

    @Test
    void get_throwsWhenMissing() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("999");
    }

    @Test
    void list_returnsAll() {
        when(repository.findAll()).thenReturn(List.of(new Employee(), new Employee()));
        assertThat(service.list()).hasSize(2);
        verify(repository).findAll();
    }

    @Test
    void delete_behavesProperly() {
        when(repository.existsById(1L)).thenReturn(true);
        service.delete(1L);
        verify(repository).deleteById(1L);

        when(repository.existsById(2L)).thenReturn(false);
        assertThatThrownBy(() -> service.delete(2L)).isInstanceOf(NoSuchElementException.class);
    }
}
