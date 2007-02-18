/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.portal.service;

public class PortalStringUtil
{
	/**
	 * replaces the first occurance of a string without reverting to regex or
	 * creating arrays/vectors etc could also have used StringUtil for this
	 * perpose, but wanted something simpler.
	 * 
	 * @param path
	 * @param marker
	 * @param replacement
	 * @return
	 */
	public static String replaceFirst(String path, String marker, String replacement)
	{
		if (path == null)
		{
			return path;
		}
		int i = path.indexOf(marker);
		if (i >= 0)
		{
			String before = path.substring(0, i);
			String after = path.substring(i + marker.length());
			return before + replacement + after;
		}
		return path;
	}
}
