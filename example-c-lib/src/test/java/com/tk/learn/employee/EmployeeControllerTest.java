package com.tk.learn.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tk.learn.model.dao.Employee;
import com.tk.learn.model.dto.EmployeeReq;
import com.tk.learn.web.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EmployeeControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private EmployeeService service;
    private EmployeeController controller;

    @BeforeEach
    void setup() {
        this.objectMapper = new ObjectMapper();
        this.service = mock(EmployeeService.class);
        this.controller = new EmployeeController(service);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/employees should create and return 201 with Location header")
    void create_shouldReturn201() throws Exception {
        EmployeeReq req = new EmployeeReq("John", "Doe", "john@ex.com");
        Employee created = new Employee(1L, "John", "Doe", "john@ex.com");
        given(service.create(any(Employee.class))).willReturn(created);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/employees/1"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john@ex.com"));
    }

    @Test
    @DisplayName("POST /api/employees with invalid payload should return 400 with error body")
    void create_shouldReturn400_whenInvalid() throws Exception {
        // invalid: blank names and invalid email
        EmployeeReq req = new EmployeeReq("", "", "not-an-email");

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName", not(emptyString())))
                .andExpect(jsonPath("$.lastName", not(emptyString())))
                .andExpect(jsonPath("$.email", not(emptyString())));
    }

    @Test
    @DisplayName("GET /api/employees/{id} should return 200 with employee")
    void get_shouldReturn200() throws Exception {
        given(service.get(1L)).willReturn(new Employee(1L, "A", "B", "a@b.com"));

        mockMvc.perform(get("/api/employees/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("A"))
                .andExpect(jsonPath("$.email").value("a@b.com"));
    }

    @Test
    @DisplayName("GET /api/employees/{id} should return 404 when not found")
    void get_shouldReturn404_whenNotFound() throws Exception {
        given(service.get(9L)).willThrow(new NoSuchElementException("Employee not found: 9"));

        mockMvc.perform(get("/api/employees/{id}", 9L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("Employee not found")));
    }

    @Test
    @DisplayName("GET /api/employees should return list of employees")
    void list_shouldReturnEmployees() throws Exception {
        given(service.list()).willReturn(List.of(
                new Employee(1L, "A", "B", "a@b.com"),
                new Employee(2L, "C", "D", "c@d.com")
        ));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].email").value("c@d.com"));
    }

    @Test
    @DisplayName("PUT /api/employees/{id} should update and return employee")
    void update_shouldReturn200() throws Exception {
        Long id = 3L;
        EmployeeReq req = new EmployeeReq("New", "Name", "new@ex.com");
        Employee existing = new Employee(id, "Old", "Name", "old@ex.com");
        Employee updated = new Employee(id, "New", "Name", "new@ex.com");

        given(service.get(id)).willReturn(existing);
        given(service.update(eq(id), any(Employee.class))).willReturn(updated);

        mockMvc.perform(put("/api/employees/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.firstName").value("New"))
                .andExpect(jsonPath("$.email").value("new@ex.com"));
    }

    @Test
    @DisplayName("PUT /api/employees/{id} with invalid payload should return 400")
    void update_shouldReturn400_whenInvalid() throws Exception {
        EmployeeReq req = new EmployeeReq("", "", "bad");
        mockMvc.perform(put("/api/employees/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email", not(emptyString())));
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} should return 204 when deleted")
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/employees/{id}", 7L))
                .andExpect(status().isNoContent());
        Mockito.verify(service).delete(7L);
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} should return 404 when not found")
    void delete_shouldReturn404_whenNotFound() throws Exception {
        Mockito.doThrow(new NoSuchElementException("Employee not found: 8"))
                .when(service).delete(8L);

        mockMvc.perform(delete("/api/employees/{id}", 8L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("Employee not found")));
    }
}
