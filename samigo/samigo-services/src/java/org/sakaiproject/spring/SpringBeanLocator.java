/**
 * Copyright (c) 2005-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.spring;

import org.springframework.context.ApplicationContext;

public class SpringBeanLocator
{

  // The object callers wait on if the bean locator hasn't been set yet.
  // This is needed because this bean allows the service to get at beans in the tool
  // The job scheduler can run before the webapp has been started and therefore can
  // attempt to get beans out of the tool before the tool has been started up.
  private static final Object waitLock = new Object();
  private static ApplicationContext context = null;

  public static SpringBeanLocator getInstance()
  {
      return new SpringBeanLocator();
  }

  /**
   * For integration inside a web context
   * @param context the WebApplicationContext
   */
  public static void setApplicationContext(ApplicationContext context)
  {
    synchronized(waitLock)
    {
      SpringBeanLocator.context = context;
      waitLock.notifyAll();
    }
  }

  public Object getBean(String name)
  {
    if (context == null) {
      try {
          synchronized (waitLock)
          {
              // Should always wait in a loop.
              while (context == null) {
                // Will release the lock while we are waiting.
                waitLock.wait();
              }
          }
      } catch (InterruptedException e) {
        throw new RuntimeException("Got interrupted waiting for bean to be setup.", e);
      }
    }
    return context.getBean(name);

  }

}
