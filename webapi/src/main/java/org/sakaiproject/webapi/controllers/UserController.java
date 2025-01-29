/*
 * Copyright (c) 2003-2024 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    public ResponseEntity<Map<String, Boolean>> checkSuperUser() {

        return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_JSON)
          .body(Map.of("isSuperUser", securityService.isSuperUser()));
    }

}
