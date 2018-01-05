/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.tool.section.facade.sakai;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.section.api.facade.manager.Context;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;

/**
 * Uses Sakai's ToolManager to determine the current context.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
@Slf4j
public class ContextSakaiImpl implements Context {

	/**
	 * @inheritDoc
	 */
	public String getContext(Object request) {
        Placement placement = ToolManager.getCurrentPlacement();        
        if(placement == null) {
            log.error("Placement is null");
            return null;
        }
        return placement.getContext();
	}

	public String getContextTitle(Object request) {
		String siteContext = getContext(null);
		String siteTitle = null;
		try {
			siteTitle = SiteService.getSite(siteContext).getTitle();
		} catch (IdUnusedException e) {
			log.error("Unable to get site for context " + siteContext);
			siteTitle = siteContext; // Better than nothing???
		}
		return siteTitle;
	}
}
