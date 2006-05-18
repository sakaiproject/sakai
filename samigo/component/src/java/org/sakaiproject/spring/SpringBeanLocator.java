/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

public class SpringBeanLocator
{

  private static Log log = LogFactory.getLog(SpringBeanLocator.class);
  private static WebApplicationContext waCtx = null;
  private static ConfigurableApplicationContext caCtx = null;
  private static boolean inWebContext = false;
  private static SpringBeanLocator instance = null;

  public static SpringBeanLocator getInstance()
  {
    if (instance != null)
    {
      return instance;
    }
    else
    {
      return new SpringBeanLocator();
    }
  }

  /**
   * For integration inside a web context
   * @param context the WebApplicationContext
   */
  public void setApplicationContext(WebApplicationContext context)
  {
    this.waCtx = context;
    inWebContext = true;
  }

  /**
   * Support unit testing via a ConfigurableApplicationContext concrete subclass
   * such as FileSystemXmlApplicationContext
   * @param ca
   */
  public void setConfigurableApplicationContext(ConfigurableApplicationContext
                                                ca)
  {
    this.caCtx = ca;
    inWebContext = false;
  }

  public Object getBean(String name)
  {
    if (inWebContext)
    {
      //log.info("** context in Locator " + waCtx);
      return waCtx.getBean(name);
    }
    else
    {
      //log.info("** context in Locator " + caCtx);
      return caCtx.getBean(name);
    }

  }

}
