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

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import org.sakaiproject.tool.assessment.integration.helper.ifc.AgentHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.AuthzHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.PublishingTargetHelper;
import java.io.FileInputStream;
import java.io.InputStream;
import org.sakaiproject.tool.assessment.integration.context.spring.FactoryUtil;

/**
 * This is an abstract class.  It defines the public methods available for
 * the properties that it furnishes.
 *
 * @author Ed Smiley
 */
public abstract class IntegrationContextFactory
{
  private static Log log = LogFactory.getLog(IntegrationContextFactory.class);

  private static final String FS = File.separator;
  private static final String CONFIGURATION =
    "org" + FS + "sakaiproject" + FS + "spring" + FS + "integrationContext.xml";
  private static IntegrationContextFactory instance;

  /**
   * Static method returning an implementation instance of this factory.
   * @return the factory singleton
   */
  public static IntegrationContextFactory getInstance()
  {
    if (instance==null)
    {
      try
      {
        // the instance is provided by Spring-injection
//        Resource res = new ClassPathResource(CONFIGURATION);
//        BeanFactory factory = new XmlBeanFactory(res);
        instance = FactoryUtil.lookup();
//          (IntegrationContextFactory) factory.getBean("integrationContextFactory");
      }
      catch (Exception ex)
      {
        log.error("Unable to read integration context'" +
                  CONFIGURATION + "': " + ex);
      }
    }
    return instance;
  }

  // the factory api
  public abstract boolean isIntegrated();
  public abstract AgentHelper getAgentHelper();
  public abstract AuthzHelper getAuthzHelper();
  public abstract GradebookHelper getGradebookHelper();
  public abstract GradebookServiceHelper getGradeBookServiceHelper();
  public abstract PublishingTargetHelper getPublishingTargetHelper();
}