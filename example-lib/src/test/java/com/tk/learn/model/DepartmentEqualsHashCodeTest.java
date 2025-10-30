package com.tk.learn.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class DepartmentEqualsHashCodeTest {

    @Test
    void equalsHashCode_shouldFollowContract_basedOnIdOnly() {
        EqualsVerifier.forClass(Department.class)
                .usingGetClass()
                .suppress(nl.jqno.equalsverifier.Warning.SURROGATE_KEY)
                .suppress(nl.jqno.equalsverifier.Warning.IDENTICAL_COPY_FOR_VERSIONED_ENTITY)
                // Prefab values for the collection element type to avoid deep graph traversal
                .withPrefabValues(Employee.class,
                        new Employee("A", "B", "a@example.com", null),
                        new Employee("C", "D", "c@example.com", null))
                .verify();
    }
}
