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
package org.sakaiproject.tool.assessment.integration.context.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.AgentHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.PublishingTargetHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.SectionAwareServiceHelper;


/**
 * IntegrationContext is an internal implementation of IntegrationContextFactory.
 * It is the implementation class actually used by Spring and returned by its
 * abstract superclasses' (IntegrationContextFactory) getInstance method.
 * @author Ed Smiley
 */
public class IntegrationContext extends IntegrationContextFactory
{
  private static Log log = LogFactory.getLog(IntegrationContext.class);

  private boolean integrated;
  private AgentHelper agentHelper;
  private GradebookHelper gradebookHelper;
  private GradebookServiceHelper gradebookServiceHelper;
  private PublishingTargetHelper publishingTargetHelper;
  private SectionAwareServiceHelper sectionAwareServiceHelper;

  // plain old Java bean properties, nothing mysterious here
  // just that we add mutators for Spring to hook, these are not
  // part of the factory api, so IntegrationContextFactory doesn't have
  // the setXXX() methods.
  public boolean isIntegrated()
  {
    return integrated;
  }
  public void setIntegrated(boolean integrated)
  {
    this.integrated = integrated;
  }
  public AgentHelper getAgentHelper()
  {
    return agentHelper;
  }
  public void setAgentHelper(AgentHelper agentHelper)
  {
    this.agentHelper = agentHelper;
  }
  public GradebookHelper getGradebookHelper()
  {
    return gradebookHelper;
  }
  public void setGradebookHelper(GradebookHelper gradebookHelper)
  {
    this.gradebookHelper = gradebookHelper;
  }
  public GradebookServiceHelper getGradebookServiceHelper()
  {
    return gradebookServiceHelper;
  }
  public void setGradebookServiceHelper(GradebookServiceHelper gradebookServiceHelper)
  {
    this.gradebookServiceHelper = gradebookServiceHelper;
  }
  public PublishingTargetHelper getPublishingTargetHelper()
  {
    return publishingTargetHelper;
  }
  public void setPublishingTargetHelper(PublishingTargetHelper publishingTargetHelper)
  {
    this.publishingTargetHelper = publishingTargetHelper;
  }

  public SectionAwareServiceHelper getSectionAwareServiceHelper()
  {
    return sectionAwareServiceHelper;
  }
  public void setSectionAwareServiceHelper(SectionAwareServiceHelper sectionAwareServiceHelper)
  {
    this.sectionAwareServiceHelper = sectionAwareServiceHelper;
  }
}
