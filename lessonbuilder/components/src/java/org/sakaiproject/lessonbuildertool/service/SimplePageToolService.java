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
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Currently, the sole purpose of this service is to register our edit permission, and create table
 * indices.
 * 
 * @author Maarten van Hoof
 * 
 */
@Slf4j
public class SimplePageToolService implements ResourceLoaderAware, LessonBuilderAccessAPI {

	SqlService sqlService = null;
	boolean autoDdl = false;

	private ResourceLoader resourceLoader;
 
	public void setResourceLoader(ResourceLoader resourceLoader) {
	    this.resourceLoader = resourceLoader;
	}
 
	@Setter
	private FunctionManager functionManager;
	
	public Resource getResource(String location){
	    return resourceLoader.getResource(location);
	}

        private HttpAccess httpAccess = null;
        public void setHttpAccess(HttpAccess h) {
	    httpAccess = h;
	}

	private ToolApi toolApi = null;
	public void setToolApi(ToolApi t) {
	    toolApi = t;
	}

	public HttpAccess getHttpAccess() {
	    return httpAccess;
	}

	public void setSqlService(SqlService sqlService) {
		this.sqlService = sqlService;
	}

	public void setAutoDdl(boolean autoDdl) {
		this.autoDdl = autoDdl;
	}

	public String loadCartridge(File f, String d, String siteId) {
	    log.info("loadcart in simplepagetoolservice " + f + " " + d + " " + siteId);
	    return toolApi.loadCartridge(f, d, siteId);
	}

	public String deleteOrphanPages(String siteId) {
	    log.info("deleteOrphanPages in simplepagetoolservice " + siteId);
	    return toolApi.deleteOrphanPages(siteId);
	}

	public SimplePageToolService() {}

	public void init() {

		log.info("Initializing Lesson Builder Tool");

		// for debugging I'd like to be able to reload, so avoid duplicates
		List <String> registered = functionManager.getRegisteredFunctions(SimplePage.PERMISSION_LESSONBUILDER_PREFIX);
		if (registered == null || !registered.contains(SimplePage.PERMISSION_LESSONBUILDER_UPDATE))
		    functionManager.registerFunction(SimplePage.PERMISSION_LESSONBUILDER_UPDATE);
		if (registered == null || !registered.contains(SimplePage.PERMISSION_LESSONBUILDER_READ))
		    functionManager.registerFunction(SimplePage.PERMISSION_LESSONBUILDER_READ);
		if (registered == null || !registered.contains(SimplePage.PERMISSION_LESSONBUILDER_SEE_ALL))
		    functionManager.registerFunction(SimplePage.PERMISSION_LESSONBUILDER_SEE_ALL);

		try {
			// hibernate will do the tables, but we need this for the indices
		    if (autoDdl) {
			sqlService.ddl(this.getClass().getClassLoader(), "simplepage");
			log.info("Completed Lesson Builder DDL");
		    }
		} catch (Exception e) {
			log.warn("Unable to DDL Lesson Builder", e);
		}

	}

}
