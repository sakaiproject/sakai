package org.sakaiproject.webapi.controllers;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.authz.api.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class UserController extends AbstractSakaiApiController{

    @Autowired
    private SecurityService securityService;

    @GetMapping("/user/roles")
    public ResponseEntity<Map<String, Boolean>> getUserRoles() {

        Map<String, Boolean> roles = new HashMap<>();
        roles.put("isSuperUser", securityService.isSuperUser());

        return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_JSON)
          .body(roles);
    }

}
