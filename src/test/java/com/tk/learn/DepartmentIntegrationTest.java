package com.tk.learn;

import com.tk.learn.model.Department;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DepartmentIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void departmentCrudFlow() {
        // Create
        Department req = new Department("Engineering");
        ResponseEntity<Department> createResp = restTemplate.postForEntity(url("/api/departments"), req, Department.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Department created = createResp.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        Long id = created.getId();

        // Read
        Department fetched = restTemplate.getForObject(url("/api/departments/" + id), Department.class);
        assertThat(fetched).isNotNull();
        assertThat(fetched.getName()).isEqualTo("Engineering");

        // List
        ResponseEntity<Department[]> listResp = restTemplate.getForEntity(url("/api/departments"), Department[].class);
        assertThat(listResp.getBody()).isNotEmpty();

        // Update
        Department update = new Department("R&D");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Department> updateEntity = new HttpEntity<>(update, headers);
        ResponseEntity<Department> updResp = restTemplate.exchange(url("/api/departments/" + id), HttpMethod.PUT, updateEntity, Department.class);
        assertThat(updResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updResp.getBody()).isNotNull();
        assertThat(updResp.getBody().getName()).isEqualTo("R&D");

        // Delete
        restTemplate.delete(url("/api/departments/" + id));
        ResponseEntity<Department> afterDelete = restTemplate.getForEntity(url("/api/departments/" + id), Department.class);
        assertThat(afterDelete.getStatusCode().is4xxClientError() || afterDelete.getStatusCode().is5xxServerError()).isTrue();
    }
}
