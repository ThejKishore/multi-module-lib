package com.tk.learn.employee;

import com.tk.learn.model.Employee;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Employee> create(@Valid @RequestBody Employee employee,
                                           @RequestParam(value = "departmentId", required = false) Long departmentId) {
        Employee created = service.create(employee, departmentId);
        return ResponseEntity.created(URI.create("/api/employees/" + created.getId())).body(created);
    }

    @GetMapping("/{id}")
    public Employee get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    public List<Employee> list() {
        return service.list();
    }

    @PutMapping("/{id}")
    public Employee update(@PathVariable Long id,
                           @Valid @RequestBody Employee employee,
                           @RequestParam(value = "departmentId", required = false) Long departmentId) {
        return service.update(id, employee, departmentId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
