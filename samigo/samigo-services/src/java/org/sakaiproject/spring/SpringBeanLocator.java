/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/samigo-app/src/java/org/sakaiproject/tool/assessment/ui/listener/deli\
very/UpdateTimerListener.java $
* $Id: UpdateTimerListener.java 13802 2006-08-16 23:13:50Z ktsao@stanford.edu $
***********************************************************************************
*
* Copyright (c) 2005, 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 1.0 (the"License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
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

  //private static Log log = LogFactory.getLog(SpringBeanLocator.class);
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
  public static void setApplicationContext(WebApplicationContext context)
  {
	  SpringBeanLocator.waCtx = context;
	  SpringBeanLocator.inWebContext = true;
  }

  /**
   * Support unit testing via a ConfigurableApplicationContext concrete subclass
   * such as FileSystemXmlApplicationContext
   * @param ca
   */
  public static void setConfigurableApplicationContext(ConfigurableApplicationContext
                                                ca)
  {
    SpringBeanLocator.caCtx = ca;
    SpringBeanLocator.inWebContext = false;
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
