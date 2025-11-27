package com.tk.learn.model.dto;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.Validator;

public record EmployeeReq(String firstName, String lastName, String email) {
    public static final Validator<EmployeeReq> employeeValidator = ValidatorBuilder.<EmployeeReq>of()
            .constraint(EmployeeReq::firstName, "firstName", c -> c
                    .notBlank().message("Name Can't be empty")
                    .lessThanOrEqual(50).message("Name Can't be more than 50 characters"))
            .constraint(EmployeeReq::lastName, "lastName", c -> c
                    .notBlank().message("Last Name Can't be empty")
                    .lessThanOrEqual(50).message("Last Name Can't be more than 50 characters"))
            .constraint(EmployeeReq::email, "email", c -> c
                    .notBlank().message("Email Can't be empty")
                    .lessThanOrEqual(100).message("Email Can't be more than 100 characters")
                    .email().message("Email is not valid"))
            .build();

}
