package com.tk.learn.employee;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tk.learn.model.dao.Employee;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Employee create(Employee employee) {
        return repository.save(employee);
    }

    @Transactional
    public Employee update(Long id, Employee updated) {
        Employee existing = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Employee not found: " + id));
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setEmail(updated.getEmail());
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
