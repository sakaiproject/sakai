/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.controllers;

import org.springframework.http.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class EntityController extends AbstractSakaiApiController {

    @Autowired
    private EntityManager entityManager;

    @GetMapping(value = "/tool-entities/tools/{toolId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<Map<String, String>>> getToolEntityMaps(@PathVariable String toolId, @RequestParam List<String> sites) {

        Map<String, List<Map<String, String>>> map = new HashMap<>();
        for (EntityProducer ep : entityManager.getEntityProducers()) {
            if (ep instanceof EntityTransferrer) {
                EntityTransferrer et = (EntityTransferrer) ep;
                if (ArrayUtils.contains(et.myToolIds(), toolId)) {
                    sites.forEach(siteId -> map.put(siteId, et.getEntityMap(siteId)));
                    break;
                }
            }
        }

        return map;
	}
}
