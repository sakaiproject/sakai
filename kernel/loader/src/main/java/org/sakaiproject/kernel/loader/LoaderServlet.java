package org.sakaiproject.kernel.loader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;

public class LoaderServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2435254891070526945L;

	private static final Log log = LogFactory.getLog("sakai-kernel");

	@Override
	public void init(ServletConfig arg0) throws ServletException {
		super.init(arg0);
		long start = System.currentTimeMillis();
		try {
			log.info("START---------------------- Loading kernel ");
			org.sakaiproject.component.api.ComponentManager cm = ComponentManager
					.getInstance();
		} catch (Throwable t) {
			log.error("Failed to Startup ", t);
		}
		log.info("END------------------------ Loaded kernel in  "
				+ (System.currentTimeMillis() - start) + "ms");
	}
}
