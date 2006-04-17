/**********************************************************************************
 * $URL$
   * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 *
   * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 *
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
  public String getGradebookUId()
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
        "assessment via URL.  Gradebook not updated.");
      return null;
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
