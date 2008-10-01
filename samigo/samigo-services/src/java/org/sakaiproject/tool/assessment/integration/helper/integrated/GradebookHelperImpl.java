/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/integration/helper/integrated/GradebookHelperImpl.java $
 * $Id: GradebookHelperImpl.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 Sakai Foundation
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


package org.sakaiproject.tool.assessment.integration.helper.integrated;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.api.Placement; 
import org.sakaiproject.tool.cover.ToolManager; 
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookHelper;

/**
 *
 * <p>Description:
 * This is an integrated context implementation helper delegate class for
 * the GradebookFacade class.
 * "Integrated" means that Samigo (Tests and Quizzes)
 * is running within the context of the Sakai portal and authentication
 * mechanisms, and therefore makes calls on Sakai for things it needs.</p>
 * <p>Note: To customize behavior you can add your own helper class to the
 * Spring injection via the integrationContext.xml for your context.
 * The particular integrationContext.xml to be used is selected by the
 * build process.
 * </p>
 * <p>Sakai Project Copyright (c) 2005</p>
 * <p> </p>
 * @author Ed Smiley <esmiley@stanford.edu>
 * based on code originally in GradebookFacade
 */

public class GradebookHelperImpl implements GradebookHelper
{
  private static Log log = LogFactory.getLog(GradebookHelperImpl.class);

  /**
   * Get current gradebook uid.
   * This will *fail* unless called from an integrated Sakai context!
   * @return the current gradebook uid.
   */
  public String getGradebookUId(String siteId)
  {  
    String context;

    Placement placement = null;
    try
    {
      placement = ToolManager.getInstance().getCurrentPlacement();
    }
    catch (Exception ex)
    {
      log.warn(ex);
      placement = null;
    }
    if (placement == null)
    {
      log.warn(
        "getGradebookUId() - no tool placement found, probably taking an " +
        "assessment via URL.");
      if (siteId == null) {
    	  log.warn("getGradebookUId() - siteId is null too.");
          return null;
      }
      else {
    	  return siteId;
      }
    }
    context = placement.getContext();
    return context;
  }

  /**
   * Get the default gradebook uid.
   * @return "Test Gradebook #1" (always)
   */

  public String getDefaultGradebookUId()
  {
    return "Test Gradebook #1";
  }

}
