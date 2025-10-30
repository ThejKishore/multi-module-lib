package com.tk.learn;

import com.tk.learn.model.Department;
import com.tk.learn.model.Employee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EmployeeIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void employeeCrudFlow() {
        // Ensure a Department exists
        Department deptReq = new Department("QA");
        ResponseEntity<Department> deptResp = restTemplate.postForEntity(url("/api/departments"), deptReq, Department.class);
        assertThat(deptResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long deptId = deptResp.getBody().getId();

        // Create employee
        Employee emp = new Employee();
        emp.setFirstName("John");
        emp.setLastName("Doe");
        emp.setEmail("john.doe@example.com");
        ResponseEntity<Employee> createResp = restTemplate.postForEntity(url("/api/employees?departmentId=" + deptId), emp, Employee.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Employee created = createResp.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        Long id = created.getId();

        // Read
        Employee fetched = restTemplate.getForObject(url("/api/employees/" + id), Employee.class);
        assertThat(fetched).isNotNull();
        assertThat(fetched.getFirstName()).isEqualTo("John");
        assertThat(fetched.getDepartment()).isNotNull();

        // List
        ResponseEntity<Employee[]> listResp = restTemplate.getForEntity(url("/api/employees"), Employee[].class);
        assertThat(listResp.getBody()).isNotEmpty();

        // Update
        Employee update = new Employee();
        update.setFirstName("Jane");
        update.setLastName("Doe");
        update.setEmail("jane.doe@example.com");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Employee> updateEntity = new HttpEntity<>(update, headers);
        ResponseEntity<Employee> updResp = restTemplate.exchange(url("/api/employees/" + id + "?departmentId=" + deptId), HttpMethod.PUT, updateEntity, Employee.class);
        assertThat(updResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updResp.getBody()).isNotNull();
        assertThat(updResp.getBody().getFirstName()).isEqualTo("Jane");

        // Delete
        restTemplate.delete(url("/api/employees/" + id));
        ResponseEntity<Employee> afterDelete = restTemplate.getForEntity(url("/api/employees/" + id), Employee.class);
        assertThat(afterDelete.getStatusCode().is4xxClientError() || afterDelete.getStatusCode().is5xxServerError()).isTrue();
    }
}
