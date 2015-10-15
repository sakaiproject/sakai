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

import java.util.Date;

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
		long expire = ServerConfigurationService.getInt("portal.cdn.expire",0);
		String version = ServerConfigurationService.getString("portal.cdn.version");
		if ( expire < 1 && version == null ) return "";
		String retval = "?";
		if ( expire > 0 ) {
			Date dt = new Date();
			long timeVal = dt.getTime() / 1000; // Seconds...
			expire = timeVal / expire;
			retval = retval + "expire=" + expire;
			if ( version != null ) retval = retval + "&";
		}
		if ( version != null ) retval = retval + "version=" + version;
		return retval;
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
             "       if ( tver.indexOf('1.11.') == 0 ) {\n" +
             "               window.console && console.log('"+where+" PortalUtils.includeLatestJquery() detected jQuery '+tver);\n" +
             "               needJQuery = false;\n" +
             "       } else {\n" +
             "               var overrideJQuery = true;\n" +
             "               window.console && console.log('"+where+" PortalUtils.includeLatestJquery() found jQuery '+tver);\n" +
             "       }\n" +
             "}\n" +
             "if ( needJQuery ) {\n" +
             "   document.write('\\x3Cscript type=\"text/javascript\" src=\"" +
                 getScriptPath() + "jquery/jquery-1.11.3.min.js" + getCDNQuery() + 
                 "\">'+'\\x3C/script>')\n" +
             "   document.write('\\x3Cscript type=\"text/javascript\" src=\"" +
                 getScriptPath() + "jquery/jquery-migrate-1.2.1.min.js" + getCDNQuery() + 
                 "\">'+'\\x3C/script>')\n" +
             "   document.write('\\x3Cscript type=\"text/javascript\" src=\"" +
                 getScriptPath() + "bootstrap/3.3.5/js/bootstrap.min.js" + getCDNQuery() + 
                 "\">'+'\\x3C/script>')\n" +
             "   document.write('\\x3Cscript type=\"text/javascript\" src=\"" +
                 getScriptPath() + "jquery/ui/1.11.3/jquery-ui.min.js" + getCDNQuery() + 
                 "\">'+'\\x3C/script>')\n" +
             "} else { \n" +
             "   window.console && console.log('jQuery already loaded '+jQuery.fn.jquery+' in '+where);\n" +
             "   if (typeof jQuery.migrateWarnings == 'undefined') {\n" +
             "           document.write('\\x3Cscript type=\"text/javascript\" src=\"" + getScriptPath() + "jquery/jquery-migrate-1.2.1.min.js" + getCDNQuery() + "\">'+'\\x3C/script>')\n" +
             "           window.console && console.log('Adding jQuery migrate');\n" +
             "   }\n" +
             "   if ( typeof jQuery.fn.popover == 'undefined') {\n" +
             "           document.write('\\x3Cscript type=\"text/javascript\" src=\"" + getScriptPath() + "bootstrap/3.3.5/js/bootstrap.min.js" + getCDNQuery() + "\">'+'\\x3C/script>')\n" +
             "           window.console && console.log('Adding Bootstrap');\n" +
             "   }\n" +
             "   if (typeof jQuery.ui == 'undefined') {\n" +
             "           document.write('\\x3Cscript type=\"text/javascript\" src=\"" + getScriptPath() + "jquery/ui/1.11.3/jquery-ui.min.js" + getCDNQuery() + "\">'+'\\x3C/script>')\n" +
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

}

