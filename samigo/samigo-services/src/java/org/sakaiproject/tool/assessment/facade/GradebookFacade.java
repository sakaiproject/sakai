/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.facade;

import java.io.Serializable;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;

/**
 * <p>Description: Implements the internal gradebook information.
 * Resolves the Sakai site ID used by Gradebook.</p>
 * <p>Sakai Project Copyright (c) 2005</p>
 * <p> </p>
 * @author Ed Smiley <esmiley@stanford.edu>
 *
 */
@Slf4j
public class GradebookFacade implements Serializable
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

  /**
   * Get current gradebook uid.
   * @return the current gradebook uid.
   */
  public static String getGradebookUId(String siteId)
  {
    Placement placement = null;
    try {
      placement = ToolManager.getCurrentPlacement();
    } catch (Exception e) {
      log.warn("Unable to resolve current tool placement for gradebook", e);
    }
    if (placement != null) {
      return placement.getContext();
    }
    log.warn("No tool placement available for gradebook; using site ID {}", siteId);
    return siteId;
  }
  
  public static String getGradebookUId()
  {
    return getGradebookUId(null);
  }

}
