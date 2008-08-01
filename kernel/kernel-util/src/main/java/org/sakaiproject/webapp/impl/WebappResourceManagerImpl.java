package org.sakaiproject.webapp.impl;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.webapp.api.WebappResourceManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Feb 1, 2008
 * Time: 9:36:58 AM
 * To change this template use File | Settings | File Templates.
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
