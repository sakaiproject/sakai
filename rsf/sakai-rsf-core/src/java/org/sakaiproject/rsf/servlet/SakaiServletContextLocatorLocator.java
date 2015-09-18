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

public class SakaiServletContextLocatorLocator implements ApplicationContextAware {
	public static final String SAKAI_SCL = "org.sakaiproject.context.api.ServletContextLocator";
	private ApplicationContext applicationContext;

	public ServletContextLocator getServletContextLocator() {
		final Object bean = applicationContext.containsBean(SAKAI_SCL) ? applicationContext.getBean(SAKAI_SCL) : null;
		final Method[] locateContext = new Method[1];
		try {
			locateContext[0] = bean == null ? null
					: bean.getClass().getMethod("locateContext", new Class[] { String.class });
		} catch (Exception e) {
			Logger.log.error("Error looking up locateContext method for Sakai ServletContextLocator");
		}
		return new ServletContextLocator() {

			public ServletContext locateContext(String contextName) {
				if (locateContext[0] != null) {
					return (ServletContext) JDKReflectiveCache.invokeMethod(locateContext[0], bean,
							new Object[] { contextName });
				} else {
					return null;
				}
			}
		};
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
}
