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
package org.sakaiproject.tool.assessment.data.dao.assessment;
import java.util.HashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookHelper;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.PublishingTargetHelper;
import org.sakaiproject.api.kernel.component.cover.ComponentManager;
import org.sakaiproject.service.framework.log.Logger;
import org.sakaiproject.service.legacy.site.Site;
import org.sakaiproject.service.legacy.site.SiteService;
import org.sakaiproject.tool.assessment.facade.AgentFacade;

/**
 * <p>Description: Implements the internal publishing target information.
 * Uses helper to determine integration context implementation.</p>
 * <p>Sakai Project Copyright (c) 2005</p>
 * <p> </p>
 * @author Ed Smiley <esmiley@stanford.edu>
 *
 */

public class PublishingTarget implements PublishingTargetHelper
{
  // delegate implementation to helper class
  private static final PublishingTargetHelper helper =
      IntegrationContextFactory.getInstance().getPublishingTargetHelper();
  private static final boolean integrated =
    IntegrationContextFactory.getInstance().isIntegrated();
  private static Log log = LogFactory.getLog(PublishingTarget.class);

  public PublishingTarget() {
  }

  public HashMap getTargets()
  {
    return helper.getTargets();
  }

///////////////////////////////////////////////////////////////////////////////
// The following methods are not fully implemented in the standalone version.
// They are used only in the integrated version,
// so please note that the helper class in the standalone version will
// throw an UnsupportedOperationException, thus we trap them in a try/catch and
// we return null or do nothing (if void) if we encounter an exception.
///////////////////////////////////////////////////////////////////////////////

  /**
   * Set the Sakai site service
   * @param siteService the service
   */
  public SiteService getSiteService()
  {
    try
    {
      return helper.getSiteService();
    }
    catch (Exception ex)
    {
      log.warn("attempting to get site service: " + ex);
      return null;
    }
  }

  /**
   * Set the Sakai site service
   * @param siteService the service
   */
  public void setSiteService(SiteService siteService)
  {
    try
    {
      helper.setSiteService(siteService);
    }
    catch (Exception ex)
    {
      log.warn("attempting to set site service: " + ex);
    }
  }

  /**
   * @return Returns the log.
   */
  public Logger getLog()
  {
    try
    {
      return helper.getLog();
    }
    catch (Exception ex)
    {
      log.warn("attempting to get logger service: " + ex);
      return null;
    }
  }

  /**
   * @param log The log to set.
   */
  public void setLog(Logger logger)
  {
    try
    {
      helper.setLog(logger);
    }
    catch (Exception ex)
    {
      log.warn("attempting to set logger service: " + ex);
    }
  }

  /**
   * for unit test
   * @param args ignored
   */
  public static void main(String[] args)
  {
    unitTest();
  }

  /**
   * Unit test can be run from command line.
   * Bypasses methods that cannot be run outside Sakai.
   * Needs integrationContext.xml in classpath at org.sakaiproject.spring
   */
  public static void unitTest()
  {
    PublishingTarget pt = new PublishingTarget();
    System.out.println("pt.integrated="+pt.integrated);
    System.out.println("pt.helper="+pt.helper);
    System.out.println("pt.getTargets()="+pt.getTargets());
    if (integrated)
    {
      System.out.println(
        "Running integrated version in standalone.  " +
        "Bypassing site and logging services tests.");
      System.out.println("bypassing pt.getLog()");
      System.out.println("bypassing pt.getSiteService()");
      System.out.println("Bypassing: Setting site service");
      System.out.println("Bypassing: Setting logger");
    }
    else
    {
    Logger testLogger = pt.getLog();
    SiteService testSiteService = pt.getSiteService();
    System.out.println("pt.getLog()="+testLogger);
    System.out.println("pt.getSiteService()="+testSiteService);
    // reset to saved off values
    System.out.println("Setting site service");
    pt.setSiteService(testSiteService);
    System.out.println("Setting logger");
    pt.setLog(testLogger);
    }
  }

}
