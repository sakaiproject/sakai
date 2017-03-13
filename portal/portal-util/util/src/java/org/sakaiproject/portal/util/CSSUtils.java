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
	 * Returns a URL for the tool_base.css suitable for putting in an href= fieldresolved for CDN
	 *
	 * @return <code>cssToolBase</code> URL for the tool_base.css
	 */
	public static String getCssToolBaseCDN() 
	{
		String cssToolBaseCDN = PortalUtils.getCDNPath()
		+ getCssToolBase()
		+ PortalUtils.getCDNQuery();
		return cssToolBaseCDN;
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
	 * Returns a URL for the print.css suitable for putting in an href= field for media="print".
	 *
	 * @param <code>skinFolder</code>
	 *		where the print.css skin lives for this site.
	 * @return <code>cssPrintSkin</code> URL for the print.css
	 */
	public static String getCssPrintSkin(String skinFolder)
	{
		skinFolder = adjustCssSkinFolder(skinFolder);
		String skinRepo = ServerConfigurationService.getString("skin.repo");
		String cssPrintSkin = skinRepo + "/" + skinFolder + "/print.css";
		return cssPrintSkin;
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
	 * Returns a URL for the portal.css suitable for putting in an href= field resolved for CDN
	 *
	 * @param <code>skinFolder</code>
	 *		where the portal.css skin lives for this site.
	 * @return <code>cssPortalSkin</code> URL for the portal.css
	 */
	public static String getCssPortalSkinCDN(String skinFolder)
	{
		String cssPortalSkin = PortalUtils.getCDNPath()
				+ getCssPortalSkin(skinFolder)
				+ PortalUtils.getCDNQuery();

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
	 * Returns a URL for the print.css suitable for putting in an href= field for media="print"
	 *
	 * @param <code>skinFolder</code>
	 *		where the print.css skin lives for this site.
	 * @return <code>cssPrintSkin</code> URL for the print.css
	 */
	public static String getCssPrintSkinCDN(String skinFolder)
	{
		String cssPrintSkin = getCssPrintSkin(skinFolder);
		if ( cssPrintSkin.startsWith("/") ) {
			cssPrintSkin = PortalUtils.getCDNPath() + cssPrintSkin + PortalUtils.getCDNQuery();
		}
		return cssPrintSkin;
	}

	/** 
	 * Convenience method to retrieve the skin folder from a site
	 * @param site
	 * @return skinFolder
	 */
	public static String getSkinFromSite(Site site) {
		String skinFolder=null;
		if (site!=null) {
			skinFolder = site.getSkin();
		}
		return skinFolder;
	}

	/** 
	 * Gets the full CSS link for the portal skin 
	 * @param skin
	 * @return headCssPortalSkin
	 */
	public static String getPortalSkinLink(String skin) {
		String headCssPortalSkin = "<link href=\"" 
				+ getCssPortalSkinCDN(skin)
				+ "\" type=\"text/css\" rel=\"stylesheet\" media=\"screen, tty, tv, handheld, projection\" />\n";
		return headCssPortalSkin;
	}
	
	/** 
	 * Gets the full CSS link for the tool skin (including print version)
	 * @param skin
	 * @param isInlineRequest
	 * @return headCssToolBse
	 */
	public static String getCssToolBaseLink(String skin,boolean isInlineRequest) {

		String headCssToolBase = "<link href=\""
				+ getCssToolBaseCDN()
				+ "\" type=\"text/css\" rel=\"stylesheet\" media=\"screen, tty, tv, handheld, projection\" />\n";

		if ( ! isInlineRequest ) {
			String headCssPortalSkin = "<link href=\"" 
				+ getCssPortalSkinCDN(skin)
				+ "\" type=\"text/css\" rel=\"stylesheet\" media=\"screen, tty, tv, handheld, projection\" />\n";
			headCssToolBase = headCssPortalSkin + headCssToolBase;
		}
		return headCssToolBase;

	}
	
	public static String getCssToolSkinLink(String skin, boolean isInlineRequest) {

		if (isInlineRequest)
		{
			return "";
		}

		String headCssToolSkin = "<link href=\"" 
				+ getCssToolSkinCDN(skin)
				+ "\" type=\"text/css\" rel=\"stylesheet\" media=\"screen, tty, tv, handheld, projection\" />\n";

		String headCssPrintSkin = "<link href=\"" 
				+ getCssPrintSkinCDN(skin)
				+ "\" type=\"text/css\" rel=\"stylesheet\" media=\"print\" />\n";
		
		return headCssToolSkin + headCssPrintSkin;
	}

	public static String getCssHead(String skin, boolean isInlineRequest) {
		// setup html information that the tool might need (skin, body on load,
		// js includes, etc).
		String headCss = getCssToolBaseLink(skin,isInlineRequest);
		if (!isInlineRequest)
		{
			headCss += getCssToolSkinLink(skin, isInlineRequest);
		}
		return headCss;
	}

}
