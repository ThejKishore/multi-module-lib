package com.tk.learn.department;

import com.tk.learn.model.Department;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@org.springframework.context.annotation.Import(JpaTestConfig.class)
class DepartmentRepositoryTest {

    @Autowired
    private DepartmentRepository repository;

    @Test
    void save_find_update_delete_crudFlow() {
        // create
        Department d = new Department("Engineering");
        Department saved = repository.save(d);
        assertThat(saved.getId()).isNotNull();

        // read
        Optional<Department> byId = repository.findById(saved.getId());
        assertThat(byId).isPresent();
        assertThat(byId.get().getName()).isEqualTo("Engineering");

        // update
        byId.get().setName("Platform");
        Department updated = repository.save(byId.get());
        assertThat(updated.getName()).isEqualTo("Platform");

        // list
        assertThat(repository.findAll()).extracting(Department::getName).contains("Platform");

        // delete
        repository.deleteById(saved.getId());
        assertThat(repository.findById(saved.getId())).isEmpty();
    }
}
