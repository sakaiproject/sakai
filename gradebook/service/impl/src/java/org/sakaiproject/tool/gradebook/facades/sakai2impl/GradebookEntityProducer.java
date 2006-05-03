/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005, 2006 The Regents of the University of California, The MIT Corporation
*
* Licensed under the Educational Community License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.facades.sakai2impl;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.entity.api.ContextObserver;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;

/**
 * Implements the Sakai EntityProducer approach to integration of tool-specific
 * storage with site management.
 */
public class GradebookEntityProducer extends BaseEntityProducer implements ContextObserver {
    private static final Log log = LogFactory.getLog(GradebookEntityProducer.class);

    private String[] toolIdArray;
    private GradebookService gradebookService;

	public void setToolIds(List toolIds) {
		if (log.isDebugEnabled()) log.debug("setToolIds(" + toolIds + ")");
		if (toolIds != null) {
			toolIdArray = (String[])toolIds.toArray(new String[0]);
		}
	}

	public String[] myToolIds() {
		return toolIdArray;
	}

	public void contextCreated(String context, boolean toolPlacement) {
		// Only create Gradebook storage if the Gradebook tool is actually
		// part of the new site.
		if (toolPlacement && !gradebookService.isGradebookDefined(context)) {
			if (log.isInfoEnabled()) log.info("Gradebook being added to context " + context);
			gradebookService.addGradebook(context, context);
		}
	}

	public void contextUpdated(String context, boolean toolPlacement) {
		if (toolPlacement) {
			if (!gradebookService.isGradebookDefined(context)) {
				if (log.isInfoEnabled()) log.info("Gradebook being added to context " + context);
				gradebookService.addGradebook(context, context);
			}
		} else {
			if (gradebookService.isGradebookDefined(context)) {
				// We've been directed to leave Gradebook data in place when
				// the tool is removed from a site, just in case the site
				// owner changes their mind later.
				if (log.isInfoEnabled()) log.info("Gradebook removed from context " + context + " but associated data will remain until context deletion");
			}
		}
	}

	public void contextDeleted(String context, boolean toolPlacement) {
		if (gradebookService.isGradebookDefined(context)) {
			if (log.isInfoEnabled()) log.info("Gradebook being deleted from context " + context);
			try {
				gradebookService.deleteGradebook(context);
			} catch (GradebookNotFoundException e) {
				if (log.isWarnEnabled()) log.warn(e);
			}
		}
	}

	public void setGradebookService(GradebookService gradebookService) {
		this.gradebookService = gradebookService;
	}
}
