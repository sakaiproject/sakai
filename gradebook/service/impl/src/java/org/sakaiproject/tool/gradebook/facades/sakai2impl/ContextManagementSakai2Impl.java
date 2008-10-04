/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006 Sakai Foundation, the MIT Corporation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.facades.sakai2impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.gradebook.facades.ContextManagement;

/**
 * Gets the current context for a gradebook inside sakai2.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class ContextManagementSakai2Impl implements ContextManagement {
    private static final Log log = LogFactory.getLog(ContextManagementSakai2Impl.class);
    private static final String DEFAULT_GRADEBOOK_NAME = "QA_6";

	/**
	 * @see org.sakaiproject.tool.gradebook.facades.ContextManagement#getGradebookUid(java.lang.Object)
	 */
	public String getGradebookUid(Object notNeededInSakai) {
        // get the Tool Placement, and return the tool's context if available
        Placement placement = ToolManager.getCurrentPlacement();
        if(placement == null) {
            log.error("Placement is null");
            return DEFAULT_GRADEBOOK_NAME;
        }

        return placement.getContext();
	}
}


