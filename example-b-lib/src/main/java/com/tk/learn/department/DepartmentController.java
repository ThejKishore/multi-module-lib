package com.tk.learn.department;

import com.tk.learn.model.Department;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService service;

    public DepartmentController(DepartmentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Department> create(@Valid @RequestBody Department dept) {
        Department created = service.create(dept);
        return ResponseEntity.created(URI.create("/api/departments/" + created.getId())).body(created);
    }

    @GetMapping("/{id}")
    public Department get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    public List<Department> list() {
        return service.list();
    }

    @PutMapping("/{id}")
    public Department update(@PathVariable Long id, @Valid @RequestBody Department dept) {
        return service.update(id, dept);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
