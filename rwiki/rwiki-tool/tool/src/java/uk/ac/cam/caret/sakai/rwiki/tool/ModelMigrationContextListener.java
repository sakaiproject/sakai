/**
 * 
 */
package uk.ac.cam.caret.sakai.rwiki.tool;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import uk.ac.cam.caret.sakai.rwiki.service.api.model.DataMigrationController;

/**
 * @author ieb
 */

// FIXME: Tool
public class ModelMigrationContextListener implements ServletContextListener
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent contextEvent)
	{
		try
		{
			WebApplicationContext wac = WebApplicationContextUtils
					.getWebApplicationContext(contextEvent.getServletContext());
			// Logger log = (Logger)wac.getBean("rwiki-logger");
			DataMigrationController dataMig = (DataMigrationController) wac
					.getBean("rwikiDataMigration");
			dataMig.update();
		}
		catch (Exception ex)
		{
			throw new RuntimeException(
					"Data Migration Failed, you should investigate this before restarting, or remove the RWiki tool from Sakai",
					ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent contextEvent)
	{
		// TODO Auto-generated method stub

	}

}
