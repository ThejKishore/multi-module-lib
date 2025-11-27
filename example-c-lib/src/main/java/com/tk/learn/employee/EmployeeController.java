package com.tk.learn.employee;

import am.ik.yavi.core.ConstraintViolations;
import com.tk.learn.model.dao.Employee;
import com.tk.learn.model.dto.EmployeeReq;
import com.tk.learn.model.dto.EmployeeResp;
import com.tk.learn.model.exceptions.InValidObjectException;
import com.tk.learn.model.mapper.EmployeeMapper;
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
    public ResponseEntity<EmployeeResp> create(@RequestBody EmployeeReq employee) {
        ConstraintViolations violations = EmployeeReq.employeeValidator.validate(employee);
        if(!violations.isValid()){
            throw new InValidObjectException(violations.details());
        }
        Employee employee1 = EmployeeMapper.toEmployee(employee);

        Employee created = service.create(employee1);
        EmployeeResp resp = EmployeeMapper.toEmployeeResp(created);
        return ResponseEntity.created(URI.create("/api/employees/" + created.getId())).body(resp);
    }

    @GetMapping("/{id}")
    public EmployeeResp get(@PathVariable Long id) {
        Employee employee = service.get(id);
        return EmployeeMapper.toEmployeeResp(employee);
    }

    @GetMapping
    public List<EmployeeResp> list() {
        return service.list().stream().map(EmployeeMapper::toEmployeeResp).toList();
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResp> update(@PathVariable Long id, @RequestBody EmployeeReq employee) {
        ConstraintViolations violations = EmployeeReq.employeeValidator.validate(employee);
        if(!violations.isValid()){
          throw new InValidObjectException(violations.details());
        }
        Employee employee1 = EmployeeMapper.toEmployee(employee);
        Employee employee2 = service.get(id);
        employee2.setFirstName(employee1.getFirstName());
        employee2.setLastName(employee1.getLastName());
        employee2.setEmail(employee1.getEmail());
        Employee updateEmployee = service.update(id, employee2);
        EmployeeResp resp = EmployeeMapper.toEmployeeResp(updateEmployee);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
