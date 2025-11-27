package com.tk.learn.model.mapper;

import com.tk.learn.model.dao.Employee;
import com.tk.learn.model.dto.EmployeeReq;
import com.tk.learn.model.dto.EmployeeResp;

public class EmployeeMapper {

    private EmployeeMapper(){}

    public static Employee toEmployee(EmployeeReq employeeReq){
        Employee employee = new Employee();
        employee.setFirstName(employeeReq.firstName());
        employee.setLastName(employeeReq.lastName());
        employee.setEmail(employeeReq.email());
        return employee;
    }

    public static EmployeeResp toEmployeeResp(Employee employee){
        return  new EmployeeResp(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail()
        );
    }
}
