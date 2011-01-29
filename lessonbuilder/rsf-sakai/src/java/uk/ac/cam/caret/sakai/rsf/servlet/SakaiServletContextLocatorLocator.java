/*
 * Created on 16 Mar 2007
 */
package uk.ac.cam.caret.sakai.rsf.servlet;

import java.lang.reflect.Method;

import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import uk.org.ponder.reflect.JDKReflectiveCache;
import uk.org.ponder.servletutil.ServletContextLocator;
import uk.org.ponder.util.Logger;

/**
 * Adapts Sakai ServletContextLocator service to J-ServletUtil
 * ServletContextLocator, failing quietly if not available. This should be
 * replaced by self-assembly system once available.
 */

public class SakaiServletContextLocatorLocator implements
    ApplicationContextAware {
  public static final String SAKAI_SCL = "org.sakaiproject.context.api.ServletContextLocator";
  private ApplicationContext applicationContext;

  public ServletContextLocator getServletContextLocator() {
    final Object bean = applicationContext.containsBean(SAKAI_SCL) ? applicationContext
        .getBean(SAKAI_SCL)
        : null;
    final Method[] locateContext = new Method[1];
    try {
      locateContext[0] = bean == null ? null
          : bean.getClass().getMethod("locateContext",
              new Class[] { String.class });
    }
    catch (Exception e) {
      Logger.log
          .error("Error looking up locateContext method for Sakai ServletContextLocator");
    }
    return new ServletContextLocator() {

      public ServletContext locateContext(String contextName) {
        if (locateContext[0] != null) {
          return (ServletContext) JDKReflectiveCache.invokeMethod(
              locateContext[0], bean, new Object[] { contextName });
        }
        else {
          return null;
        }
      }
    };
  }

  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }
}
