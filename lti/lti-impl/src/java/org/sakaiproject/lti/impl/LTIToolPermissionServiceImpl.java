/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.lti.impl;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.Set;

import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.lti.api.LTIToolPermissionService;
import org.sakaiproject.lti.api.model.LtiTool;
import org.sakaiproject.lti.api.repository.LtiToolRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LTIToolPermissionServiceImpl implements LTIToolPermissionService {

    @Autowired
    private FunctionManager functionManager;

    @Autowired
    private LtiToolRepository toolRepository;

    @Override
    @Transactional(readOnly = true)
    public Set<String> getToolPermissions(Long toolId) {

        return toolRepository.findById(toolId)
            .map(t -> Set.copyOf(t.getPermissions()))
            .orElseGet(() -> {

                log.warn("No tool for id {}", toolId);
                return Set.of();
            });
    }

    @Override
    @Transactional
    public void setToolPermissions(Long toolId, Set<String> permissions, String siteId) throws Exception {

        LtiTool tool = toolRepository.findById(toolId).orElseThrow(() -> new Exception("No tool for id " + toolId));

        Set<String> toInsert = new LinkedHashSet<>();
        if (permissions != null) {
            Set<String> registered = new HashSet<>(functionManager.getRegisteredFunctions());
            for (String permission : permissions) {
                if (!registered.contains(permission)) {
                    log.warn("Ignoring unregistered function {} for tool {}", permission, toolId);
                    continue;
                }

                toInsert.add(permission);
            }
        }

        tool.setPermissions(toInsert);

        toolRepository.save(tool);
    }

    @Override
    @Transactional
    public void deleteToolPermissions(Long toolId) {

        toolRepository.findById(toolId).ifPresentOrElse(tool -> {

          tool.getPermissions().clear();
          toolRepository.save(tool);
        }, () -> log.warn("No tool for id {}", toolId));
    }
}

