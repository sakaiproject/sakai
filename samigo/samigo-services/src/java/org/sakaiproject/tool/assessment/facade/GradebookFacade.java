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

import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookHelper;

/**
 * <p>Description: Implements the internal gradebook information.
 * Uses helper to determine integration context impelmentation.</p>
 * <p>Sakai Project Copyright (c) 2005</p>
 * <p> </p>
 * @author Ed Smiley <esmiley@stanford.edu>
 *
 */
public class GradebookFacade implements Serializable
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
  private static final GradebookHelper helper =
      IntegrationContextFactory.getInstance().getGradebookHelper();
  private static final boolean integrated =
    IntegrationContextFactory.getInstance().isIntegrated();

  /**
   * Get current gradebook uid.
   * @return the current gradebook uid.
   */
  public static String getGradebookUId(String siteId)
  {
    return helper.getGradebookUId(siteId);
  }
  
  public static String getGradebookUId()
  {
    return getGradebookUId(null);
  }

  /**
 * Get the default gradebook uid.
 * @return "Test Gradebook #1" (always)
 */
  public static String getDefaultGradebookUId()
  {
    return helper.getDefaultGradebookUId();
  }
}
