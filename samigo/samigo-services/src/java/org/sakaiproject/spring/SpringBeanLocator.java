/**********************************************************************************
* $URL$URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/spring/SpringBeanLocator.java $
* $Id$
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/
package org.sakaiproject.spring;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

public class SpringBeanLocator
{

  // The object callers wait on if the bean locator hasn't been set yet.
  // This is needed because this bean allows the service to get at beans in the tool
  // The job scheduler can run before the webapp has been started and therefore can
  // attempt to get beans out of the tool before the tool has been started up.
  private static Object waitLock = new Object();
  private static WebApplicationContext waCtx = null;
  private static ConfigurableApplicationContext caCtx = null;
  private static boolean inWebContext = false;
  private static SpringBeanLocator instance = null;

  public static SpringBeanLocator getInstance()
  {
    //if (instance != null)
    //{
    //  return instance;
    //}
    //else
    //{
      return new SpringBeanLocator();
    //}
  }

  /**
   * For integration inside a web context
   * @param context the WebApplicationContext
   */
  public static void setApplicationContext(WebApplicationContext context)
  {
    synchronized(waitLock)
    {
      SpringBeanLocator.waCtx = context;
      SpringBeanLocator.inWebContext = true;
      waitLock.notifyAll();
    }
  }

  /**
   * Support unit testing via a ConfigurableApplicationContext concrete subclass
   * such as FileSystemXmlApplicationContext
   * @param ca
   */
  public static void setConfigurableApplicationContext(ConfigurableApplicationContext
                                                ca)
  {
    synchronized (waitLock)
    {
      SpringBeanLocator.caCtx = ca;
      SpringBeanLocator.inWebContext = false;
      waitLock.notifyAll();
    }
  }

  public Object getBean(String name)
  {
    if (waCtx == null && caCtx == null) {
      try {
        waitLock.wait();
      } catch (InterruptedException e) {
        throw new RuntimeException("Got interrupted waiting for bean to be setup.", e);
      }
    }
    if (inWebContext)
    {
      return waCtx.getBean(name);
    }
    else
    {
      return caCtx.getBean(name);
    }

  }

}
