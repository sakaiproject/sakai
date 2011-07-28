/*
 * Created on Feb 28, 2005
 */
package uk.ac.cam.caret.sakai.rsf.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.tool.api.Tool;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import uk.org.ponder.beanutil.BeanLocator;
import uk.org.ponder.rsac.RSACBeanLocator;
import uk.org.ponder.rsac.servlet.RSACUtils;
import uk.org.ponder.rsf.processor.ForcibleException;
import uk.org.ponder.util.Logger;
import uk.org.ponder.util.UniversalRuntimeException;

/**
 * The main servlet to be used when handling an RSF servlet request through
 * Sakai.
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * 
 */
public class ReasonableSakaiServlet extends HttpServlet {
  private RSACBeanLocator rsacbl;

  public void init(ServletConfig config) {
    try {
      super.init(config);
      ServletContext context = getServletContext();
      Logger.log.info("ReasonableSakaiServlet starting up for context "
          + context.getRealPath(""));

      WebApplicationContext wac = WebApplicationContextUtils
          .getWebApplicationContext(context);
      if (wac == null) {
        throw new IllegalStateException("Error acquiring web application context " +
                "- servlet context not configured correctly");
      }
      rsacbl = (RSACBeanLocator) 
        wac.getBean(RSACBeanLocator.RSAC_BEAN_LOCATOR_NAME);
    }
    catch (Throwable t) {
      Logger.log.warn("Error initialising SakaiRSF servlet: ", t);
    }
  }

  protected void service(HttpServletRequest req, HttpServletResponse res) {
    try {
      req.setCharacterEncoding("UTF-8");
      // This line was added for Sakai 2.0
      req.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);
 
      RSACUtils.startServletRequest(req, res, 
          rsacbl, RSACUtils.HTTP_SERVLET_FACTORY);
      // A request bean locator just good for this request.
      BeanLocator rbl = rsacbl.getBeanLocator();
     
      // pass the request to RSF. 
      rbl.locateBean("rootHandlerBean");
    }
    catch (Throwable t) {
      if (t instanceof UniversalRuntimeException) {
        UniversalRuntimeException ure = (UniversalRuntimeException) t;
        if (ForcibleException.class.isAssignableFrom(ure.getCategory())) {
          throw (ure);
        }
      }
      Logger.log.warn("Error servicing SakaiRSF request ", t);
      try {
        res.getWriter().println(
            "[An error occurred handling this RSF request]");
        if (rsacbl == null) {
          res.getWriter().println(
              "[Context has not been started properly]");
        }
        res.getWriter().close();
      }
      catch (Exception e) {
      }
      throw UniversalRuntimeException.accumulate(t,
          "Error servicing SakaiRSF request ");
    }
    finally {
      if (rsacbl != null) {
        rsacbl.endRequest();
      }
    }

  }
}