package com.tk.learn.model.contract;


import com.tk.learn.model.dto.EmployeeReq;
import com.tk.learn.model.dto.EmployeeResp;
import org.springframework.http.ResponseEntity;
import org.springframework.web.service.annotation.*;

import java.util.List;

@HttpExchange("/api/employees")
public interface EmployeeRestClient {

    @PostExchange
    ResponseEntity<EmployeeResp> create(EmployeeReq employee) ;
    
    @GetExchange("/{id}")
    EmployeeResp get(Long id);

    @GetExchange
     List<EmployeeResp> list();

    @PutExchange("/{id}")
    ResponseEntity<EmployeeResp> update(Long id,EmployeeReq employee);

    @DeleteExchange("/{id}")
    void delete( Long id);
}
