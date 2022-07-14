/**********************************************************************************
 * $URL: $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.webapp.impl;

import java.io.InputStream;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.webapp.api.WebappResourceManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

/**
 * Currently used by the portal and library projects to get access to the spring WAC
 * 
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Feb 1, 2008
 * Time: 9:36:58 AM
 */
public class WebappResourceManagerImpl implements WebappResourceManager, ApplicationContextAware {

   private WebApplicationContext webApplicationContext;
   private String globalBeanId;

   public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
      if (applicationContext instanceof WebApplicationContext) {
         setWebApplicationContext((WebApplicationContext)applicationContext);
         if (getGlobalBeanId() != null) {
            WebappResourceManager other = (WebappResourceManager) ComponentManager.getInstance().get(getGlobalBeanId());
            if (other != null) {
               other.setWebApplicationContext((WebApplicationContext) applicationContext);
            }
            else {
               ComponentManager.getInstance().loadComponent(getGlobalBeanId(), this);
            }
         }
      }
      else {
         throw new BeanDefinitionValidationException(
            "WebappResourceManagerImpl must be used in a Web application context");
      }
   }

   public InputStream getResourceAsStream(String s) {
      return getWebApplicationContext().getServletContext().getResourceAsStream(s);
   }

   public WebApplicationContext getWebApplicationContext() {
      return webApplicationContext;
   }

   public void setWebApplicationContext(WebApplicationContext webApplicationContext) {
      this.webApplicationContext = webApplicationContext;
   }

   public String getGlobalBeanId() {
      return globalBeanId;
   }

   public void setGlobalBeanId(String globalBeanId) {
      this.globalBeanId = globalBeanId;
   }
}
