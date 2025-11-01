package com.tk.learn.department;

import com.tk.learn.model.Department;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DepartmentServiceTest {

    private static final Logger log = LoggerFactory.getLogger(DepartmentServiceTest.class);
    @Mock
    private DepartmentRepository repository;

    @InjectMocks
    private DepartmentService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_savesDepartment() {
        Department input = new Department("Engineering");
        Department saved = new Department("Engineering");
        // simulate JPA assigning ID
        try {
            var idField = Department.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(saved, 1L);
        } catch (Exception e) {
            log.error("Failed to set id field", e);
        }

        when(repository.save(any(Department.class))).thenReturn(saved);

        Department result = service.create(input);

        assertThat(result.getId()).isEqualTo(1L);
        ArgumentCaptor<Department> captor = ArgumentCaptor.forClass(Department.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Engineering");
    }

    @Test
    void update_updatesName() {
        Department existing = new Department("Old");
        try {
            var idField = Department.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(existing, 10L);
        } catch (Exception ignored) {
            log.error("Failed to set id field", ignored);
        }
        when(repository.findById(10L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Department.class))).thenAnswer(i -> i.getArgument(0));

        Department updated = new Department("New");
        Department result = service.update(10L, updated);

        assertThat(result.getName()).isEqualTo("New");
        verify(repository).save(existing);
    }

    @Test
    void get_throwsWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("99");
    }

    @Test
    void list_delegatesToRepository() {
        when(repository.findAll()).thenReturn(List.of(new Department("A"), new Department("B")));
        assertThat(service.list()).hasSize(2);
        verify(repository).findAll();
    }

    @Test
    void delete_whenMissing_throws() {
        when(repository.existsById(123L)).thenReturn(false);
        assertThatThrownBy(() -> service.delete(123L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void delete_whenExists_deletes() {
        when(repository.existsById(1L)).thenReturn(true);
        service.delete(1L);
        verify(repository).deleteById(1L);
    }
}
