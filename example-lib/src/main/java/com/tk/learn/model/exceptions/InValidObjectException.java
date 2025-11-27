package com.tk.learn.model.exceptions;

import am.ik.yavi.core.ViolationDetail;
import am.ik.yavi.fn.Pair;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InValidObjectException extends RuntimeException{
    private  final transient List<ViolationDetail> constraintViolations;
    public InValidObjectException(List<ViolationDetail> constraintViolations) {
        super();
        this.constraintViolations = constraintViolations;
    }

    public Map<String,String> getErrors(){
        return constraintViolations.stream()
                .map(c -> new Pair<String,String>(c.getKey() , c.getDefaultMessage()))
                .collect(Collectors.toMap(Pair::first,Pair::second));
    }

    public List<ViolationDetail> getConstraintViolations() {
        return constraintViolations;
    }

}
