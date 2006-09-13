/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package uk.ac.cam.caret.sakai.rwiki.component.macros;

import java.io.IOException;
import java.io.Writer;

import org.radeox.api.engine.ImageRenderEngine;
import org.radeox.api.engine.RenderEngine;
import org.radeox.macro.BaseLocaleMacro;
import org.radeox.macro.parameter.MacroParameter;
import org.radeox.util.Encoder;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;

import uk.ac.cam.caret.sakai.rwiki.component.radeox.service.impl.SpecializedRenderContext;

/**
 * This is a reimplementation of the LinkMacro but made aware of the sakai://
 * and worksite:// url formats
 * 
 * @author andrew
 */
public class SakaiLinkMacro extends BaseLocaleMacro
{
	private static String[] paramDescription = {
			"1,text: Text of the link ",
			"2,url: URL of the link, if this is external and no target is specified, a new window will open ",
			"3,img: (optional) if 'none' then no small URL image will be used",
			"4,target: (optional) Target window, if 'none' is specified, the url will use the current window",
			"Remember if using positional parameters, you must include dummies for the optional parameters" };

	private static String description = "Generated a link";

	public String[] getParamDescription()
	{
		return paramDescription;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.radeox.macro.Macro#getDescription()
	 */
	public String getDescription()
	{
		return description;
	}

	public String getLocaleKey()
	{
		return "macro.link";
	}

	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException
	{

		SpecializedRenderContext context = (SpecializedRenderContext) params
				.getContext();
		RenderEngine engine = context.getRenderEngine();

		String text = params.get("text", 0);
		String url = params.get("url", 1);
		String img = params.get("img", 2);
		String target = params.get("target", 3);

		// check for single url argument (text == url)
		if (params.getLength() == 1)
		{
			url = text;
			text = Encoder.toEntity(text.charAt(0))
					+ Encoder.escape(text.substring(1));
		}

		if (url != null && text != null)
		{
			if (target == null)
			{
				
				if (url.startsWith("sakai:") || url.startsWith("worksite:/") || url.indexOf(":/") < 0 || url.indexOf(":/") > 10)
				{
					target = "none";
				}
				else
				{
					target = "rwikiexternal";
				}

			}




			writer.write("<span class=\"nobr\">");
			if (!"none".equals(img) && engine instanceof ImageRenderEngine)
			{
				writer.write(((ImageRenderEngine) engine)
						.getExternalImageLink());
			}
			

			String siteId = context.getSiteId();

			String siteType = null;
			try
			{
				Site s = SiteService.getSite(siteId);
				siteType = s.getType();
			}
			catch (IdUnusedException e)
			{
			}
			
			url = convertLink(url, siteType, siteId);

			writer.write("<a href=\"" + Encoder.escape(url) + "\"");
			if (!"none".equals(target))
			{
				writer.write("target=\"" + Encoder.escape(target) + "\"");
			}
			writer.write(">");
			writer.write(text);
			writer.write("</a></span>");
		}
		else
		{
			throw new IllegalArgumentException(
					"link needs a name and a url as argument");
		}
		return;
	}
	private String convertLink(String link, String siteType, String siteId)
	{

		if (link.startsWith("sakai:/"))
		{
			String refSiteUrl = link.substring("sakai:/".length());
			if ( refSiteUrl.startsWith("/") ) {
				refSiteUrl = refSiteUrl.substring(1);
			}
			String[] parts = refSiteUrl.split("/");
			if (parts == null || parts.length < 1)
			{
				return "Link Cant be resolved";
			}

			String regSiteId = parts[0];
			String regSiteType = "group";
			try
			{
				Site s = SiteService.getSite(regSiteId);
				regSiteType = s.getType();

			}
			catch (IdUnusedException e)
			{
			}
			if ((regSiteId != null && regSiteId.startsWith("~")) || regSiteType == null)
			{
				String remLink = link.substring("sakai:/".length());
				if ( remLink.startsWith("/") ) {
					remLink = remLink.substring(1);
				}
				if ( remLink.startsWith("~") ) {
					remLink = remLink.substring(1);
				}
				link = "/access/content/user/"
						+ remLink;

			}
			else
			{
				link = "/access/content/group/"
						+ link.substring("sakai:/".length());
			}
		}
		else if (link.startsWith("worksite:/"))
		{
			// need to check siteid

			
			if ((siteId != null && siteId.startsWith("~")) || siteType == null)
			{
				if ( siteId.startsWith("~") ) {
					siteId = siteId.substring(1);
				}
				link = "/access/content/user/" + siteId + "/"
						+ link.substring("worksite:/".length());

			}
			else
			{
				link = "/access/content/group/" + siteId + "/"
						+ link.substring("worksite:/".length());
			}

		}
		return link;
	}

}
