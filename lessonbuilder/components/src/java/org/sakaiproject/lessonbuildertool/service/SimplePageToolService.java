/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Eric Jeney, jeney@rutgers.edu
 *
 * Copyright (c) 2010 Rutgers, the State University of New Jersey
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

package org.sakaiproject.lessonbuildertool.service;

import java.io.File;
import java.util.List;

import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.lessonbuildertool.LessonBuilderAccessAPI;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.ToolApi;
import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.messaging.api.UserNotificationHandler;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Currently, the sole purpose of this service is to register our edit permission, and create table
 * indices.
 *
 * @author Maarten van Hoof
 *
 */
@Setter
@Slf4j
public class SimplePageToolService implements ResourceLoaderAware, LessonBuilderAccessAPI {

    private boolean autoDdl;
    private FunctionManager functionManager;
    @Getter
    private HttpAccess httpAccess;
    private ResourceLoader resourceLoader;
    private SqlService sqlService;
    private ToolApi toolApi;
    private UserMessagingService userMessagingService;
    private UserNotificationHandler userNotificationHandler;

    public SimplePageToolService() {}

    public void init() {

        log.debug("Initializing Lessons Simple Page Tool Service");

        // for debugging I'd like to be able to reload, so avoid duplicates
        List <String> registered = functionManager.getRegisteredFunctions(SimplePage.PERMISSION_LESSONBUILDER_PREFIX);
        if (!registered.contains(SimplePage.PERMISSION_LESSONBUILDER_UPDATE)) {
            functionManager.registerFunction(SimplePage.PERMISSION_LESSONBUILDER_UPDATE);
        }
        if (!registered.contains(SimplePage.PERMISSION_LESSONBUILDER_READ)) {
            functionManager.registerFunction(SimplePage.PERMISSION_LESSONBUILDER_READ);
        }
        if (!registered.contains(SimplePage.PERMISSION_LESSONBUILDER_SEE_ALL)) {
            functionManager.registerFunction(SimplePage.PERMISSION_LESSONBUILDER_SEE_ALL);
        }

        userMessagingService.registerHandler(this.userNotificationHandler);

        if (autoDdl) {
            try {
                // hibernate will do the tables, but we need this for the indices
                sqlService.ddl(this.getClass().getClassLoader(), "simplepage");
                log.debug("Completed Lesson Builder DDL");
            } catch (Exception e) {
                log.warn("Unable to DDL Lesson Builder: {}", e.toString());
            }
        }
    }

    public Resource getResource(String location){
        return resourceLoader.getResource(location);
    }

    public String loadCartridge(File f, String d, String siteId) {

        log.info("loadcart in simplepagetoolservice {} {} {}", f, d, siteId);
        return toolApi.loadCartridge(f, d, siteId);
    }

    public String deleteOrphanPages(String siteId) {

        log.info("deleteOrphanPages in simplepagetoolservice {}", siteId);
        return toolApi.deleteOrphanPages(siteId);
    }
}
