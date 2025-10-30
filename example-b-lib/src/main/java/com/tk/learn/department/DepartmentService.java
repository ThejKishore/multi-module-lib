package com.tk.learn.department;

import com.tk.learn.model.Department;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DepartmentService {

    private final DepartmentRepository repository;

    public DepartmentService(DepartmentRepository repository) {
        this.repository = repository;
    }

    public Department create(Department dept) {
        return repository.save(dept);
    }

    public Department update(Long id, Department updated) {
        Department existing = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Department not found: " + id));
        existing.setName(updated.getName());
        return repository.save(existing);
    }

    public Department get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Department not found: " + id));
    }

    public List<Department> list() {
        return repository.findAll();
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Department not found: " + id);
        }
        repository.deleteById(id);
    }
}
