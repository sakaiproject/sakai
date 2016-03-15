/**
 * Copyright © 2005, CARET, University of Cambridge
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */
/*
 * Created on 16 Mar 2007
 */
package org.sakaiproject.rsf.servlet;

import java.lang.reflect.Method;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import uk.org.ponder.servletutil.ServletUtil;
import uk.org.ponder.util.Logger;

public class SakaiServletContextLocatorListener implements ServletContextListener {

	private Object locator;
	private String contextName;

	private static String error = "Unable to load ServletContextLocator from Sakai component manager, aborting registration";

	public void contextInitialized(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		try {
			WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(context);
			if (wac == null) {
				Logger.log.error("Error locating application context");
				return;
			}
			locator = wac.getBean(SakaiServletContextLocatorLocator.SAKAI_SCL);
			if (locator == null) {
				Logger.log.warn(error);
				return;
			}
		} catch (Exception e) {
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
		} catch (Exception e) {
			Logger.log.error("Error registering context with name " + name, e);
		}

	}

	public void contextDestroyed(ServletContextEvent sce) {
		if (locator != null) {
			try {
				Method method = locator.getClass().getMethod("deregisterContext",
						new Class[] { String.class, ServletContext.class });
				method.invoke(locator, new Object[] { contextName, sce.getServletContext() });
			} catch (Exception e) {
				Logger.log.error("Error deregistering context with name " + contextName, e);
			}
		}
	}

}
