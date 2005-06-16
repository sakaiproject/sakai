/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.ui;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

/**
 * This servlet class can be used to instantiate singleton beans which provide
 * external application services via (for example) the Sakai "component" framework.
 * This ensures that the beans are in place before other applications call the
 * services.
 *
 * Because the service beans are not guaranteed to be instantiated in any
 * particular order, this approach is not suitable for core common services
 * which might be needed by the initialization methods of other applications.
 * However, for cases where service calls are made based on user interaction,
 * this lets the service implementation code stay safely bundled with the rest
 * of the web application code.
 *
 * <p>
 * USAGE: In the servlet init-param "services", specify the names of the
 * Spring beans to be loaded, separated by commas or whitespace.
 * <pre>
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;sakai.gradebook.services&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;org.sakaiproject.tool.gradebook.ui.InitializeServicesServlet&lt;/servlet-class&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;services&lt;/param-name&gt;
 *     &lt;param-value&gt;
 *       org.sakaiproject.service.gradebook.GradebookService
 *       org.sakaiproject.service.gradebook.shared.GradebookArchiveService
 *     &lt;/param-value&gt;
 *   &lt;/init-param&gt;
 *   &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
 * &lt;/servlet&gt;
 * </pre>
 */
public class InitializeServicesServlet extends HttpServlet {
	private static Log logger = LogFactory.getLog(InitializeServicesServlet.class);
	public final static String SERVICES_PARAMETER = "services";

	/**
     * Instantiate any external service implementations by loading the equivalent
     * Spring singleton beans.
	 */
    public void init() throws ServletException {
		super.init();
		String servicesList = getInitParameter(SERVICES_PARAMETER);
		if (logger.isDebugEnabled()) logger.debug("servicesList=" + servicesList);
		if (servicesList != null) {
			ApplicationContext ac = (ApplicationContext)getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
			if (logger.isInfoEnabled()) logger.info("init ac=" + ac);
			String[] services = servicesList.split("[,\\s]+");
			for (int i = 0; i < services.length; i++) {
				Object serviceBean = ac.getBean(services[i]);
				if (logger.isInfoEnabled()) logger.info(" loaded service=" + serviceBean);
			}
		}
	}

}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Id$
 *************************************************************************************************************************************************************************************************************************************************************/
