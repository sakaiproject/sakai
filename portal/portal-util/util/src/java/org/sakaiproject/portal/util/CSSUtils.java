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

import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.component.cover.ServerConfigurationService;

public class CSSUtils
{

	/**
	 * Returns a URL for the tool_base.css suitable for putting in an href= field.
	 *
	 * @return <code>cssToolBase</code> URL for the tool_base.css
	 */
	public static String getCssToolBase() 
	{
		String skinRepo = ServerConfigurationService.getString("skin.repo");
		String cssToolBase = skinRepo + "/tool_base.css";
		return cssToolBase;
	}

	/**
	 * Captures the (yes) overly complex rules for the skin folder naming convention
	 *
	 * @param <code>skinFolder</code>
	 *		The folder where the skins are to be found.
	 * @return <code>skinFolder</code> The adjusted folder where the skins can be found.
	 */
	public static String adjustCssSkinFolder(String skinFolder)
	{
		if (skinFolder == null)
		{
			skinFolder = ServerConfigurationService.getString("skin.default");
			if ( skinFolder == null ) skinFolder = ""; // Not likely - not good if it happens
		}
		return skinFolder;
	}

	/**
	 * Returns a URL for the tool.css suitable for putting in an href= field.
	 *
	 * @param <code>skinFolder</code>
	 *		where the tool.css skin lives for this site.
	 * @return <code>cssToolSkin</code> URL for the tool.css
	 */
	public static String getCssToolSkin(String skinFolder)
	{
		skinFolder = adjustCssSkinFolder(skinFolder);
		String skinRepo = ServerConfigurationService.getString("skin.repo");
		String cssToolSkin = skinRepo + "/" + skinFolder + "/tool.css";
		return cssToolSkin;
	}

	/**
	 * Returns a URL for the portal.css suitable for putting in an href= field.
	 *
	 * @param <code>skinFolder</code>
	 *		where the portal.css skin lives for this site.
	 * @return <code>cssPortalSkin</code> URL for the portal.css
	 */
	public static String getCssPortalSkin(String skinFolder)
	{
		skinFolder = adjustCssSkinFolder(skinFolder);
		String skinRepo = ServerConfigurationService.getString("skin.repo");
		String cssPortalSkin = skinRepo + "/" + skinFolder + "/portal.css";
		return cssPortalSkin;
	}


	/**
	 * Returns a URL for the tool.css suitable for putting in an href= field.
	 *
	 * @param <code>skinFolder</code>
	 *		where the tool.css skin lives for this site.
	 * @return <code>cssToolSkin</code> URL for the tool.css
	 */
	public static String getCssToolSkinCDN(String skinFolder)
	{
		String cssToolSkin = getCssToolSkin(skinFolder);
		if ( cssToolSkin.startsWith("/") ) {
			cssToolSkin = PortalUtils.getCDNPath() + cssToolSkin + PortalUtils.getCDNQuery();
		}
		return cssToolSkin;
	}

	/**
	 * Returns a URL for the tool.css suitable for putting in an href= field.
	 *
	 * @param <code>site</code>
	 *		The site for this tool.
	 * @return <code>cssToolSkin</code> URL for the tool.css
	 */
	public static String getCssToolSkin(Site site)
	{
		String skinFolder = site.getSkin();
		return getCssToolSkin(skinFolder);
	}

	/**
	 * Returns a URL for the tool.css suitable for putting in an href= field.
	 *
	 * @param <code>site</code>
	 *		The site for this tool.
	 * @return <code>cssToolSkin</code> URL for the tool.css
	 */
	public static String getCssToolSkinCDN(Site site)
	{
		String skinFolder = site.getSkin();
		return getCssToolSkinCDN(skinFolder);
	}


}

