package com.tk.learn.department;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tk.learn.model.Department;
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

@WebMvcTest(controllers = DepartmentController.class)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DepartmentService service;

    @Test
    void create_returns201_andBody() throws Exception {
        Department req = new Department("Engineering");
        Department resp = new Department("Engineering");
        resp.setId(1L);
        Mockito.when(service.create(any(Department.class))).thenReturn(resp);

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/departments/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Engineering"));
    }

    @Test
    void get_returnsDepartment() throws Exception {
        Department d = new Department("Engineering");
        d.setId(5L);
        Mockito.when(service.get(5L)).thenReturn(d);

        mockMvc.perform(get("/api/departments/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Engineering"));
    }

    @Test
    void list_returnsArray() throws Exception {
        Department a = new Department("A"); a.setId(1L);
        Department b = new Department("B"); b.setId(2L);
        Mockito.when(service.list()).thenReturn(List.of(a, b));

        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("A"))
                .andExpect(jsonPath("$[1].name").value("B"));
    }

    @Test
    void update_returnsUpdated() throws Exception {
        Department patch = new Department("Platform");
        Department updated = new Department("Platform");
        updated.setId(3L);
        Mockito.when(service.update(eq(3L), any(Department.class))).thenReturn(updated);

        mockMvc.perform(put("/api/departments/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Platform"));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/departments/9"))
                .andExpect(status().isNoContent());
        Mockito.verify(service).delete(9L);
    }
}
