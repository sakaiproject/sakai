package org.sakaiproject.webapp.api;

import org.springframework.web.context.WebApplicationContext;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Feb 1, 2008
 * Time: 9:33:52 AM
 * To change this template use File | Settings | File Templates.
 */

/**
 * this interface allows a webapp to expose it's internal resources
 * through the component manager.  webapps may implement this interface to 
 * perform filtering, or there is a default implementation that can be used
 * 
 * webapps should expose this with a bean id like:
 * org.sakaiproject.component.api.WebappResourceManager.<webapp name>
 */
public interface WebappResourceManager {
   
   java.io.InputStream getResourceAsStream(java.lang.String s);

   void setWebApplicationContext(WebApplicationContext webApplicationContext);
}
