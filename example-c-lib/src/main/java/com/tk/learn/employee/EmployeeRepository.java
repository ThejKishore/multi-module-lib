package com.tk.learn.employee;

import com.tk.learn.model.dao.Employee;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class EmployeeRepository {

    private final JdbcClient jdbc;

    public EmployeeRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Employee save(Employee employee) {
        if (employee.getId() == null) {
            Long id = insert(employee);
            return findById(id).orElseThrow();
        } else {
            update(employee);
            return findById(employee.getId()).orElseThrow();
        }
    }

    public Optional<Employee> findById(Long id) {
        String sql = "SELECT id, first_name, last_name, email FROM employees WHERE id = ?";
        return jdbc.sql(sql)
                .param(id)
                .query(this::mapEmployee)
                .optional();
    }

    public List<Employee> findAll() {
        String sql = "SELECT id, first_name, last_name, email FROM employees ORDER BY id";
        return jdbc.sql(sql)
                .query(this::mapEmployee)
                .list();
    }

    public boolean existsById(Long id) {
        Integer one = jdbc.sql("SELECT 1 FROM employees WHERE id = ?")
                .param(id)
                .query(Integer.class)
                .optional()
                .orElse(null);
        return one != null;
    }

    public void deleteById(Long id) {
        jdbc.sql("DELETE FROM employees WHERE id = ?").param(id).update();
    }

    private Long insert(Employee e) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.sql("INSERT INTO employees(first_name, last_name, email) VALUES (?,?,?)")
                .param(e.getFirstName())
                .param(e.getLastName())
                .param(e.getEmail())
                .update(kh);
        Number key = kh.getKey();
        return key == null ? null : key.longValue();
    }

    private void update(Employee e) {
        jdbc.sql("UPDATE employees SET first_name = ?, last_name = ?, email = ? WHERE id = ?")
                .param(e.getFirstName())
                .param(e.getLastName())
                .param(e.getEmail())
                .param(e.getId())
                .update();
    }

    private Employee mapEmployee(ResultSet rs, int rowNum) throws SQLException {
        Employee e = new Employee();
        e.setId(rs.getLong("id"));
        e.setFirstName(rs.getString("first_name"));
        e.setLastName(rs.getString("last_name"));
        e.setEmail(rs.getString("email"));
        return e;
    }
}
