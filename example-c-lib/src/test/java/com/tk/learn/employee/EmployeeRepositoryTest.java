package com.tk.learn.employee;

import com.tk.learn.model.Department;
import com.tk.learn.model.Employee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@org.springframework.context.annotation.Import(JpaTestConfig.class)
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    void save_find_update_delete_withDepartment() {
        // Create an employee without department first
        Employee e = new Employee();
        e.setFirstName("John");
        e.setLastName("Doe");
        e.setEmail("john.doe@example.com");
        Employee saved = employeeRepository.save(e);
        assertThat(saved.getId()).isNotNull();

        // Persist department and associate
        Department dept = new Department("Engineering");
        dept = em.persistFlushFind(dept);
        saved.setDepartment(dept);
        Employee updated = employeeRepository.save(saved);
        assertThat(updated.getDepartment()).isNotNull();
        assertThat(updated.getDepartment().getName()).isEqualTo("Engineering");

        // find
        Optional<Employee> byId = employeeRepository.findById(saved.getId());
        assertThat(byId).isPresent();
        assertThat(byId.get().getFirstName()).isEqualTo("John");

        // list
        assertThat(employeeRepository.findAll()).hasSize(1);

        // delete
        employeeRepository.deleteById(saved.getId());
        assertThat(employeeRepository.findById(saved.getId())).isEmpty();
    }
}
