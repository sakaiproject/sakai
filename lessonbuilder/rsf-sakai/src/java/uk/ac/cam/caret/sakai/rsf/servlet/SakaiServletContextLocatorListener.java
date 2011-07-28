/*
 * Created on 16 Mar 2007
 */
package uk.ac.cam.caret.sakai.rsf.servlet;

import java.lang.reflect.Method;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import uk.org.ponder.servletutil.ServletUtil;
import uk.org.ponder.util.Logger;

public class SakaiServletContextLocatorListener implements
    ServletContextListener {

  private Object locator;
  private String contextName;

  private static String error = "Unable to load ServletContextLocator from Sakai component manager, aborting registration";

  public void contextInitialized(ServletContextEvent sce) {
    ServletContext context = sce.getServletContext();
    try {
      WebApplicationContext wac = WebApplicationContextUtils
          .getWebApplicationContext(context);
      if (wac == null) {
        Logger.log.error("Error locating application context");
        return;
      }
      locator = wac.getBean(SakaiServletContextLocatorLocator.SAKAI_SCL);
      if (locator == null) {
        Logger.log.warn(error);
        return;
      }
    }
    catch (Exception e) {
      Logger.log.error(error, e);
    }

    String name = context.getInitParameter("sakai-context-name");
    if (name == null) {
      name = ServletUtil.computeContextName(context);
    }
    contextName = name;
    try {
      Method method = locator.getClass().getMethod("registerContext",
          new Class[] { String.class, ServletContext.class });
      method.invoke(locator, new Object[] { name, context });
    }
    catch (Exception e) {
      Logger.log.error("Error registering context with name " + name, e);
    }

  }

  public void contextDestroyed(ServletContextEvent sce) {
    if (locator != null) {
      try {
        Method method = locator.getClass().getMethod("deregisterContext",
            new Class[] { String.class, ServletContext.class });
        method.invoke(locator, new Object[] { contextName,
            sce.getServletContext() });
      }
      catch (Exception e) {
        Logger.log.error(
            "Error deregistering context with name " + contextName, e);
      }
    }
  }

}
