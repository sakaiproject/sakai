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
package org.sakaiproject.tool.assessment.integration.helper.standalone;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.service.framework.log.Logger;
import org.sakaiproject.service.legacy.site.SiteService;
import org.sakaiproject.tool.assessment.integration.helper.ifc.PublishingTargetHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.*;

/**
 *
 * <p>Description:
 * This is a stub standalone context implementation helper delegate class for
 * the PublishingTarget class.  All but one method is unimplemented.
 * "Standalone" means that Samigo (Tests and Quizzes)
 * is running without the context of the Sakai portal and authentication
 * mechanisms, and therefore we leave standalone-unused methods unimplemented.</p>
 * <p>Note: To customize behavior you can add your own helper class to the
 * Spring injection via the integrationContext.xml for your context.
 * The particular integrationContext.xml to be used is selected by the
 * build process.
 * </p>
 * <p>Sakai Project Copyright (c) 2005</p>
 * <p> </p>
 * @author Ed Smiley <esmiley@stanford.edu>
 *
 */

public class PublishingTargetHelperImpl implements PublishingTargetHelper
{
  private static Log log = LogFactory.getLog(PublishingTargetHelperImpl.class);

  public HashMap getTargets()
  {
     HashMap map = new HashMap();
     map.put("Anonymous Users", "ANONYMOUS_USERS");
     map.put("Authenticated Users", "AUTHENTICATED_USERS");
     return map;
  }

///////////////////////////////////////////////////////////////////////////////
// The following methods are not implemented in the standalone version of
// PublishingTarget.  Currently they are not used in the standalone context,
// so they are left throwing UnsupportedOperationExceptions.
///////////////////////////////////////////////////////////////////////////////

  /**
   * UNIMPLEMENTED.
   * @todo If required for standalone context at some point in the future,
   * you'll need to implement this
   * org.sakaiproject.tool.assessment.integration.helper.ifc.PublishingTargetHelper method
   * @return throw UnsupportedOperationException
   */
  public SiteService getSiteService()
  {
    throw new java.lang.UnsupportedOperationException(
      "Method getSiteService() not yet implemented for standalone context.");
  }

  /**
   * UNIMPLEMENTED.
   * @todo If required for standalone context at some point in the future,
   * you'll need to implement this
   * org.sakaiproject.tool.assessment.integration.helper.ifc.PublishingTargetHelper method
   * @param siteService
   */
  public void setSiteService(SiteService siteService)
  {
    throw new java.lang.UnsupportedOperationException(
      "Method setSiteService() not yet implemented for standalone context.");
  }

  /**
   * UNIMPLEMENTED.
   * @todo If required for standalone context at some point in the future,
   * you'll need to implement this
   * org.sakaiproject.tool.assessment.integration.helper.ifc.PublishingTargetHelper method
   * @return throw UnsupportedOperationException
   */
  public Logger getLog()
  {
    throw new java.lang.UnsupportedOperationException(
      "Method getLog() not yet implemented for standalone context.");
  }

  /**
   * UNIMPLEMENTED.
   * @todo If required for standalone context at some point in the future,
   * you'll need to implement this
   * org.sakaiproject.tool.assessment.integration.helper.ifc.PublishingTargetHelper method
   * @param log the logger
   */
  public void setLog(Logger log)
  {
    throw new java.lang.UnsupportedOperationException(
      "Method setLog() not yet implemented for standalone context.");
  }
}