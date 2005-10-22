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
package org.sakaiproject.tool.assessment.integration.context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.integration.context.spring.FactoryUtil;
import org.sakaiproject.tool.assessment.integration.helper.ifc.AgentHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.PublishingTargetHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.SectionAwareServiceHelper;

/**
 * This is an abstract class.  It defines the public methods available for
 * the properties that it furnishes.
 *
 * @author Ed Smiley
 */
public abstract class IntegrationContextFactory
{
  private static Log log = LogFactory.getLog(IntegrationContextFactory.class);
  private static IntegrationContextFactory instance;

  /**
   * Static method returning an implementation instance of this factory.
   * @return the factory singleton
   */
  public static IntegrationContextFactory getInstance()
  {
    log.info("IntegrationContextFactory.getInstance()");
    if (instance==null)
    {
      try
      {
        FactoryUtil.setUseLocator(true);
        instance = FactoryUtil.lookup();
      }
      catch (Exception ex)
      {
        log.error("Unable to read integration context: " + ex);
      }
    }
    log.info("instance="+instance);
    return instance;
  }

  // the factory api
  public abstract boolean isIntegrated();
  public abstract AgentHelper getAgentHelper();
  public abstract GradebookHelper getGradebookHelper();
  public abstract GradebookServiceHelper getGradebookServiceHelper();
  public abstract PublishingTargetHelper getPublishingTargetHelper();
  public abstract SectionAwareServiceHelper getSectionAwareServiceHelper();
}
