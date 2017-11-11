/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.portal.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 */
@Slf4j
public class URLUtils
{
	public static String addParameter(String URL, String name, String value)
	{
		int qpos = URL.indexOf('?');
		int hpos = URL.indexOf('#');
		char sep = qpos == -1 ? '?' : '&';
		String seg = sep + encodeUrl(name) + '=' + encodeUrl(value);
		return hpos == -1 ? URL + seg : URL.substring(0, hpos) + seg
				+ URL.substring(hpos);
	}

	/**
	 * The same behaviour as Web.escapeUrl, only without the "funky encoding" of
	 * the characters ? and ; (uses JDK URLEncoder directly).
	 * 
	 * @param toencode
	 *        The string to encode.
	 * @return <code>toencode</code> fully escaped using URL rules.
	 */
	public static String encodeUrl(String url)
	{
		try
		{
			return URLEncoder.encode(url, "UTF-8");
		}
		catch (UnsupportedEncodingException uee)
		{
			throw new IllegalArgumentException(uee);
		}
	}

	/**
	 * The sanitize the req.getPathInfo() information.
	 * 
	 * @param req
	 *        The current servlet request
	 * @return <code>safePathInfo</code> sanitized pathInfo
	 */

	// The characters we should never expect to see in a pathInfo
	public static String BAD_PATH_URL_CHARS = "\"'<>";

	public static String getSafePathInfo(HttpServletRequest req)
	{
		String pathInfo = req.getPathInfo();
		if ( pathInfo == null ) return null;
		String newPathInfo = pathInfo;
		for (int i =0; i < pathInfo.length() - 1; i++) {
			if (BAD_PATH_URL_CHARS.indexOf(pathInfo.charAt(i)) >= 0) {
				newPathInfo = pathInfo.substring(0,i);
				break;
			}
		}
		
		if (! newPathInfo.equals(pathInfo) ) {
			String ipAddress = req.getRemoteAddr();

			log.warn("Truncated pathInfo IP="+ipAddress+" from "+pathInfo+" to "+newPathInfo);
		}
		return newPathInfo;
	}

}
