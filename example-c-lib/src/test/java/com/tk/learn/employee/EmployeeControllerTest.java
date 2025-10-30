package com.tk.learn.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tk.learn.model.Employee;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService service;

    @Test
    void create_withDepartmentId_returns201() throws Exception {
        Employee req = new Employee();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmail("john.doe@example.com");

        Employee resp = new Employee();
        resp.setId(1L);
        resp.setFirstName("John");
        resp.setLastName("Doe");
        resp.setEmail("john.doe@example.com");
        Mockito.when(service.create(any(Employee.class), eq(5L))).thenReturn(resp);

        mockMvc.perform(post("/api/employees?departmentId=5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/employees/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void get_returnsEmployee() throws Exception {
        Employee e = new Employee();
        e.setId(7L);
        e.setFirstName("Jane");
        e.setLastName("Doe");
        e.setEmail("jane.doe@example.com");
        Mockito.when(service.get(7L)).thenReturn(e);

        mockMvc.perform(get("/api/employees/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    void list_returnsArray() throws Exception {
        Employee a = new Employee(); a.setId(1L); a.setFirstName("A");
        Employee b = new Employee(); b.setId(2L); b.setFirstName("B");
        Mockito.when(service.list()).thenReturn(List.of(a, b));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void update_returnsUpdated() throws Exception {
        Employee patch = new Employee();
        patch.setFirstName("New");
        patch.setLastName("Name");
        Employee updated = new Employee(); updated.setId(3L); updated.setFirstName("New");
        Mockito.when(service.update(eq(3L), any(Employee.class), eq(9L))).thenReturn(updated);

        mockMvc.perform(put("/api/employees/3?departmentId=9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.firstName").value("New"));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/employees/4"))
                .andExpect(status().isNoContent());
        Mockito.verify(service).delete(4L);
    }
}
