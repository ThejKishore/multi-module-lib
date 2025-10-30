package com.tk.learn.employee;

import com.tk.learn.model.Department;
import com.tk.learn.model.Employee;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EmployeeService {

    private final EmployeeRepository repository;
    private final EntityManager entityManager;

    public EmployeeService(EmployeeRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Transactional
    public Employee create(Employee employee, Long departmentId) {
        if (departmentId != null) {
            Department ref = entityManager.getReference(Department.class, departmentId);
            employee.setDepartment(ref);
        } else {
            employee.setDepartment(null);
        }
        return repository.save(employee);
    }

    @Transactional
    public Employee update(Long id, Employee updated, Long departmentId) {
        Employee existing = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Employee not found: " + id));
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setEmail(updated.getEmail());
        if (departmentId != null) {
            Department ref = entityManager.getReference(Department.class, departmentId);
            existing.setDepartment(ref);
        }
        return repository.save(existing);
    }

    public Employee get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Employee not found: " + id));
    }

    public List<Employee> list() {
        return repository.findAll();
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Employee not found: " + id);
        }
        repository.deleteById(id);
    }
}
