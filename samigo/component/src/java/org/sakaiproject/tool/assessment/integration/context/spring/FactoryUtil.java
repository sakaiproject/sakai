/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/sam/tool/src/java/org/sakaiproject/tool/assessment/integration/context/IntegrationContextFactory.java $
 * $Id: IntegrationContextFactory.java 2008 2005-09-23 20:01:57Z esmiley@stanford.edu $
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

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.spring.SpringBeanLocator;

/**
 * Encapsulates the Spring lookup for this factory.
 * @author Ed smiley
 */
public class FactoryUtil
{
  private static Log log = LogFactory.getLog(FactoryUtil.class);
  private static boolean useLocator = false;
//  private static boolean useLocator = true;

  private static final String FS = File.separator;
  private static final String CONFIGURATION =
    "org" + FS + "sakaiproject" + FS + "spring" + FS + "integrationContext.xml";

  public static IntegrationContextFactory lookup() throws Exception
  {
    // the instance is provided by Spring-injection
    if (useLocator)
    {

    SpringBeanLocator locator = SpringBeanLocator.getInstance();
    return
      (IntegrationContextFactory) locator.getBean("integrationContextFactory");
    }
    else // unit testing
    {
      Resource res = new ClassPathResource(CONFIGURATION);
      BeanFactory factory = new XmlBeanFactory(res);
      return
        (IntegrationContextFactory) factory.getBean("integrationContextFactory");
    }

  }
  public static boolean getUseLocator()
  {
    return useLocator;
  }

  public static void setUseLocator(boolean use)
  {
    useLocator = use;
  }

}