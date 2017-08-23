/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2013 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.portal.util;

import java.time.Instant;

import org.sakaiproject.component.cover.ServerConfigurationService;

public class PortalUtils
{

	/**
	 * Returns an absolute URL for "/library" servlet with CDN path as necessary
	 */
	public static String getLibraryPath()
	{
		return getCDNPath() + "/library/";
	}

	/**
	 * Returns an absolute for "/library/webjars" with CDN path as necessary
	 */
	public static String getWebjarsPath()
	{
		return getLibraryPath() + "webjars/";
	}

	/**
	 * Returns an absolute for "/library/js" servlet with CDN path as necessary
	 */
	public static String getScriptPath()
	{
		return getLibraryPath() + "js/";
	}

	/**
	 * Returns the CDN Path or empty string (i.e. never null)
	 */
	public static String getCDNPath()
	{
		return ServerConfigurationService.getString("portal.cdn.path", "");
	}

	/**
	 * Returns the CDN query string or empty string (i.e. never null)
	 */
	public static String getCDNQuery()
	{
		long expire = ServerConfigurationService.getInt("portal.cdn.expire", 0);
		String version = ServerConfigurationService.getString("portal.cdn.version", ServerConfigurationService.getString("version.service", "0"));
		StringBuilder cdnQuery = new StringBuilder();
		cdnQuery.append("?version=").append(version);
		if ( expire > 0 ) {
			Instant instant = Instant.now();
			expire = instant.getEpochSecond() / expire;
			cdnQuery.append("&expire=").append(expire);
		}
		return cdnQuery.toString();
	}

	/**
	 * Returns the text to intelligently include the latest version of jQuery
     * 
     * @param where A string to be used in browser console log messages
	 */
	public static String includeLatestJQuery(String where)
	{
        String retval = 
             "<script type=\"text/javascript\">\n" +
             "var needJQuery = true;\n" +
             "if ( window.jQuery ) {\n" +
             "       tver = jQuery.fn.jquery;\n" +
             "       if ( tver.indexOf('1.12.') == 0 ) {\n" +
             "               window.console && console.log('"+where+" PortalUtils.includeLatestJquery() detected jQuery '+tver);\n" +
             "               needJQuery = false;\n" +
             "       } else {\n" +
             "               var overrideJQuery = true;\n" +
             "               window.console && console.log('"+where+" PortalUtils.includeLatestJquery() found jQuery '+tver);\n" +
             "       }\n" +
             "}\n" +
             "if ( needJQuery ) {\n" +
             "   document.write('\\x3Cscript type=\"text/javascript\" src=\"" +
                 getLatestJQueryPath() + getCDNQuery() + 
                 "\">'+'\\x3C/script>')\n" +
             "   document.write('\\x3Cscript type=\"text/javascript\" src=\"" +
                 getWebjarsPath() + "jquery-migrate/1.4.1/jquery-migrate.min.js" + getCDNQuery() + 
                 "\">'+'\\x3C/script>')\n" +
             "   document.write('\\x3Cscript type=\"text/javascript\" src=\"" +
                 getWebjarsPath() + "bootstrap/3.3.7/js/bootstrap.min.js" + getCDNQuery() +
                 "\">'+'\\x3C/script>')\n" +
             "   document.write('\\x3Cscript type=\"text/javascript\" src=\"" +
                 getWebjarsPath() + "jquery-ui/1.12.1/jquery-ui.min.js" + getCDNQuery() + 
                 "\">'+'\\x3C/script>')\n" +
             "   document.write('\\x3Clink rel=\"stylesheet\" href=\"" +
                 getWebjarsPath() + "jquery-ui/1.12.1/jquery-ui.min.css" + getCDNQuery() + 
                 "\"/>')\n" +
             "} else { \n" +
             "   window.console && console.log('jQuery already loaded '+jQuery.fn.jquery+' in '+'" + where + "');\n" +
             "   if (typeof jQuery.migrateWarnings == 'undefined') {\n" +
             "           document.write('\\x3Cscript type=\"text/javascript\" src=\"" + getWebjarsPath() + "jquery/jquery-migrate-1.4.1.min.js" + getCDNQuery() + "\">'+'\\x3C/script>')\n" +
             "           window.console && console.log('Adding jQuery migrate');\n" +
             "   }\n" +
             "   if ( typeof jQuery.fn.popover == 'undefined') {\n" +
             "           document.write('\\x3Cscript type=\"text/javascript\" src=\"" + getWebjarsPath() + "bootstrap/3.3.7/js/bootstrap.min.js" + getCDNQuery() + "\">'+'\\x3C/script>')\n" +
             "           window.console && console.log('Adding Bootstrap');\n" +
             "   }\n" +
             "   if (typeof jQuery.ui == 'undefined') {\n" +
             "           document.write('\\x3Cscript type=\"text/javascript\" src=\"" + getWebjarsPath() + "jquery-ui/1.12.1/jquery-ui.min.js" + getCDNQuery() + "\">'+'\\x3C/script>')\n" +
             "           document.write('\\x3Clink rel=\"stylesheet\" href=\"" + getWebjarsPath() + "jquery-ui/1.12.1/jquery-ui.min.css" + getCDNQuery() + "\"/>')\n" +
             "           window.console && console.log('Adding jQuery UI');\n" +
             "   }\n" +
             "}\n" +
             "</script>\n" +
             "<script type=\"text/javascript\">\n" +
             "if ( needJQuery ) {\n" +
             "       window.console && console.log('"+where+" PortalUtils.includeLatestJquery() loaded jQuery+migrate+Bootstrap+UI '+$.fn.jquery);\n" +
             "}\n" +
	     "$PBJQ = jQuery;\n" +
             "</script>\n";

		return retval;
	}

	public static String getLatestJQueryPath() {
		 return getWebjarsPath() + "jquery/1.12.4/jquery.min.js";
	}
}

