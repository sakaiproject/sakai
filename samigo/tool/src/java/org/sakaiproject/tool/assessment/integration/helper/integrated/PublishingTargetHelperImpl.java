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
 *
 * <p>Description:
 * This is an integrated context implementation helper delegate class for
 * the PublishingTarget class.
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
 */

public class PublishingTargetHelperImpl implements PublishingTargetHelper
{
  private static Log log = LogFactory.getLog(PublishingTargetHelperImpl.class);

  private Logger logger;
  private SiteService siteService;
  private org.sakaiproject.api.kernel.component.ComponentManager cm;

  private static String LOGGER_SERVICE = "org.sakaiproject.service.framework.log.cover.Logger";
  private static String SITE_SERVICE = "org.sakaiproject.service.legacy.site.SiteService";

  public PublishingTargetHelperImpl()
  {
  }


  /**
   * Gets to whom you can publish.
   * @return map of key value pairs:
   *  e.g. "Authenticated Users"->"AUTHENTICATED_USERS"
   */
  public HashMap getTargets()
  {

     HashMap map = new HashMap();
     map.put("Anonymous Users", "ANONYMOUS_USERS");
     map.put(AgentFacade.getCurrentSiteName(), AgentFacade.getCurrentSiteId());
     return map;
  }

  /**
   * @return Returns the siteService.
   */
  public SiteService getSiteService()
  {
    cm = ComponentManager.getInstance();
    siteService = (org.sakaiproject.service.legacy.site.SiteService) cm.get(SITE_SERVICE);
    return siteService;
  }
  /**
   * @param siteService The siteService to set.
   */
  public void setSiteService(SiteService siteService)
  {
    this.siteService = siteService;
  }
  /**
   * @return Returns the log.
   */
  public Logger getLog()
  {
    cm = ComponentManager.getInstance();
    logger = (org.sakaiproject.service.framework.log.Logger) cm.get(LOGGER_SERVICE);
    return logger;
  }
  /**
   * @param log The log to set.
   */
  public void setLog(Logger logger)
  {
    this.logger = logger;
  }

}